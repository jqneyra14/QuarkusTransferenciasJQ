package com.bcrp.microservices.audit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String evento; // ABONO_PROCESADO, CARGO_PROCESADO, etc.

    @Column(length = 50)
    private String referencia;

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "numero_cuenta", length = 20)
    private String numeroCuenta;

    @Column(length = 20)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String detalles;

    @NotNull
    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }

    @PrePersist
    public void prePersist() {
        if (this.fechaEvento == null) {
            this.fechaEvento = LocalDateTime.now();
        }
    }
}