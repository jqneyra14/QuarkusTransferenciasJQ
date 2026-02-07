package com.bcrp.microservices.transaction.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/bcrp")
@RegisterRestClient(configKey = "bcrp-service")
public interface BcrpServiceClient {

    @POST
    @Path("/confirmar-abono")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> confirmarAbono(ConfirmacionDTO confirmacion);

    // DTO interno
    class ConfirmacionDTO {
        public String referencia;
        public String estado;
        public String mensaje;

        public ConfirmacionDTO(String referencia, String estado, String mensaje) {
            this.referencia = referencia;
            this.estado = estado;
            this.mensaje = mensaje;
        }
    }
}