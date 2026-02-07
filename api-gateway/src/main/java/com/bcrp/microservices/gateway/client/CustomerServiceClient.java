package com.bcrp.microservices.gateway.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/customers")
@RegisterRestClient(configKey = "customer-service")
public interface CustomerServiceClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> listarTodos();

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> obtenerPorId(@PathParam("id") Long id);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> crear(String body);

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> actualizar(@PathParam("id") Long id, String body);

    @DELETE
    @Path("/{id}")
    Uni<Response> eliminar(@PathParam("id") Long id);
}