package com.bcrp.microservices.audit.consumer;

import com.bcrp.microservices.audit.entity.AuditEntity;
import com.bcrp.microservices.audit.repository.AuditRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class AuditEventConsumer {

    private static final Logger LOG = Logger.getLogger(AuditEventConsumer.class);

    @Inject
    AuditRepository repository;

    /**
     * Consumir eventos del topic "audit-events" de Kafka
     * Se ejecuta automáticamente cuando llega un mensaje
     */
    @Incoming("audit-events")
    public Uni<Void> consumirEvento(String mensaje) {
        LOG.infof("📨 Evento recibido de Kafka: %s", mensaje);

        return Panache.withTransaction(() -> {
                    // Parsear el mensaje (formato simple: "EVENTO|referencia|monto|cuenta")
                    String[] partes = mensaje.split("\\|");

                    AuditEntity audit = new AuditEntity();
                    audit.setEvento(partes.length > 0 ? partes[0] : "DESCONOCIDO");
                    audit.setReferencia(partes.length > 1 ? partes[1] : null);
                    audit.setDetalles(mensaje);
                    audit.setFechaEvento(LocalDateTime.now());

                    return repository.persist(audit)
                            .onItem().invoke(() -> LOG.info("✅ Evento de auditoría guardado en BD"))
                            .replaceWithVoid();
                })
                .onFailure().invoke(error ->
                        LOG.errorf("❌ Error al guardar auditoría: %s", error.getMessage())
                );
    }
}