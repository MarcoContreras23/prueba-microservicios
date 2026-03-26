package prueba.microservicios.cuenta_movimientos_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import prueba.microservicios.cuenta_movimientos_service.entity.Movimiento;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuentaNumeroCuentaOrderByFechaDesc(String numeroCuenta);

    Optional<Movimiento> findTopByCuentaNumeroCuentaOrderByFechaDesc(String numeroCuenta);

    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.numeroCuenta = :numeroCuenta " +
            "AND m.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fecha ASC")
    List<Movimiento> findByCuentaAndFechaBetween(
            @Param("numeroCuenta") String numeroCuenta,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.clienteId = :clienteId " +
            "AND m.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fecha ASC")
    List<Movimiento> findByClienteIdAndFechaBetween(
            @Param("clienteId") String clienteId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}
