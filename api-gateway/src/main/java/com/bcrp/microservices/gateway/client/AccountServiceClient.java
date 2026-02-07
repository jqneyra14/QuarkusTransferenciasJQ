package com.bcrp.microservices.gateway.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.math.BigDecimal;

@Path("/api/accounts")
@RegisterRestClient(configKey = "account-service")
public interface AccountServiceClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> listarTodos();

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> obtenerPorId(@PathParam("id") Long id);

    @GET
    @Path("/numero/{numero}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> buscarPorNumero(@PathParam("numero") String numero);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> crear(String body);

    @POST
    @Path("/{id}/acreditar")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> acreditar(@PathParam("id") Long id, @QueryParam("monto") BigDecimal monto);

    @POST
    @Path("/{id}/debitar")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> debitar(@PathParam("id") Long id, @QueryParam("monto") BigDecimal monto);
}