package com.bcrp.microservices.gateway.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/transactions")
@RegisterRestClient(configKey = "transaction-service")
public interface TransactionServiceClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> listarTodas();

    @POST
    @Path("/procesar-abono")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> procesarAbono(String body);

    @GET
    @Path("/estado/{estado}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> listarPorEstado(@PathParam("estado") String estado);
}