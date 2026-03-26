package prueba.microservicios.cuenta_movimientos_service.service;

import java.util.List;

import prueba.microservicios.cuenta_movimientos_service.dto.CuentaDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.CuentaResponseDTO;

public interface CuentaService {

    CuentaResponseDTO crear(CuentaDTO dto);

    List<CuentaResponseDTO> listarTodas();

    CuentaResponseDTO obtenerPorNumeroCuenta(String numeroCuenta);

    CuentaResponseDTO actualizar(String numeroCuenta, CuentaDTO dto);

    void eliminar(String numeroCuenta);
}
