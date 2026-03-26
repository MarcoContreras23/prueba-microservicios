package prueba.microservicios.cuenta_movimientos_service.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import prueba.microservicios.cuenta_movimientos_service.dto.CuentaDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.CuentaResponseDTO;
import prueba.microservicios.cuenta_movimientos_service.exception.ResourceNotFoundException;
import prueba.microservicios.cuenta_movimientos_service.service.CuentaService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private CuentaService cuentaService;

        @Autowired
        private ObjectMapper objectMapper;

        private CuentaDTO buildCuentaDTO() {
                return CuentaDTO.builder()
                                .numeroCuenta("478758")
                                .tipoCuenta("Ahorro")
                                .saldoInicial(new BigDecimal("2000.00"))
                                .estado(true)
                                .clienteId("CLI001")
                                .build();
        }

        private CuentaResponseDTO buildCuentaResponseDTO() {
                return CuentaResponseDTO.builder()
                                .numeroCuenta("478758")
                                .tipoCuenta("Ahorro")
                                .saldoInicial(new BigDecimal("2000.00"))
                                .estado(true)
                                .clienteId("CLI001")
                                .build();
        }

        @Test
        void crear_debeRetornarCreated() throws Exception {
                when(cuentaService.crear(any(CuentaDTO.class))).thenReturn(buildCuentaResponseDTO());

                mockMvc.perform(post("/cuentas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildCuentaDTO())))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.numeroCuenta").value("478758"))
                                .andExpect(jsonPath("$.tipoCuenta").value("Ahorro"))
                                .andExpect(jsonPath("$.saldoInicial").value(2000.00))
                                .andExpect(jsonPath("$.clienteId").value("CLI001"));
        }

        @Test
        void crear_conDatosInvalidos_debeRetornarBadRequest() throws Exception {
                CuentaDTO dtoInvalido = CuentaDTO.builder().build();

                mockMvc.perform(post("/cuentas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dtoInvalido)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void listarTodas_debeRetornarListaDeCuentas() throws Exception {
                when(cuentaService.listarTodas()).thenReturn(List.of(buildCuentaResponseDTO()));

                mockMvc.perform(get("/cuentas"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].numeroCuenta").value("478758"));
        }

        @Test
        void listarTodas_sinCuentas_debeRetornarListaVacia() throws Exception {
                when(cuentaService.listarTodas()).thenReturn(List.of());

                mockMvc.perform(get("/cuentas"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        void obtenerPorNumeroCuenta_debeRetornarCuenta() throws Exception {
                when(cuentaService.obtenerPorNumeroCuenta("478758")).thenReturn(buildCuentaResponseDTO());

                mockMvc.perform(get("/cuentas/478758"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.numeroCuenta").value("478758"))
                                .andExpect(jsonPath("$.tipoCuenta").value("Ahorro"));
        }

        @Test
        void obtenerPorNumeroCuenta_noExiste_debeRetornar404() throws Exception {
                when(cuentaService.obtenerPorNumeroCuenta("999999"))
                                .thenThrow(new ResourceNotFoundException("Cuenta no encontrada con número: 999999"));

                mockMvc.perform(get("/cuentas/999999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message", containsString("999999")));
        }

        @Test
        void actualizar_debeRetornarCuentaActualizada() throws Exception {
                CuentaResponseDTO actualizada = buildCuentaResponseDTO();
                actualizada.setTipoCuenta("Corriente");
                when(cuentaService.actualizar(eq("478758"), any(CuentaDTO.class))).thenReturn(actualizada);

                CuentaDTO dto = buildCuentaDTO();
                dto.setTipoCuenta("Corriente");

                mockMvc.perform(put("/cuentas/478758")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.tipoCuenta").value("Corriente"));
        }

        @Test
        void eliminar_debeRetornarNoContent() throws Exception {
                doNothing().when(cuentaService).eliminar("478758");

                mockMvc.perform(delete("/cuentas/478758"))
                                .andExpect(status().isNoContent());

                verify(cuentaService).eliminar("478758");
        }

        @Test
        void eliminar_noExiste_debeRetornar404() throws Exception {
                doThrow(new ResourceNotFoundException("Cuenta no encontrada con número: 999999"))
                                .when(cuentaService).eliminar("999999");

                mockMvc.perform(delete("/cuentas/999999"))
                                .andExpect(status().isNotFound());
        }
}
