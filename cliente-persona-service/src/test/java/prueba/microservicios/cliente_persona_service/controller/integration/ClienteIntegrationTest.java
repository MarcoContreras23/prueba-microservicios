package prueba.microservicios.cliente_persona_service.controller.integration;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import prueba.microservicios.cliente_persona_service.dto.ClienteDTO;
import prueba.microservicios.cliente_persona_service.dto.ClienteResponseDTO;
import prueba.microservicios.cliente_persona_service.repository.ClienteRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestRabbitMQConfig.class)
class ClienteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("Flujo completo: Crear, obtener, actualizar y eliminar cliente")
    void flujoCrudCompleto() throws Exception {
        // 1. Crear cliente
        ClienteDTO createRequest = ClienteDTO.builder()
                .nombre("Jose Lema")
                .genero("Masculino")
                .edad(30)
                .identificacion("1234567890")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .contrasena("1234")
                .estado(true)
                .build();

        MvcResult createResult = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Jose Lema")))
                .andExpect(jsonPath("$.clienteId", notNullValue()))
                .andExpect(jsonPath("$.estado", is(true)))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        String clienteId = objectMapper.readValue(responseJson, ClienteResponseDTO.class).getClienteId();

        // 2. Obtener cliente por ID
        mockMvc.perform(get("/clientes/" + clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Jose Lema")))
                .andExpect(jsonPath("$.clienteId", is(clienteId)));

        // 3. Listar todos los clientes
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 4. Actualizar cliente
        ClienteDTO updateRequest = ClienteDTO.builder()
                .nombre("Jose Lema Actualizado")
                .genero("Masculino")
                .edad(31)
                .identificacion("1234567890")
                .direccion("Nueva dirección 123")
                .telefono("098254785")
                .contrasena("5678")
                .estado(true)
                .build();

        mockMvc.perform(put("/clientes/" + clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Jose Lema Actualizado")))
                .andExpect(jsonPath("$.direccion", is("Nueva dirección 123")));

        // 5. Eliminar cliente (soft delete)
        mockMvc.perform(delete("/clientes/" + clienteId))
                .andExpect(status().isNoContent());

        // 6. Verificar que el cliente se marcó como inactivo
        mockMvc.perform(get("/clientes/" + clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is(false)));
    }

    @Test
    @DisplayName("Crear cliente con identificación duplicada retorna error")
    void crearClienteConIdentificacionDuplicada() throws Exception {
        ClienteDTO request = ClienteDTO.builder()
                .nombre("Jose Lema")
                .identificacion("1234567890")
                .contrasena("1234")
                .build();

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Intentar crear otro con la misma identificación
        ClienteDTO duplicateRequest = ClienteDTO.builder()
                .nombre("Otro Cliente")
                .identificacion("1234567890")
                .contrasena("5678")
                .build();

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest());
    }
}
