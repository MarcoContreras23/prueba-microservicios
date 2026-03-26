package prueba.microservicios.cuenta_movimientos_service.dto;

import java.util.List;

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
public class ReporteDTO {

    private String cliente;
    private List<EstadoCuentaDTO> cuentas;
}
