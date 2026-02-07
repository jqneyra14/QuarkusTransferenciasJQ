package com.bcrp.microservices.transaction.resource;

import com.bcrp.microservices.transaction.dto.*;
import com.bcrp.microservices.transaction.repository.TransactionRepository;
import com.bcrp.microservices.transaction.service.TransactionOrchestrator;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Transaction", description = "Gestión de transferencias interbancarias")
public class TransactionResource {

    @Inject
    TransactionOrchestrator orchestrator;

    @Inject
    TransactionRepository repository;

    /**
     * Endpoint para recibir notificaciones de abono desde BCRP
     * Este endpoint es llamado por bcrp-simulator-service
     */
    @POST
    @Path("/procesar-abono")
    @Operation(summary = "Procesar abono notificado por BCRP")
    public Uni<Response> procesarAbono(@Valid AbonoDTO abono) {
        return orchestrator.procesarAbono(abono);
    }

    /**
     * ⭐ API COMPOSITE PATTERN
     * Obtener detalle completo de una transacción (transacción + cuenta + cliente)
     */
    @GET
    @Path("/{referencia}/detalle-completo")
    @Operation(summary = "Obtener detalle completo de transacción (API Composite)")
    public Uni<Response> obtenerDetalleCompleto(@PathParam("referencia") String referencia) {
        // TODO: Implementar llamadas paralelas a account-service y customer-service
        // Por ahora solo devuelve la transacción
        return repository.findByReferencia(referencia)
                .onItem().ifNotNull().transform(trans ->
                        Response.ok(new TransactionDTO(trans)).build())
                .onItem().ifNull().continueWith(
                        Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Operation(summary = "Listar todas las transacciones")
    public Uni<List<TransactionDTO>> listarTodas() {
        return repository.listAll()
                .map(transactions -> transactions.stream()
                        .map(TransactionDTO::new)
                        .collect(Collectors.toList()));
    }

    @GET
    @Path("/estado/{estado}")
    @Operation(summary = "Listar transacciones por estado")
    public Uni<List<TransactionDTO>> listarPorEstado(@PathParam("estado") String estado) {
        return repository.findByEstado(estado)
                .map(transactions -> transactions.stream()
                        .map(TransactionDTO::new)
                        .collect(Collectors.toList()));
    }

    @GET
    @Path("/cuenta/{numeroCuenta}")
    @Operation(summary = "Listar transacciones de una cuenta")
    public Uni<List<TransactionDTO>> listarPorCuenta(@PathParam("numeroCuenta") String numeroCuenta) {
        return repository.findByNumeroCuenta(numeroCuenta)
                .map(transactions -> transactions.stream()
                        .map(TransactionDTO::new)
                        .collect(Collectors.toList()));
    }
}