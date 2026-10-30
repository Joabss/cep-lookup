package com.challenge.ceplookup.service;

import com.challenge.ceplookup.dto.CepResponseDTO;
import com.challenge.ceplookup.model.CepLog;

import java.util.List;

/**
 * Contract for CEP lookup log persistence.
 * Segregated from CepService to respect ISP (Interface Segregation Principle).
 */
public interface CepLogService {

    CepLog registrar(String cep, CepResponseDTO response);

    List<CepLog> buscarHistorico(String cep);
}
