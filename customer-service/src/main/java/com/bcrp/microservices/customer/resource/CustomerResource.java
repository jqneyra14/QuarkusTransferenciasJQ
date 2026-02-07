package com.bcrp.microservices.customer.resource;

import com.bcrp.microservices.customer.dto.CustomerDTO;
import com.bcrp.microservices.customer.entity.CustomerEntity;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customer", description = "Gestión de clientes")
public class CustomerResource {

    @GET
    @Operation(summary = "Listar todos los clientes")
    public Uni<List<CustomerDTO>> listarTodos() {
        return CustomerEntity.<CustomerEntity>listAll()
                .map(customers -> customers.stream()
                        .map(CustomerDTO::new)
                        .collect(Collectors.toList()));
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    public Uni<Response> obtenerPorId(@PathParam("id") Long id) {
        return CustomerEntity.<CustomerEntity>findById(id)
                .onItem().ifNotNull().transform(customer ->
                        Response.ok(new CustomerDTO(customer)).build())
                .onItem().ifNull().continueWith(
                        Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Operation(summary = "Crear un nuevo cliente")
    public Uni<Response> crear(@Valid CustomerDTO dto) {
        return Panache.withTransaction(() -> {
            return CustomerEntity.findByDni(dto.dni)
                    .onItem().transformToUni(existing -> {
                        if (existing != null) {
                            return Uni.createFrom().item(
                                    Response.status(Response.Status.CONFLICT)
                                            .entity("Ya existe cliente con DNI: " + dto.dni)
                                            .build()
                            );
                        }

                        CustomerEntity customer = dto.toEntity();
                        customer.prePersist();

                        return customer.persist()
                                .map(c -> Response.status(Response.Status.CREATED)
                                        .entity(new CustomerDTO((CustomerEntity) c))
                                        .build());
                    });
        });
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar cliente")
    public Uni<Response> actualizar(@PathParam("id") Long id, @Valid CustomerDTO dto) {
        return Panache.withTransaction(() ->
                CustomerEntity.<CustomerEntity>findById(id)
                        .onItem().ifNotNull().transformToUni(customer -> {
                            customer.nombre = dto.nombre;
                            customer.apellido = dto.apellido;
                            customer.dni = dto.dni;
                            customer.email = dto.email;
                            customer.telefono = dto.telefono;
                            customer.direccion = dto.direccion;
                            customer.activo = dto.activo;
                            customer.preUpdate();

                            return customer.persist()
                                    .map(c -> Response.ok(new CustomerDTO((CustomerEntity) c)).build());
                        })
                        .onItem().ifNull().continueWith(
                                Response.status(Response.Status.NOT_FOUND).build())
        );
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar cliente (soft delete)")
    public Uni<Response> eliminar(@PathParam("id") Long id) {
        return Panache.withTransaction(() ->
                CustomerEntity.<CustomerEntity>findById(id)
                        .onItem().ifNotNull().transformToUni(customer -> {
                            customer.activo = false;
                            customer.preUpdate();
                            return customer.persist()
                                    .map(c -> Response.noContent().build());
                        })
                        .onItem().ifNull().continueWith(
                                Response.status(Response.Status.NOT_FOUND).build())
        );
    }
}