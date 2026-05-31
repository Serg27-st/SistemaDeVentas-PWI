package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "direcciones_envio")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String alias;

    @Column(nullable = false, length = 300)
    private String direccionCompleta;

    @Column(length = 150)
    private String referencia;

    @Column(length = 100)
    private String distrito;

    @Column(nullable = false)
    @Builder.Default
    private boolean esDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_web_id", nullable = false)
    private ClienteWeb clienteWeb;
}
