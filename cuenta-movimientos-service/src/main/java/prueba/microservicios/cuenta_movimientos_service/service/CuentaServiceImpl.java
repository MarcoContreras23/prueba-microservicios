package prueba.microservicios.cuenta_movimientos_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import prueba.microservicios.cuenta_movimientos_service.dto.CuentaDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.CuentaResponseDTO;
import prueba.microservicios.cuenta_movimientos_service.entity.Cuenta;
import prueba.microservicios.cuenta_movimientos_service.exception.ResourceNotFoundException;
import prueba.microservicios.cuenta_movimientos_service.repository.CuentaRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;

    @Override
    @Transactional
    public CuentaResponseDTO crear(CuentaDTO dto) {
        if (cuentaRepository.existsByNumeroCuenta(dto.getNumeroCuenta())) {
            throw new IllegalArgumentException("Ya existe una cuenta con el número: " + dto.getNumeroCuenta());
        }

        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta(dto.getNumeroCuenta())
                .tipoCuenta(dto.getTipoCuenta())
                .saldoInicial(dto.getSaldoInicial())
                .estado(dto.getEstado() != null ? dto.getEstado() : true)
                .clienteId(dto.getClienteId())
                .build();

        Cuenta saved = cuentaRepository.save(cuenta);
        log.info("Cuenta creada: {}", saved.getNumeroCuenta());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> listarTodas() {
        return cuentaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDTO obtenerPorNumeroCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));
        return mapToResponse(cuenta);
    }

    @Override
    @Transactional
    public CuentaResponseDTO actualizar(String numeroCuenta, CuentaDTO dto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));

        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setSaldoInicial(dto.getSaldoInicial());
        if (dto.getEstado() != null) {
            cuenta.setEstado(dto.getEstado());
        }
        cuenta.setClienteId(dto.getClienteId());

        Cuenta updated = cuentaRepository.save(cuenta);
        log.info("Cuenta actualizada: {}", updated.getNumeroCuenta());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void eliminar(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));

        cuenta.setEstado(false);
        cuentaRepository.save(cuenta);
        log.info("Cuenta eliminada (soft delete): {}", numeroCuenta);
    }

    private CuentaResponseDTO mapToResponse(Cuenta cuenta) {
        return CuentaResponseDTO.builder()
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .estado(cuenta.getEstado())
                .clienteId(cuenta.getClienteId())
                .build();
    }
}
