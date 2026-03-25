package prueba.microservicios.cliente_persona_service.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import prueba.microservicios.cliente_persona_service.dto.ClienteDTO;
import prueba.microservicios.cliente_persona_service.dto.ClienteResponseDTO;
import prueba.microservicios.cliente_persona_service.exception.GlobalExceptionHandler;
import prueba.microservicios.cliente_persona_service.exception.ResourceNotFoundException;
import prueba.microservicios.cliente_persona_service.service.ClienteService;

@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Autowired
    private ObjectMapper objectMapper;


    @Nested
    @DisplayName("POST /clientes - Crear cliente")
    class CrearCliente {

        @Test
        @DisplayName("Debe crear cliente exitosamente y retornar 201")
        void crearClienteExitosamente() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema")
                    .genero("Masculino")
                    .edad(30)
                    .identificacion("1234567890")
                    .direccion("Otavalo sn y principal")
                    .telefono("098254785")
                    .contrasena("1234")
                    .estado(true)
                    .build();

            ClienteResponseDTO response = ClienteResponseDTO.builder()
                    .personaId(1L)
                    .clienteId("abc12345")
                    .nombre("Jose Lema")
                    .genero("Masculino")
                    .edad(30)
                    .identificacion("1234567890")
                    .direccion("Otavalo sn y principal")
                    .telefono("098254785")
                    .estado(true)
                    .build();

            when(clienteService.crear(any(ClienteDTO.class))).thenReturn(response);

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.clienteId", is("abc12345")))
                    .andExpect(jsonPath("$.nombre", is("Jose Lema")))
                    .andExpect(jsonPath("$.genero", is("Masculino")))
                    .andExpect(jsonPath("$.edad", is(30)))
                    .andExpect(jsonPath("$.identificacion", is("1234567890")))
                    .andExpect(jsonPath("$.direccion", is("Otavalo sn y principal")))
                    .andExpect(jsonPath("$.telefono", is("098254785")))
                    .andExpect(jsonPath("$.estado", is(true)));

            verify(clienteService, times(1)).crear(any(ClienteDTO.class));
        }

        @Test
        @DisplayName("Debe crear cliente con estado por defecto (sin enviar estado)")
        void crearClienteSinEstado() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Maria Lopez")
                    .identificacion("0987654321")
                    .contrasena("5678")
                    .build();

            ClienteResponseDTO response = ClienteResponseDTO.builder()
                    .personaId(2L)
                    .clienteId("def67890")
                    .nombre("Maria Lopez")
                    .identificacion("0987654321")
                    .estado(true)
                    .build();

            when(clienteService.crear(any(ClienteDTO.class))).thenReturn(response);

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.clienteId", is("def67890")))
                    .andExpect(jsonPath("$.estado", is(true)));
        }

        @Test
        @DisplayName("Debe fallar validacion cuando no se envia nombre")
        void validacionFallaSinNombre() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .identificacion("1234567890")
                    .contrasena("1234")
                    .build();

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", containsString("nombre")));
        }

        @Test
        @DisplayName("Debe fallar validacion cuando no se envia identificacion")
        void validacionFallaSinIdentificacion() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema")
                    .contrasena("1234")
                    .build();

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", containsString("identificaci")));
        }

        @Test
        @DisplayName("Debe fallar validacion cuando no se envia contrasena")
        void validacionFallaSinContrasena() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema")
                    .identificacion("1234567890")
                    .build();

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", containsString("contrase")));
        }

        @Test
        @DisplayName("Debe fallar validacion cuando contrasena tiene menos de 4 caracteres")
        void validacionFallaContrasenaCorta() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema")
                    .identificacion("1234567890")
                    .contrasena("123")
                    .build();

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", containsString("contrase")));
        }

        @Test
        @DisplayName("Debe fallar validacion cuando edad es negativa")
        void validacionFallaEdadNegativa() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema")
                    .identificacion("1234567890")
                    .contrasena("1234")
                    .edad(-5)
                    .build();

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", containsString("edad")));
        }

        @Test
        @DisplayName("Debe fallar cuando body esta vacio")
        void validacionFallaBodyVacio() throws Exception {
            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }

        @Test
        @DisplayName("Debe retornar 400 cuando identificacion ya existe")
        void crearClienteConIdentificacionDuplicada() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema")
                    .identificacion("1234567890")
                    .contrasena("1234")
                    .build();

            when(clienteService.crear(any(ClienteDTO.class)))
                    .thenThrow(new IllegalArgumentException("Ya existe un cliente con la identificacion: 1234567890"));

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", containsString("Ya existe")));
        }
    }


    @Nested
    @DisplayName("GET /clientes - Listar clientes")
    class ListarClientes {

        @Test
        @DisplayName("Debe listar todos los clientes")
        void listarTodosLosClientes() throws Exception {
            ClienteResponseDTO cliente1 = ClienteResponseDTO.builder()
                    .personaId(1L)
                    .clienteId("abc12345")
                    .nombre("Jose Lema")
                    .estado(true)
                    .build();

            ClienteResponseDTO cliente2 = ClienteResponseDTO.builder()
                    .personaId(2L)
                    .clienteId("def67890")
                    .nombre("Marianela Montalvo")
                    .estado(true)
                    .build();

            when(clienteService.listarTodos()).thenReturn(List.of(cliente1, cliente2));

            mockMvc.perform(get("/clientes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].nombre", is("Jose Lema")))
                    .andExpect(jsonPath("$[1].nombre", is("Marianela Montalvo")));

            verify(clienteService, times(1)).listarTodos();
        }

        @Test
        @DisplayName("Debe retornar lista vacia cuando no hay clientes")
        void listarClientesVacio() throws Exception {
            when(clienteService.listarTodos()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/clientes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Debe retornar lista con un solo cliente")
        void listarUnSoloCliente() throws Exception {
            ClienteResponseDTO cliente = ClienteResponseDTO.builder()
                    .personaId(1L)
                    .clienteId("abc12345")
                    .nombre("Jose Lema")
                    .estado(true)
                    .build();

            when(clienteService.listarTodos()).thenReturn(List.of(cliente));

            mockMvc.perform(get("/clientes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].clienteId", is("abc12345")));
        }
    }


    @Nested
    @DisplayName("GET /clientes/{clienteId} - Obtener por ID")
    class ObtenerClientePorId {

        @Test
        @DisplayName("Debe obtener cliente por clienteId exitosamente")
        void obtenerClientePorId() throws Exception {
            ClienteResponseDTO response = ClienteResponseDTO.builder()
                    .personaId(1L)
                    .clienteId("abc12345")
                    .nombre("Jose Lema")
                    .genero("Masculino")
                    .edad(30)
                    .identificacion("1234567890")
                    .direccion("Otavalo sn y principal")
                    .telefono("098254785")
                    .estado(true)
                    .build();

            when(clienteService.obtenerPorClienteId("abc12345")).thenReturn(response);

            mockMvc.perform(get("/clientes/abc12345"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.clienteId", is("abc12345")))
                    .andExpect(jsonPath("$.nombre", is("Jose Lema")))
                    .andExpect(jsonPath("$.personaId", is(1)))
                    .andExpect(jsonPath("$.genero", is("Masculino")))
                    .andExpect(jsonPath("$.edad", is(30)))
                    .andExpect(jsonPath("$.identificacion", is("1234567890")))
                    .andExpect(jsonPath("$.direccion", is("Otavalo sn y principal")))
                    .andExpect(jsonPath("$.telefono", is("098254785")))
                    .andExpect(jsonPath("$.estado", is(true)));

            verify(clienteService, times(1)).obtenerPorClienteId("abc12345");
        }

        @Test
        @DisplayName("Debe retornar 404 cuando cliente no existe")
        void clienteNoEncontrado() throws Exception {
            when(clienteService.obtenerPorClienteId("inexistente"))
                    .thenThrow(new ResourceNotFoundException("Cliente no encontrado con clienteId: inexistente"));

            mockMvc.perform(get("/clientes/inexistente"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Cliente no encontrado")));

            verify(clienteService, times(1)).obtenerPorClienteId("inexistente");
        }
    }


    @Nested
    @DisplayName("PUT /clientes/{clienteId} - Actualizar cliente")
    class ActualizarCliente {

        @Test
        @DisplayName("Debe actualizar cliente exitosamente")
        void actualizarClienteExitosamente() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema Actualizado")
                    .identificacion("1234567890")
                    .contrasena("5678")
                    .direccion("Nueva direccion")
                    .build();

            ClienteResponseDTO response = ClienteResponseDTO.builder()
                    .personaId(1L)
                    .clienteId("abc12345")
                    .nombre("Jose Lema Actualizado")
                    .identificacion("1234567890")
                    .direccion("Nueva direccion")
                    .estado(true)
                    .build();

            when(clienteService.actualizar(eq("abc12345"), any(ClienteDTO.class))).thenReturn(response);

            mockMvc.perform(put("/clientes/abc12345")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre", is("Jose Lema Actualizado")))
                    .andExpect(jsonPath("$.direccion", is("Nueva direccion")))
                    .andExpect(jsonPath("$.clienteId", is("abc12345")));

            verify(clienteService, times(1)).actualizar(eq("abc12345"), any(ClienteDTO.class));
        }

        @Test
        @DisplayName("Debe retornar 404 al actualizar cliente inexistente")
        void actualizarClienteNoEncontrado() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema")
                    .identificacion("1234567890")
                    .contrasena("1234")
                    .build();

            when(clienteService.actualizar(eq("inexistente"), any(ClienteDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Cliente no encontrado con clienteId: inexistente"));

            mockMvc.perform(put("/clientes/inexistente")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", containsString("Cliente no encontrado")));
        }

        @Test
        @DisplayName("Debe fallar validacion al actualizar sin campos obligatorios")
        void actualizarClienteValidacionFalla() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema")
                    .build();

            mockMvc.perform(put("/clientes/abc12345")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }
    }

    @Nested
    @DisplayName("DELETE /clientes/{clienteId} - Eliminar cliente")
    class EliminarCliente {

        @Test
        @DisplayName("Debe eliminar (soft delete) cliente exitosamente y retornar 204")
        void eliminarClienteExitosamente() throws Exception {
            doNothing().when(clienteService).eliminar("abc12345");

            mockMvc.perform(delete("/clientes/abc12345"))
                    .andExpect(status().isNoContent());

            verify(clienteService, times(1)).eliminar("abc12345");
        }

        @Test
        @DisplayName("Debe retornar 404 al eliminar cliente inexistente")
        void eliminarClienteNoEncontrado() throws Exception {
            doThrow(new ResourceNotFoundException("Cliente no encontrado con clienteId: inexistente"))
                    .when(clienteService).eliminar("inexistente");

            mockMvc.perform(delete("/clientes/inexistente"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", containsString("Cliente no encontrado")));

            verify(clienteService, times(1)).eliminar("inexistente");
        }
    }

    @Nested
    @DisplayName("Otros escenarios")
    class OtrosEscenarios {

        @Test
        @DisplayName("Debe retornar 500 cuando el servicio lanza excepcion inesperada en crear")
        void errorInternoAlCrear() throws Exception {
            ClienteDTO request = ClienteDTO.builder()
                    .nombre("Jose Lema")
                    .identificacion("1234567890")
                    .contrasena("1234")
                    .build();

            when(clienteService.crear(any(ClienteDTO.class)))
                    .thenThrow(new RuntimeException("Error de conexion a BD"));

            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status", is(500)))
                    .andExpect(jsonPath("$.error", is("Internal Server Error")));
        }

        @Test
        @DisplayName("Debe retornar 500 cuando el servicio lanza excepcion inesperada en listar")
        void errorInternoAlListar() throws Exception {
            when(clienteService.listarTodos())
                    .thenThrow(new RuntimeException("Error de conexion a BD"));

            mockMvc.perform(get("/clientes"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status", is(500)));
        }
    }
}
