package prueba.microservicios.cuenta_movimientos_service.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoReporteDTO {
    private String fecha;
    private String tipo;
    private BigDecimal valor;
    private BigDecimal saldo;
}
