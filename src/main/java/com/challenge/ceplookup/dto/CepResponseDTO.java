package com.challenge.ceplookup.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Immutable DTO representing the response from the external CEP API.
 *
 * Java 21: uses record — elimina boilerplate de getters/setters/construtores/equals/hashCode.
 * Records são implicitamente finais e imutáveis, ideal para DTOs de transporte de dados.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CepResponseDTO(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        String ibge,
        String gia,
        String ddd,
        String siafi,
        Boolean erro
) {
    /**
     * Factory para resposta "não encontrado".
     */
    public static CepResponseDTO notFound(String cep) {
        return new CepResponseDTO(cep, null, null, null, null, null, null, null, null, null, true);
    }

    /**
     * Java 21: método semântico direto no record — mais legível que checar erro != null.
     */
    public boolean found() {
        return erro == null || !erro;
    }
}
