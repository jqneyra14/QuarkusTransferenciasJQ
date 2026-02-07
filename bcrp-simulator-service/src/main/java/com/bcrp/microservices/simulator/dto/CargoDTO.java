package com.bcrp.microservices.simulator.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CargoDTO {

    @NotBlank
    public String numeroCuentaOrigen;

    @NotBlank
    public String numeroCuentaDestino;

    @NotNull
    @DecimalMin(value = "0.01")
    public BigDecimal monto;

    @NotBlank
    public String bancoDestino;

    @NotBlank
    public String referencia;

    public String moneda = "PEN";

    public LocalDateTime fechaOperacion = LocalDateTime.now();

    public String concepto;

    public CargoDTO() {}
}