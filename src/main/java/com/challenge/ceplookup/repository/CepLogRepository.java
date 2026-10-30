package com.challenge.ceplookup.repository;

import com.challenge.ceplookup.model.CepLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for CepLog persistence.
 * Follows the Repository pattern (DDD) and the Dependency Inversion Principle (SOLID-D).
 */
@Repository
public interface CepLogRepository extends JpaRepository<CepLog, Long> {

    List<CepLog> findByCepConsultado(String cep);

    List<CepLog> findByConsultadoEmBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT l FROM CepLog l WHERE l.cepConsultado = :cep ORDER BY l.consultadoEm DESC")
    List<CepLog> findHistoricoByCep(@Param("cep") String cep);
}
