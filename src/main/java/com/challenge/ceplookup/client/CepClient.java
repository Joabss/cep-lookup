package com.challenge.ceplookup.client;

import com.challenge.ceplookup.dto.CepResponseDTO;

/**
 * Interface for external CEP lookup clients.
 *
 * Applies SOLID principles:
 *  - ISP (Interface Segregation): focuses only on CEP fetching, nothing else.
 *  - DIP (Dependency Inversion): service depends on this abstraction, not on a concrete HTTP client.
 *
 * This makes it trivial to swap WireMock (tests) for ViaCEP (prod) without touching business logic.
 */
public interface CepClient {

    /**
     * Fetches address data for the given CEP.
     *
     * @param cep 8-digit Brazilian postal code (digits only, no hyphen)
     * @return populated DTO or a DTO with erro=true if not found
     */
    CepResponseDTO buscarCep(String cep);
}
