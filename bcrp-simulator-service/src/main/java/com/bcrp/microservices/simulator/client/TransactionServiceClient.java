package com.bcrp.microservices.simulator.client;

import com.bcrp.microservices.simulator.dto.AbonoDTO;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/transactions")
@RegisterRestClient(configKey = "transaction-service")
public interface TransactionServiceClient {

    /**
     * Notificar un abono a transaction-service
     * Este método simula que el BCRP notifica una transferencia entrante
     */
    @POST
    @Path("/procesar-abono")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> notificarAbono(AbonoDTO abono);
}