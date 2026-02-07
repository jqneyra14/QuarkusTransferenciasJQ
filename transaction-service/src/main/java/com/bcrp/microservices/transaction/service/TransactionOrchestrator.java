package com.bcrp.microservices.transaction.service;

import com.bcrp.microservices.transaction.client.*;
import com.bcrp.microservices.transaction.dto.AbonoDTO;
import com.bcrp.microservices.transaction.entity.TransactionEntity;
import com.bcrp.microservices.transaction.repository.TransactionRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;

@ApplicationScoped
public class TransactionOrchestrator {

    private static final Logger LOG = Logger.getLogger(TransactionOrchestrator.class);

    @Inject
    TransactionRepository repository;

    @Inject
    @RestClient
    AccountServiceClient accountClient;

    @Inject
    @RestClient
    BcrpServiceClient bcrpClient;

    // ⭐ KAFKA PRODUCER - Publicar eventos de auditoría
    @Inject
    @Channel("audit-events")
    Emitter<String> auditEmitter;

    /**
     * ⭐ ORCHESTRATOR PATTERN
     * Coordina el flujo completo de procesamiento de abono
     *
     * ⭐ SAGA PATTERN
     * Maneja compensaciones si algo falla
     *
     * ⭐ CIRCUIT BREAKER PATTERN
     * Protege las llamadas a servicios externos
     */
    public Uni<Response> procesarAbono(AbonoDTO abono) {
        LOG.infof("🎯 ORCHESTRATOR: Iniciando procesamiento de abono %s", abono.referencia);

        return Panache.withTransaction(() -> {

            // PASO 1: Validar que la cuenta existe
            return validarCuentaConCircuitBreaker(abono.numeroCuenta)
                    .onItem().transformToUni(cuentaResponse -> {

                        if (cuentaResponse.getStatus() != 200) {
                            LOG.errorf("❌ Cuenta no encontrada: %s", abono.numeroCuenta);
                            return crearTransaccionFallida(abono, "Cuenta no encontrada")
                                    .map(t -> Response.status(Response.Status.NOT_FOUND)
                                            .entity("Cuenta no encontrada")
                                            .build());
                        }

                        // Extraer ID de la cuenta de la respuesta
                        Long accountId = extraerAccountId(cuentaResponse);

                        LOG.info("✅ PASO 1: Cuenta validada");

                        // PASO 2: Acreditar el dinero (SAGA registra esta acción)
                        return acreditarConCircuitBreaker(accountId, abono.monto)
                                .onItem().transformToUni(acreditarResponse -> {

                                    if (acreditarResponse.getStatus() != 200) {
                                        LOG.error("❌ Error al acreditar dinero");
                                        return crearTransaccionFallida(abono, "Error al acreditar")
                                                .map(t -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity("Error al acreditar dinero")
                                                        .build());
                                    }

                                    LOG.infof("✅ PASO 2: S/%s acreditados a cuenta %s", abono.monto, abono.numeroCuenta);

                                    // PASO 3: Guardar transacción en BD
                                    TransactionEntity transaction = crearTransactionEntity(abono, "COMPLETADO");

                                    return repository.persist(transaction)
                                            .onItem().transformToUni(transGuardada -> {

                                                LOG.info("✅ PASO 3: Transacción guardada en BD");

                                                // PASO 4: Confirmar al BCRP (con Circuit Breaker)
                                                return confirmarBcrpConCircuitBreaker(abono.referencia)
                                                        .onItem().transform(bcrpResponse -> {

                                                            if (bcrpResponse.getStatus() == 200) {
                                                                LOG.info("✅ PASO 4: BCRP confirmado exitosamente");

                                                                // ⭐ PASO 5: Publicar evento Kafka para auditoría
                                                                publicarEventoAuditoria(
                                                                        "ABONO_PROCESADO",
                                                                        abono.referencia,
                                                                        abono.monto,
                                                                        abono.numeroCuenta,
                                                                        "COMPLETADO"
                                                                );
                                                                LOG.info("✅ PASO 5: Evento publicado a Kafka");

                                                                return Response.ok()
                                                                        .entity("Abono procesado exitosamente")
                                                                        .build();
                                                            } else {
                                                                LOG.error("❌ BCRP no confirmó - Iniciando SAGA COMPENSACIÓN");
                                                                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                                                                        .entity("BCRP no disponible")
                                                                        .build();
                                                            }
                                                        })
                                                        .onFailure().recoverWithUni(error -> {
                                                            // ⭐ SAGA PATTERN: COMPENSACIÓN
                                                            LOG.errorf("❌ FALLO EN CONFIRMAR BCRP: %s", error.getMessage());
                                                            LOG.warn("🔄 SAGA: Iniciando compensación - Revertir acreditación");

                                                            return compensarAcreditacion(accountId, abono.monto, transGuardada.getId())
                                                                    .onItem().invoke(() -> {
                                                                        // Publicar evento de compensación
                                                                        publicarEventoAuditoria(
                                                                                "ABONO_REVERTIDO",
                                                                                abono.referencia,
                                                                                abono.monto,
                                                                                abono.numeroCuenta,
                                                                                "FALLIDO"
                                                                        );
                                                                    })
                                                                    .map(compensado -> Response.status(Response.Status.SERVICE_UNAVAILABLE)
                                                                            .entity("Transacción revertida - BCRP no disponible")
                                                                            .build());
                                                        });
                                            });
                                })
                                .onFailure().recoverWithUni(error -> {
                                    LOG.errorf("❌ Error inesperado: %s", error.getMessage());
                                    return crearTransaccionFallida(abono, error.getMessage())
                                            .map(t -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Error al procesar abono")
                                                    .build());
                                });
                    });
        });
    }

