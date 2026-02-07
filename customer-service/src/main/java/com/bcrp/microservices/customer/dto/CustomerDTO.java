package com.bcrp.microservices.customer.dto;


import com.bcrp.microservices.customer.entity.CustomerEntity;
import jakarta.validation.constraints.*;

public class CustomerDTO {

    public Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100)
    public String nombre;

    @NotBlank(message = "El apellido es requerido")
    @Size(min = 2, max = 100)
    public String apellido;

    @NotBlank(message = "El DNI es requerido")
    @Size(min = 8, max = 8)
    public String dni;

    @Email
    public String email;

    @Size(min = 9, max = 15)
    public String telefono;

    public String direccion;
    public Boolean activo;

    public CustomerDTO() {}

    public CustomerDTO(CustomerEntity customer) {
        this.id = customer.id;
        this.nombre = customer.nombre;
        this.apellido = customer.apellido;
        this.dni = customer.dni;
        this.email = customer.email;
        this.telefono = customer.telefono;
        this.direccion = customer.direccion;
        this.activo = customer.activo;
    }

    public CustomerEntity toEntity() {
        CustomerEntity customer = new CustomerEntity();
        customer.id = this.id;
        customer.nombre = this.nombre;
        customer.apellido = this.apellido;
        customer.dni = this.dni;
        customer.email = this.email;
        customer.telefono = this.telefono;
        customer.direccion = this.direccion;
        customer.activo = this.activo != null ? this.activo : true;
        return customer;
    }
}