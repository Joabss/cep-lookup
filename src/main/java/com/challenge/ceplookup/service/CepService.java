package com.challenge.ceplookup.service;

import com.challenge.ceplookup.dto.CepResponseDTO;
import com.challenge.ceplookup.model.CepLog;

import java.util.List;

/**
 * Main use-case interface for CEP lookup.
 * Controllers depend only on this interface (DIP).
 */
public interface CepService {

    CepResponseDTO buscarCep(String cep);

    List<CepLog> buscarHistorico(String cep);
}
