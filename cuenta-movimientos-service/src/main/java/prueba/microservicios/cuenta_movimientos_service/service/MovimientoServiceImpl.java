package prueba.microservicios.cuenta_movimientos_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoResponseDTO;
import prueba.microservicios.cuenta_movimientos_service.entity.Cuenta;
import prueba.microservicios.cuenta_movimientos_service.entity.Movimiento;
import prueba.microservicios.cuenta_movimientos_service.exception.ResourceNotFoundException;
import prueba.microservicios.cuenta_movimientos_service.exception.SaldoNoDisponibleException;
import prueba.microservicios.cuenta_movimientos_service.repository.CuentaRepository;
import prueba.microservicios.cuenta_movimientos_service.repository.MovimientoRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    @Transactional
    public MovimientoResponseDTO registrarMovimiento(MovimientoDTO dto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuenta())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + dto.getNumeroCuenta()));

        if (!cuenta.getEstado()) {
            throw new IllegalArgumentException("La cuenta se encuentra inactiva");
        }

        BigDecimal saldoActual = obtenerSaldoActual(cuenta);

        BigDecimal nuevoSaldo = saldoActual.add(dto.getValor());

        if (dto.getValor().compareTo(BigDecimal.ZERO) < 0
                && nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoNoDisponibleException("Saldo no disponible");
        }

        String tipoMovimiento = dto.getValor().compareTo(BigDecimal.ZERO) > 0 ? "Deposito" : "Retiro";

        Movimiento movimiento = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(tipoMovimiento)
                .valor(dto.getValor())
                .saldo(nuevoSaldo)
                .cuenta(cuenta)
                .build();

        Movimiento saved = movimientoRepository.save(movimiento);
        log.info("Movimiento registrado: tipo={}, valor={}, nuevoSaldo={}, cuenta={}",
                tipoMovimiento, dto.getValor(), nuevoSaldo, dto.getNumeroCuenta());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarTodos() {
        return movimientoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResponseDTO obtenerPorId(Long id) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
        return mapToResponse(movimiento);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
        movimientoRepository.delete(movimiento);
        log.info("Movimiento eliminado con id: {}", id);
    }

    private BigDecimal obtenerSaldoActual(Cuenta cuenta) {
        Optional<Movimiento> ultimoMovimiento = movimientoRepository
                .findTopByCuentaNumeroCuentaOrderByFechaDesc(cuenta.getNumeroCuenta());

        return ultimoMovimiento
                .map(Movimiento::getSaldo)
                .orElse(cuenta.getSaldoInicial());
    }

    private MovimientoResponseDTO mapToResponse(Movimiento movimiento) {
        return MovimientoResponseDTO.builder()
                .id(movimiento.getId())
                .fecha(movimiento.getFecha())
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .valor(movimiento.getValor())
                .saldo(movimiento.getSaldo())
                .numeroCuenta(movimiento.getCuenta().getNumeroCuenta())
                .build();
    }
}