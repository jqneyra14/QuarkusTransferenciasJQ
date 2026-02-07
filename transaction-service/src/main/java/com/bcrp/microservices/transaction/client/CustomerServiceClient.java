package com.bcrp.microservices.transaction.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/customers")
@RegisterRestClient(configKey = "customer-service")
public interface CustomerServiceClient {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> buscarPorId(@PathParam("id") Long id);
}