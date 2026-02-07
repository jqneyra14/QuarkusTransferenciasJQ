package com.bcrp.microservices.gateway.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/audit")
@RegisterRestClient(configKey = "audit-service")
public interface AuditServiceClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> listarTodos();

    @GET
    @Path("/evento/{evento}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> buscarPorEvento(@PathParam("evento") String evento);
}