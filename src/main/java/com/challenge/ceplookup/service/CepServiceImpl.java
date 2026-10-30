package com.challenge.ceplookup.service;

import com.challenge.ceplookup.client.CepClient;
import com.challenge.ceplookup.dto.CepResponseDTO;
import com.challenge.ceplookup.exception.CepNotFoundException;
import com.challenge.ceplookup.model.CepLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the CEP lookup flow:
 *  1. Calls the external API (via CepClient abstraction)
 *  2. Persists the log (via CepLogService abstraction)
 *  3. Returns the result or throws a domain exception
 *
 * SOLID principles applied:
 *  - SRP: only orchestrates; delegates HTTP and persistence to collaborators.
 *  - OCP: new behaviours (e.g. caching) can wrap this without modifying it.
 *  - LSP: any CepClient implementation (real API or mock) is substitutable.
 *  - DIP: depends on interfaces, not concrete classes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CepServiceImpl implements CepService {

    private final CepClient cepClient;
    private final CepLogService cepLogService;

    @Override
    public CepResponseDTO buscarCep(String cep) {
        String cleanCep = sanitizeCep(cep);
        log.info("Starting lookup for CEP: {}", cleanCep);

        var response = cepClient.buscarCep(cleanCep);

        // Sempre grava o log, inclusive em caso de não encontrado
        cepLogService.registrar(cleanCep, response);

        // Java 21: usa método semântico do record
        if (!response.found()) {
            throw new CepNotFoundException("CEP não encontrado: " + cleanCep);
        }

        return response;
    }

    @Override
    public List<CepLog> buscarHistorico(String cep) {
        return cepLogService.buscarHistorico(sanitizeCep(cep));
    }

    private String sanitizeCep(String cep) {
        return cep.replaceAll("[^0-9]", "");
    }
}
