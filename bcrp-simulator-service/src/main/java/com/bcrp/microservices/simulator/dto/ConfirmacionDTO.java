package com.bcrp.microservices.simulator.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ConfirmacionDTO {

    @NotBlank
    public String referencia;

    @NotBlank
    public String estado; // EXITOSO, FALLIDO, PENDIENTE

    public LocalDateTime fechaProceso = LocalDateTime.now();

    public String mensaje;

    public ConfirmacionDTO() {}

    public ConfirmacionDTO(String referencia, String estado, String mensaje) {
        this.referencia = referencia;
        this.estado = estado;
        this.mensaje = mensaje;
    }
}