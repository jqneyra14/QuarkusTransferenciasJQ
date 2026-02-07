package com.bcrp.microservices.gateway.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/bcrp")
@RegisterRestClient(configKey = "bcrp-service")
public interface BcrpServiceClient {

    @POST
    @Path("/simular-abono")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> simularAbono(String body);

    @GET
    @Path("/operaciones-log")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> verLog();
}