package com.bcrp.microservices.gateway.resource;

import com.bcrp.microservices.gateway.client.*;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "API Gateway", description = "Punto único de entrada al sistema")
public class GatewayResource {

    private static final Logger LOG = Logger.getLogger(GatewayResource.class);

    @Inject @RestClient CustomerServiceClient customerClient;
    @Inject @RestClient AccountServiceClient accountClient;
    @Inject @RestClient TransactionServiceClient transactionClient;
    @Inject @RestClient BcrpServiceClient bcrpClient;
    @Inject @RestClient AuditServiceClient auditClient;

    // ========== CUSTOMER ROUTES ==========

    @GET
    @Path("/customers")
    @Operation(summary = "Listar todos los clientes")
    public Uni<Response> listarClientes() {
        LOG.info("🚪 Gateway: Redirigiendo a customer-service");
        return customerClient.listarTodos();
    }

    @GET
    @Path("/customers/{id}")
    @Operation(summary = "Obtener cliente por ID")
    public Uni<Response> obtenerCliente(@PathParam("id") Long id) {
        LOG.infof("🚪 Gateway: Obteniendo cliente %d", id);
        return customerClient.obtenerPorId(id);
    }

    @POST
    @Path("/customers")
    @Operation(summary = "Crear nuevo cliente")
    public Uni<Response> crearCliente(String body) {
        LOG.info("🚪 Gateway: Creando cliente");
        return customerClient.crear(body);
    }

    @PUT
    @Path("/customers/{id}")
    @Operation(summary = "Actualizar cliente")
    public Uni<Response> actualizarCliente(@PathParam("id") Long id, String body) {
        LOG.infof("🚪 Gateway: Actualizando cliente %d", id);
        return customerClient.actualizar(id, body);
    }

    @DELETE
    @Path("/customers/{id}")
    @Operation(summary = "Eliminar cliente")
    public Uni<Response> eliminarCliente(@PathParam("id") Long id) {
        LOG.infof("🚪 Gateway: Eliminando cliente %d", id);
        return customerClient.eliminar(id);
    }

    // ========== ACCOUNT ROUTES ==========

    @GET
    @Path("/accounts")
    @Operation(summary = "Listar todas las cuentas")
    public Uni<Response> listarCuentas() {
        LOG.info("🚪 Gateway: Redirigiendo a account-service");
        return accountClient.listarTodos();
    }

    @GET
    @Path("/accounts/{id}")
    @Operation(summary = "Obtener cuenta por ID")
    public Uni<Response> obtenerCuenta(@PathParam("id") Long id) {
        LOG.infof("🚪 Gateway: Obteniendo cuenta %d", id);
        return accountClient.obtenerPorId(id);
    }

    @GET
    @Path("/accounts/numero/{numero}")
    @Operation(summary = "Buscar cuenta por número")
    public Uni<Response> buscarCuentaPorNumero(@PathParam("numero") String numero) {
        LOG.infof("🚪 Gateway: Buscando cuenta %s", numero);
        return accountClient.buscarPorNumero(numero);
    }

    @POST
    @Path("/accounts")
    @Operation(summary = "Crear nueva cuenta")
    public Uni<Response> crearCuenta(String body) {
        LOG.info("🚪 Gateway: Creando cuenta");
        return accountClient.crear(body);
    }

    @POST
    @Path("/accounts/{id}/acreditar")
    @Operation(summary = "Acreditar dinero a cuenta")
    public Uni<Response> acreditarCuenta(
            @PathParam("id") Long id,
            @QueryParam("monto") BigDecimal monto) {
        LOG.infof("🚪 Gateway: Acreditando S/%s a cuenta %d", monto, id);
        return accountClient.acreditar(id, monto);
    }

    @POST
    @Path("/accounts/{id}/debitar")
    @Operation(summary = "Debitar dinero de cuenta")
    public Uni<Response> debitarCuenta(
            @PathParam("id") Long id,
            @QueryParam("monto") BigDecimal monto) {
        LOG.infof("🚪 Gateway: Debitando S/%s de cuenta %d", monto, id);
        return accountClient.debitar(id, monto);
    }

    // ========== TRANSACTION ROUTES ==========

    @GET
    @Path("/transactions")
    @Operation(summary = "Listar todas las transacciones")
    public Uni<Response> listarTransacciones() {
        LOG.info("🚪 Gateway: Redirigiendo a transaction-service");
        return transactionClient.listarTodas();
    }

    @POST
    @Path("/transactions/procesar-abono")
    @Operation(summary = "Procesar abono interbancario")
    public Uni<Response> procesarAbono(String body) {
        LOG.info("🚪 Gateway: Procesando abono");
        return transactionClient.procesarAbono(body);
    }

    @GET
    @Path("/transactions/estado/{estado}")
    @Operation(summary = "Listar transacciones por estado")
    public Uni<Response> listarTransaccionesPorEstado(@PathParam("estado") String estado) {
        LOG.infof("🚪 Gateway: Listando transacciones con estado %s", estado);
        return transactionClient.listarPorEstado(estado);
    }

    // ========== BCRP ROUTES ==========

    @POST
    @Path("/bcrp/simular-abono")
    @Operation(summary = "Simular abono desde BCRP")
    public Uni<Response> simularAbono(String body) {
        LOG.info("🚪 Gateway: Simulando abono BCRP");
        return bcrpClient.simularAbono(body);
    }

    @GET
    @Path("/bcrp/operaciones-log")
    @Operation(summary = "Ver log de operaciones BCRP")
    public Uni<Response> verLogBcrp() {
        LOG.info("🚪 Gateway: Consultando log BCRP");
        return bcrpClient.verLog();
    }

    // ========== AUDIT ROUTES ==========

    @GET
    @Path("/audit")
    @Operation(summary = "Listar todos los logs de auditoría")
    public Uni<Response> listarAuditoria() {
        LOG.info("🚪 Gateway: Consultando auditoría");
        return auditClient.listarTodos();
    }

    @GET
    @Path("/audit/evento/{evento}")
    @Operation(summary = "Buscar logs por evento")
    public Uni<Response> buscarAuditoriaPorEvento(@PathParam("evento") String evento) {
        LOG.infof("🚪 Gateway: Buscando auditoría por evento %s", evento);
        return auditClient.buscarPorEvento(evento);
    }

    // ========== HEALTH CHECK ==========

    @GET
    @Path("/health")
    @Operation(summary = "Health check del gateway")
    public Uni<Response> health() {
        return Uni.createFrom().item(
                Response.ok()
                        .entity("{\"status\": \"UP\", \"service\": \"api-gateway\"}")
                        .build()
        );
    }
}