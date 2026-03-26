package prueba.microservicios.cuenta_movimientos_service.service;

import java.util.List;

import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoResponseDTO;

public interface MovimientoService {

    MovimientoResponseDTO registrarMovimiento(MovimientoDTO dto);

    List<MovimientoResponseDTO> listarTodos();

    MovimientoResponseDTO obtenerPorId(Long id);

    void eliminar(Long id);
}
