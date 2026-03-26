package prueba.microservicios.cuenta_movimientos_service.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoResponseDTO;
import prueba.microservicios.cuenta_movimientos_service.exception.ResourceNotFoundException;
import prueba.microservicios.cuenta_movimientos_service.exception.SaldoNoDisponibleException;
import prueba.microservicios.cuenta_movimientos_service.service.MovimientoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovimientoController.class)
class MovimientoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private MovimientoService movimientoService;

        @Autowired
        private ObjectMapper objectMapper;

        private MovimientoDTO buildDepositoDTO() {
                return MovimientoDTO.builder()
                                .numeroCuenta("478758")
                                .valor(new BigDecimal("500.00"))
                                .build();
        }

        private MovimientoResponseDTO buildDepositoResponseDTO() {
                return MovimientoResponseDTO.builder()
                                .id(1L)
                                .fecha(LocalDateTime.of(2026, 3, 26, 10, 0))
                                .tipoMovimiento("Deposito")
                                .valor(new BigDecimal("500.00"))
                                .saldo(new BigDecimal("2500.00"))
                                .numeroCuenta("478758")
                                .build();
        }

        @Test
        void registrar_deposito_debeRetornarCreated() throws Exception {
                when(movimientoService.registrarMovimiento(any(MovimientoDTO.class)))
                                .thenReturn(buildDepositoResponseDTO());

                mockMvc.perform(post("/movimientos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildDepositoDTO())))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.tipoMovimiento").value("Deposito"))
                                .andExpect(jsonPath("$.valor").value(500.00))
                                .andExpect(jsonPath("$.saldo").value(2500.00))
                                .andExpect(jsonPath("$.numeroCuenta").value("478758"));
        }

        @Test
        void registrar_retiro_debeRetornarCreated() throws Exception {
                MovimientoResponseDTO retiroResponse = MovimientoResponseDTO.builder()
                                .id(2L)
                                .fecha(LocalDateTime.of(2026, 3, 26, 11, 0))
                                .tipoMovimiento("Retiro")
                                .valor(new BigDecimal("-200.00"))
                                .saldo(new BigDecimal("1800.00"))
                                .numeroCuenta("478758")
                                .build();
                when(movimientoService.registrarMovimiento(any(MovimientoDTO.class))).thenReturn(retiroResponse);

                MovimientoDTO retiroDTO = MovimientoDTO.builder()
                                .numeroCuenta("478758")
                                .valor(new BigDecimal("-200.00"))
                                .build();

                mockMvc.perform(post("/movimientos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(retiroDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.tipoMovimiento").value("Retiro"))
                                .andExpect(jsonPath("$.saldo").value(1800.00));
        }

        @Test
        void registrar_saldoInsuficiente_debeRetornarBadRequest() throws Exception {
                when(movimientoService.registrarMovimiento(any(MovimientoDTO.class)))
                                .thenThrow(new SaldoNoDisponibleException("Saldo no disponible"));

                MovimientoDTO retiroDTO = MovimientoDTO.builder()
                                .numeroCuenta("478758")
                                .valor(new BigDecimal("-10000.00"))
                                .build();

                mockMvc.perform(post("/movimientos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(retiroDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message", containsString("Saldo no disponible")));
        }

        @Test
        void registrar_conDatosInvalidos_debeRetornarBadRequest() throws Exception {
                MovimientoDTO dtoInvalido = MovimientoDTO.builder().build();

                mockMvc.perform(post("/movimientos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dtoInvalido)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void listarTodos_debeRetornarListaDeMovimientos() throws Exception {
                when(movimientoService.listarTodos()).thenReturn(List.of(buildDepositoResponseDTO()));

                mockMvc.perform(get("/movimientos"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        void obtenerPorId_debeRetornarMovimiento() throws Exception {
                when(movimientoService.obtenerPorId(1L)).thenReturn(buildDepositoResponseDTO());

                mockMvc.perform(get("/movimientos/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.numeroCuenta").value("478758"));
        }

        @Test
        void obtenerPorId_noExiste_debeRetornar404() throws Exception {
                when(movimientoService.obtenerPorId(999L))
                                .thenThrow(new ResourceNotFoundException("Movimiento no encontrado con id: 999"));

                mockMvc.perform(get("/movimientos/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message", containsString("999")));
        }

        @Test
        void eliminar_debeRetornarNoContent() throws Exception {
                doNothing().when(movimientoService).eliminar(1L);

                mockMvc.perform(delete("/movimientos/1"))
                                .andExpect(status().isNoContent());

                verify(movimientoService).eliminar(1L);
        }
}
