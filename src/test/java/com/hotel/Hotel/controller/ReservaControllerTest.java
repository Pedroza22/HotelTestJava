package com.hotel.Hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.Hotel.dto.request.CrearReservaRequest;
import com.hotel.Hotel.dto.response.ReservaResponse;
import com.hotel.Hotel.exception.RecursoNoEncontradoException;
import com.hotel.Hotel.exception.ReglaNegocioException;
import com.hotel.Hotel.service.ReservaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
@DisplayName("ReservaController - pruebas @WebMvcTest")
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservaService reservaService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReservaResponse responseEjemplo() {
        return new ReservaResponse(
                UUID.randomUUID(), "Juan García", "101",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(4),
                300.0, "PENDIENTE");
    }

    private CrearReservaRequest requestValido() {
        return new CrearReservaRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(4));
    }

    @Test
    @DisplayName("POST /api/reservas válido → 201 Created con header Location")
    void postValido() throws Exception {
        ReservaResponse response = responseEjemplo();
        when(reservaService.crear(any())).thenReturn(response);

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("POST /api/reservas con campos null → 400 Bad Request (Bean Validation)")
    void postCamposNull() throws Exception {
        // Request con todos los campos null
        String jsonNulo = "{\"clienteId\":null,\"habitacionId\":null,\"fechaInicio\":null,\"fechaFin\":null}";

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNulo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reservas → service lanza ReglaNegocioException → 400")
    void postReglaNegocio() throws Exception {
        when(reservaService.crear(any())).thenThrow(new ReglaNegocioException("Fechas inválidas"));

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoHttp").value(400));
    }

    @Test
    @DisplayName("POST /api/reservas → service lanza RecursoNoEncontradoException → 404")
    void postRecursoNoEncontrado() throws Exception {
        when(reservaService.crear(any()))
                .thenThrow(new RecursoNoEncontradoException("Cliente no encontrado"));

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoHttp").value(404));
    }

    @Test
    @DisplayName("GET /api/reservas → 200 OK, lista vacía")
    void getListaVacia() throws Exception {
        when(reservaService.listarTodas()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/reservas → 200 OK, lista con elementos")
    void getListaConElementos() throws Exception {
        when(reservaService.listarTodas()).thenReturn(List.of(responseEjemplo()));

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/reservas/{id} → 200 OK, reserva existe")
    void getPorIdExiste() throws Exception {
        UUID id = UUID.randomUUID();
        ReservaResponse response = responseEjemplo();
        when(reservaService.obtenerPorId(id)).thenReturn(response);

        mockMvc.perform(get("/api/reservas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreHuesped").value("Juan García"));
    }

    @Test
    @DisplayName("GET /api/reservas/{id} → 404, reserva no existe")
    void getPorIdNoExiste() throws Exception {
        UUID id = UUID.randomUUID();
        when(reservaService.obtenerPorId(id))
                .thenThrow(new RecursoNoEncontradoException("Reserva", id));

        mockMvc.perform(get("/api/reservas/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoHttp").value(404));
    }
}
