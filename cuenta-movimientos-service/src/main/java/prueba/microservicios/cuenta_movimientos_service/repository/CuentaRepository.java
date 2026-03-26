package prueba.microservicios.cuenta_movimientos_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import prueba.microservicios.cuenta_movimientos_service.entity.Cuenta;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, String> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByClienteId(String clienteId);

    boolean existsByNumeroCuenta(String numeroCuenta);
}