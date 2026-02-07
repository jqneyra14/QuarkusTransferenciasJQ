package com.bcrp.microservices.audit.resource;

import com.bcrp.microservices.audit.entity.AuditEntity;
import com.bcrp.microservices.audit.repository.AuditRepository;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/audit")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Audit", description = "Consulta de logs de auditoría")
public class AuditResource {

    @Inject
    AuditRepository repository;

    @GET
    @Operation(summary = "Listar todos los logs de auditoría")
    public Uni<List<AuditEntity>> listarTodos() {
        return repository.listAll();
    }

    @GET
    @Path("/evento/{evento}")
    @Operation(summary = "Buscar logs por tipo de evento")
    public Uni<List<AuditEntity>> buscarPorEvento(@PathParam("evento") String evento) {
        return repository.findByEvento(evento);
    }

    @GET
    @Path("/referencia/{referencia}")
    @Operation(summary = "Buscar logs por referencia de transacción")
    public Uni<List<AuditEntity>> buscarPorReferencia(@PathParam("referencia") String referencia) {
        return repository.findByReferencia(referencia);
    }

    @GET
    @Path("/cuenta/{numeroCuenta}")
    @Operation(summary = "Buscar logs por número de cuenta")
    public Uni<List<AuditEntity>> buscarPorCuenta(@PathParam("numeroCuenta") String numeroCuenta) {
        return repository.findByNumeroCuenta(numeroCuenta);
    }
}