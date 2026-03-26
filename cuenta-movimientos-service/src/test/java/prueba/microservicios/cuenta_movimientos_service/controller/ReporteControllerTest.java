package prueba.microservicios.cuenta_movimientos_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import prueba.microservicios.cuenta_movimientos_service.dto.EstadoCuentaDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoReporteDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.ReporteDTO;
import prueba.microservicios.cuenta_movimientos_service.exception.ResourceNotFoundException;
import prueba.microservicios.cuenta_movimientos_service.service.ReporteService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
class ReporteControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ReporteService reporteService;

        @Test
        void generarReporte_debeRetornarReporteExitoso() throws Exception {
                MovimientoReporteDTO movimientoReporte = MovimientoReporteDTO.builder()
                                .tipo("Deposito")
                                .valor(new BigDecimal("500.00"))
                                .saldo(new BigDecimal("2500.00"))
                                .build();
                EstadoCuentaDTO estadoCuenta = EstadoCuentaDTO.builder()
                                .numeroCuenta("478758")
                                .tipo("Ahorro")
                                .saldoInicial(new BigDecimal("2000.00"))
                                .estado(true).movimientos(List.of(movimientoReporte))
                                .build();
                ReporteDTO reporte = ReporteDTO.builder()
                                .cliente("Jose Lema")
                                .cuentas(List.of(estadoCuenta))
                                .build();

                when(reporteService.generarEstadoCuenta("CLI001",
                                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                                .thenReturn(reporte);

                mockMvc.perform(get("/reportes")
                                .param("fechaInicio", "2026-03-01")
                                .param("fechaFin", "2026-03-31")
                                .param("cliente", "CLI001"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.cliente").value("Jose Lema"))
                                .andExpect(jsonPath("$.cuentas", hasSize(1)))
                                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("478758"))
                                .andExpect(jsonPath("$.cuentas[0].movimientos", hasSize(1)))
                                .andExpect(jsonPath("$.cuentas[0].movimientos[0].valor").value(500.00));
        }

        @Test
        void generarReporte_conMultiplesCuentas_debeRetornarTodas() throws Exception {
                ReporteDTO reporte = ReporteDTO.builder()
                                .cliente("Marianela Montalvo")
                                .cuentas(List.of(
                                                EstadoCuentaDTO.builder()
                                                                .numeroCuenta("478758")
                                                                .tipo("Ahorro")
                                                                .saldoInicial(new BigDecimal("2000.00"))
                                                                .estado(true)
                                                                .movimientos(List.of())
                                                                .build(),
                                                EstadoCuentaDTO.builder()
                                                                .numeroCuenta("496825")
                                                                .tipo("Corriente")
                                                                .saldoInicial(new BigDecimal("100.00"))
                                                                .estado(true)
                                                                .movimientos(List.of())
                                                                .build()))
                                .build();

                when(reporteService.generarEstadoCuenta("CLI002",
                                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                                .thenReturn(reporte);

                mockMvc.perform(get("/reportes")
                                .param("fechaInicio", "2026-03-01")
                                .param("fechaFin", "2026-03-31")
                                .param("cliente", "CLI002"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.cuentas", hasSize(2)))
                                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("478758"))
                                .andExpect(jsonPath("$.cuentas[1].numeroCuenta").value("496825"));
        }

        @Test
        void generarReporte_clienteNoExiste_debeRetornar404() throws Exception {
                when(reporteService.generarEstadoCuenta("CLI999",
                                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                                .thenThrow(new ResourceNotFoundException("Cliente no encontrado: CLI999"));

                mockMvc.perform(get("/reportes")
                                .param("fechaInicio", "2026-03-01")
                                .param("fechaFin", "2026-03-31")
                                .param("cliente", "CLI999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message", containsString("CLI999")));
        }

        @Test
        void generarReporte_sinParametros_debeRetornarError() throws Exception {
                mockMvc.perform(get("/reportes"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void generarReporte_sinMovimientos_debeRetornarCuentasConListaVacia() throws Exception {
                ReporteDTO reporte = ReporteDTO.builder()
                                .cliente("Jose Lema")
                                .cuentas(List.of(
                                                EstadoCuentaDTO.builder()
                                                                .numeroCuenta("478758")
                                                                .tipo("Ahorro")
                                                                .saldoInicial(new BigDecimal("2000.00"))
                                                                .estado(true)
                                                                .movimientos(List.of())
                                                                .build()))
                                .build();

                when(reporteService.generarEstadoCuenta("CLI001",
                                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
                                .thenReturn(reporte);

                mockMvc.perform(get("/reportes")
                                .param("fechaInicio", "2026-01-01")
                                .param("fechaFin", "2026-01-31")
                                .param("cliente", "CLI001"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.cuentas[0].movimientos", hasSize(0)));
        }
}
