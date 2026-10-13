package com.hotel.Hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.Hotel.dto.request.CrearClienteRequest;
import com.hotel.Hotel.dto.response.ClienteResponse;
import com.hotel.Hotel.exception.RecursoNoEncontradoException;
import com.hotel.Hotel.service.ClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@DisplayName("ClienteController - pruebas @WebMvcTest")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @Autowired
    private ObjectMapper objectMapper;

    private ClienteResponse responseEjemplo() {
        return new ClienteResponse(UUID.randomUUID(), "María García", "maria@test.com", true, 0);
    }

    @Test
    @DisplayName("POST /api/clientes válido → 201 Created con header Location")
    void postValido() throws Exception {
        ClienteResponse response = responseEjemplo();
        when(clienteService.crear(any())).thenReturn(response);

        CrearClienteRequest request = new CrearClienteRequest("María García", "maria@test.com");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("POST /api/clientes con email inválido → 400 (Bean Validation con @Valid)")
    void postEmailInvalido() throws Exception {
        // email con formato inválido
        String jsonInvalido = "{\"nombre\":\"María García\",\"email\":\"no-es-un-email\"}";

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/clientes → 200 OK, lista vacía")
    void getListaVacia() throws Exception {
        when(clienteService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/clientes → 200 OK, lista con clientes")
    void getListaConClientes() throws Exception {
        when(clienteService.listarTodos()).thenReturn(List.of(responseEjemplo()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("María García"));
    }

    @Test
    @DisplayName("GET /api/clientes/{id} → 200 OK, cliente existe")
    void getPorIdExiste() throws Exception {
        UUID id = UUID.randomUUID();
        ClienteResponse response = new ClienteResponse(id, "María García", "maria@test.com", true, 0);
        when(clienteService.obtenerPorId(id)).thenReturn(response);

        mockMvc.perform(get("/api/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("María García"));
    }

    @Test
    @DisplayName("GET /api/clientes/{id} → 404, cliente no existe")
    void getPorIdNoExiste() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.obtenerPorId(id))
                .thenThrow(new RecursoNoEncontradoException("Cliente", id));

        mockMvc.perform(get("/api/clientes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoHttp").value(404));
    }
}