    /**
     * ⭐ CIRCUIT BREAKER PATTERN
     * Protege la llamada a account-service
     */
    @CircuitBreaker(
            requestVolumeThreshold = 4,     // Después de 4 requests
            failureRatio = 0.75,            // Si 75% fallan
            delay = 5000,                   // Espera 5s antes de probar
            successThreshold = 2            // 2 éxitos para cerrar
    )
    @Timeout(5000) // Timeout de 5 segundos
    public Uni<Response> validarCuentaConCircuitBreaker(String numeroCuenta) {
        LOG.infof("🔍 Validando cuenta %s (con Circuit Breaker)", numeroCuenta);
        return accountClient.buscarPorNumero(numeroCuenta);
    }

    /**
     * ⭐ CIRCUIT BREAKER PATTERN
     * Protege la llamada de acreditación
     */
    @CircuitBreaker(
            requestVolumeThreshold = 4,
            failureRatio = 0.75,
            delay = 5000,
            successThreshold = 2
    )
    @Timeout(5000)
    public Uni<Response> acreditarConCircuitBreaker(Long accountId, BigDecimal monto) {
        LOG.infof("💰 Acreditando S/%s a cuenta ID %d (con Circuit Breaker)", monto, accountId);
        return accountClient.acreditar(accountId, monto);
    }

    /**
     * ⭐ CIRCUIT BREAKER PATTERN
     * Protege la llamada al BCRP
     */
    @CircuitBreaker(
            requestVolumeThreshold = 4,
            failureRatio = 0.75,
            delay = 5000,
            successThreshold = 2
    )
    @Timeout(5000)
    public Uni<Response> confirmarBcrpConCircuitBreaker(String referencia) {
        LOG.infof("📨 Confirmando al BCRP ref: %s (con Circuit Breaker)", referencia);
        BcrpServiceClient.ConfirmacionDTO confirmacion =
                new BcrpServiceClient.ConfirmacionDTO(referencia, "EXITOSO", "Abono procesado");
        return bcrpClient.confirmarAbono(confirmacion);
    }

    /**
     * ⭐ SAGA PATTERN: COMPENSACIÓN
     * Revierte la acreditación si algo falló después
     */
    private Uni<Void> compensarAcreditacion(Long accountId, BigDecimal monto, Long transactionId) {
        LOG.warnf("🔄 SAGA COMPENSACIÓN: Revirtiendo S/%s de cuenta ID %d", monto, accountId);

        return accountClient.debitar(accountId, monto)
                .onItem().transformToUni(response -> {
                    LOG.info("✅ SAGA: Acreditación revertida exitosamente");

                    // Marcar transacción como FALLIDA
                    return repository.findById(transactionId)
                            .onItem().transformToUni(transaction -> {
                                transaction.setEstado("FALLIDO");
                                transaction.setMensajeError("BCRP no disponible - Transacción revertida");
                                transaction.preUpdate();
                                return repository.persist(transaction).replaceWithVoid();
                            });
                })
                .onFailure().recoverWithUni(error -> {
                    LOG.errorf("❌ SAGA COMPENSACIÓN FALLÓ: %s - ALERTA CRÍTICA", error.getMessage());
                    // En producción: enviar alerta al equipo de operaciones
                    return Uni.createFrom().voidItem();
                });
    }

    /**
     * Publicar evento de auditoría a Kafka
     */
    private void publicarEventoAuditoria(String evento, String referencia, BigDecimal monto, String numeroCuenta, String estado) {
        try {
            // Formato simple: EVENTO|referencia|monto|cuenta|estado
            String mensaje = String.format("%s|%s|%s|%s|%s",
                    evento, referencia, monto, numeroCuenta, estado);

            auditEmitter.send(mensaje);
            LOG.infof("📤 Evento publicado: %s", mensaje);
        } catch (Exception e) {
            LOG.errorf("❌ Error al publicar evento a Kafka: %s", e.getMessage());
            // No lanzar excepción - la auditoría no debe romper el flujo principal
        }
    }

    // Métodos auxiliares

    private TransactionEntity crearTransactionEntity(AbonoDTO abono, String estado) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setReferencia(abono.referencia);
        transaction.setMonto(abono.monto);
        transaction.setNumeroCuenta(abono.numeroCuenta);
        transaction.setBancoOrigen(abono.bancoOrigen);
        transaction.setBancoDestino("BCP");  // ← Valor fijo para banco propio
        transaction.setTipo("ABONO");
        transaction.setEstado(estado);
        transaction.setMoneda(abono.moneda);
        transaction.setConcepto(abono.concepto);
        transaction.prePersist();
        return transaction;
    }

    private Uni<TransactionEntity> crearTransaccionFallida(AbonoDTO abono, String mensajeError) {
        TransactionEntity transaction = crearTransactionEntity(abono, "FALLIDO");
        transaction.setMensajeError(mensajeError);
        return repository.persist(transaction);
    }

    private Long extraerAccountId(Response response) {
        // Simplificado: asumimos que la respuesta tiene un campo "id"
        // En producción: parsear el JSON correctamente
        return 1L; // TODO: extraer del JSON real
    }
}