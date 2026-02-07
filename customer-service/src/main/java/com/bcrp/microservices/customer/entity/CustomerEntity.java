package com.bcrp.microservices.customer.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customers")
public class CustomerEntity extends PanacheEntity {
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    public String nombre;

    @NotBlank(message = "El apellido es requerido")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    public String apellido;

    @NotBlank(message = "El DNI es requerido")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    @Column(nullable = false, unique = true, length = 8)
    public String dni;

    @Email(message = "Debe ser un email válido")
    @Column(unique = true)
    public String email;

    @Size(min = 9, max = 15)
    @Column(length = 15)
    public String telefono;

    @Column(length = 200)
    public String direccion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    public LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    public LocalDateTime fechaActualizacion;

    @Column(nullable = false)
    public Boolean activo = true;


    // ========== MÉTODOS ACTIVE RECORD ==========

    public static Uni<CustomerEntity> findByDni(String dni) {
        return find("dni", dni).firstResult();
    }

    public static Uni<CustomerEntity> findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static Uni<List<CustomerEntity>> findActiveCustomers() {
        return list("activo", true);
    }

    public static Uni<List<CustomerEntity>> findByNombre(String nombre) {
        return list("LOWER(nombre) LIKE LOWER(?1)", "%" + nombre + "%");
    }

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
