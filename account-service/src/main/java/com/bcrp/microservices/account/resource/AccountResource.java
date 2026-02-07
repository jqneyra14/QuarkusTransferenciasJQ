package com.bcrp.microservices.account.resource;


import com.bcrp.microservices.account.dto.AccountDTO;
import com.bcrp.microservices.account.entity.AccountEntity;
import com.bcrp.microservices.account.repository.AccountRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Account", description = "Gestión de cuentas bancarias")
public class AccountResource {

    @Inject
    AccountRepository repository;

    @GET
    @Operation(summary = "Listar todas las cuentas activas")
    public Uni<List<AccountDTO>> listarTodas() {
        return repository.findActiveAccounts()
                .map(accounts -> accounts.stream()
                        .map(AccountDTO::new)
                        .collect(Collectors.toList()));
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener cuenta por ID")
    public Uni<Response> obtenerPorId(@PathParam("id") Long id) {
        return repository.findById(id)
                .onItem().ifNotNull().transform(account ->
                        Response.ok(new AccountDTO(account)).build())
                .onItem().ifNull().continueWith(
                        Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/numero/{numero}")
    @Operation(summary = "Buscar cuenta por número")
    public Uni<Response> buscarPorNumero(@PathParam("numero") String numero) {
        return repository.findByNumero(numero)
                .onItem().ifNotNull().transform(account ->
                        Response.ok(new AccountDTO(account)).build())
                .onItem().ifNull().continueWith(
                        Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/customer/{customerId}")
    @Operation(summary = "Listar cuentas por cliente")
    public Uni<List<AccountDTO>> listarPorCliente(@PathParam("customerId") Long customerId) {
        return repository.findByCustomerId(customerId)
                .map(accounts -> accounts.stream()
                        .map(AccountDTO::new)
                        .collect(Collectors.toList()));
    }

    @POST
    @Operation(summary = "Crear nueva cuenta")
    public Uni<Response> crear(@Valid AccountDTO dto) {
        return Panache.withTransaction(() -> {
            return repository.findByNumero(dto.numero)
                    .onItem().transformToUni(existing -> {
                        if (existing != null) {
                            return Uni.createFrom().item(
                                    Response.status(Response.Status.CONFLICT)
                                            .entity("Ya existe cuenta con número: " + dto.numero)
                                            .build()
                            );
                        }

                        AccountEntity account = dto.toEntity();
                        account.prePersist();

                        return repository.persist(account)
                                .map(a -> Response.status(Response.Status.CREATED)
                                        .entity(new AccountDTO(a))
                                        .build());
                    });
        });
    }

    @POST
    @Path("/{id}/acreditar")
    @Operation(summary = "Acreditar dinero a una cuenta")
    public Uni<Response> acreditar(
            @PathParam("id") Long id,
            @QueryParam("monto") BigDecimal monto) {

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("El monto debe ser mayor a 0")
                            .build()
            );
        }

        return Panache.withTransaction(() ->
                repository.acreditar(id, monto)
                        .map(success -> success
                                ? Response.ok().entity("Acreditación exitosa").build()
                                : Response.status(Response.Status.NOT_FOUND).build())
        );
    }

    @POST
    @Path("/{id}/debitar")
    @Operation(summary = "Debitar dinero de una cuenta")
    public Uni<Response> debitar(
            @PathParam("id") Long id,
            @QueryParam("monto") BigDecimal monto) {

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("El monto debe ser mayor a 0")
                            .build()
            );
        }

        return Panache.withTransaction(() ->
                repository.debitar(id, monto)
                        .map(success -> success
                                ? Response.ok().entity("Débito exitoso").build()
                                : Response.status(Response.Status.BAD_REQUEST)
                                .entity("Saldo insuficiente o cuenta no encontrada")
                                .build())
        );
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar cuenta (soft delete)")
    public Uni<Response> eliminar(@PathParam("id") Long id) {
        return Panache.withTransaction(() ->
                repository.findById(id)
                        .onItem().ifNotNull().transformToUni(account -> {
                            account.setActivo(false);
                            account.preUpdate();
                            return repository.persist(account)
                                    .map(a -> Response.noContent().build());
                        })
                        .onItem().ifNull().continueWith(
                                Response.status(Response.Status.NOT_FOUND).build())
        );
    }
}