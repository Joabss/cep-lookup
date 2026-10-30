package com.challenge.ceplookup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity que registra cada consulta de CEP realizada.
 *
 * Spring Boot 3 / Java 21: usa jakarta.persistence (não mais javax.persistence).
 * Lombok mantido para @Entity — records não são compatíveis com JPA por exigirem mutabilidade.
 */
@Entity
@Table(name = "cep_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CepLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cep_consultado", nullable = false, length = 8)
    private String cepConsultado;

    @Column(name = "consultado_em", nullable = false)
    private LocalDateTime consultadoEm;

    @Column(name = "logradouro")
    private String logradouro;

    @Column(name = "complemento")
    private String complemento;

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "localidade")
    private String localidade;

    @Column(name = "uf", length = 2)
    private String uf;

    @Column(name = "ibge")
    private String ibge;

    @Column(name = "ddd", length = 3)
    private String ddd;

    @Column(name = "status_consulta", nullable = false, length = 20)
    private String statusConsulta; // SUCCESS ou NOT_FOUND

    /**
     * Payload JSON completo para auditoria total.
     */
    @Column(name = "payload_retorno", columnDefinition = "TEXT")
    private String payloadRetorno;
}
