package com.bcrp.microservices.simulator.resource;

import com.bcrp.microservices.simulator.dto.*;
import com.bcrp.microservices.simulator.client.TransactionServiceClient;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/api/bcrp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "BCRP Simulator", description = "Simulador del Banco Central de Reserva del Perú")
public class BcrpSimulatorResource {

    private static final Logger LOG = Logger.getLogger(BcrpSimulatorResource.class);

    @Inject
    @RestClient
    TransactionServiceClient transactionServiceClient;

    // Lista en memoria para registrar operaciones (opcional - solo para demo)
    private final List<String> operacionesLog = new ArrayList<>();

    /**
     * Endpoint para simular que el BCRP envía una notificación de abono
     * TÚ llamas esto desde Postman/cURL para simular transferencias entrantes
     */
    @POST
    @Path("/simular-abono")
    @Operation(summary = "Simular notificación de abono desde BCRP")
    public Uni<Response> simularAbono(@Valid AbonoDTO abono) {
        LOG.infof("🏛️ BCRP Simulator: Simulando abono de %s desde %s a cuenta %s",
                abono.monto, abono.bancoOrigen, abono.numeroCuenta);

        // Registrar en log
        operacionesLog.add(String.format("[%s] ABONO: %s - %s - Ref: %s",
                LocalDateTime.now(), abono.monto, abono.numeroCuenta, abono.referencia));

        // Llamar a transaction-service para notificar el abono
        return transactionServiceClient.notificarAbono(abono)
                .onItem().transform(response -> {
                    LOG.info("✅ Notificación enviada a transaction-service");
                    return Response.ok()
                            .entity(new MensajeResponse(
                                    "Abono simulado y notificado a transaction-service",
                                    abono.referencia,
                                    "NOTIFICADO"
                            ))
                            .build();
                })
                .onFailure().recoverWithItem(error -> {
                    LOG.errorf("❌ Error al notificar a transaction-service: %s", error.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new MensajeResponse(
                                    "Error al notificar abono: " + error.getMessage(),
                                    abono.referencia,
                                    "ERROR"
                            ))
                            .build();
                });
    }

    /**
     * Endpoint para recibir confirmaciones de transaction-service
     * transaction-service llama esto después de procesar el abono
     */
    @POST
    @Path("/confirmar-abono")
    @Operation(summary = "Recibir confirmación de abono procesado")
    public Uni<Response> confirmarAbono(@Valid ConfirmacionDTO confirmacion) {
        LOG.infof("📩 BCRP Simulator: Confirmación recibida - Ref: %s, Estado: %s",
                confirmacion.referencia, confirmacion.estado);

        // Registrar confirmación
        operacionesLog.add(String.format("[%s] CONFIRMACIÓN: %s - %s - %s",
                LocalDateTime.now(), confirmacion.referencia, confirmacion.estado, confirmacion.mensaje));

        return Uni.createFrom().item(
                Response.ok()
                        .entity(new MensajeResponse(
                                "Confirmación recibida por BCRP",
                                confirmacion.referencia,
                                "RECIBIDO"
                        ))
                        .build()
        );
    }

    /**
     * Endpoint para recibir cargos (transferencias salientes)
     * transaction-service llama esto cuando tu banco envía dinero a otro banco
     */
    @POST
    @Path("/procesar-cargo")
    @Operation(summary = "Recibir cargo desde un banco")
    public Uni<Response> procesarCargo(@Valid CargoDTO cargo) {
        LOG.infof("💸 BCRP Simulator: Cargo recibido de cuenta %s a %s por %s",
                cargo.numeroCuentaOrigen, cargo.numeroCuentaDestino, cargo.monto);

        // Registrar cargo
        operacionesLog.add(String.format("[%s] CARGO: %s → %s - %s - Ref: %s",
                LocalDateTime.now(), cargo.numeroCuentaOrigen, cargo.numeroCuentaDestino,
                cargo.monto, cargo.referencia));

        // Simular proceso exitoso (en la realidad, BCRP procesaría el cargo)
        return Uni.createFrom().item(
                Response.ok()
                        .entity(new ConfirmacionDTO(
                                cargo.referencia,
                                "PROCESADO",
                                "Cargo procesado exitosamente por BCRP"
                        ))
                        .build()
        );
    }

    /**
     * Endpoint para ver el log de operaciones simuladas
     */
    @GET
    @Path("/operaciones-log")
    @Operation(summary = "Ver historial de operaciones simuladas")
    public Uni<Response> verLog() {
        return Uni.createFrom().item(
                Response.ok(operacionesLog).build()
        );
    }

    /**
     * Health check
     */
    @GET
    @Path("/health")
    @Operation(summary = "Verificar estado del simulador")
    public Uni<Response> health() {
        return Uni.createFrom().item(
                Response.ok()
                        .entity(new MensajeResponse(
                                "BCRP Simulator operativo",
                                null,
                                "ACTIVO"
                        ))
                        .build()
        );
    }

    // Clase interna para respuestas
    public static class MensajeResponse {
        public String mensaje;
        public String referencia;
        public String estado;
        public LocalDateTime timestamp;

        public MensajeResponse(String mensaje, String referencia, String estado) {
            this.mensaje = mensaje;
            this.referencia = referencia;
            this.estado = estado;
            this.timestamp = LocalDateTime.now();
        }
    }
}