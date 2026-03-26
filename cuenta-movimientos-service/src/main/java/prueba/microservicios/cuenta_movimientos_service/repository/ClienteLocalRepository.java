package prueba.microservicios.cuenta_movimientos_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import prueba.microservicios.cuenta_movimientos_service.entity.ClienteLocal;

@Repository
public interface ClienteLocalRepository extends JpaRepository<ClienteLocal, String> {

    Optional<ClienteLocal> findByClienteId(String clienteId);
}

