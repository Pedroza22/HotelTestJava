package com.hotel.Hotel.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("GlobalExceptionHandler - pruebas con MockMvc standalone")
class GlobalExceptionHandlerTest {

    // ---- Controller auxiliar inline ----
    @RestController
    static class TestController {
        @GetMapping("/test/404")
        void lanzar404() { throw new RecursoNoEncontradoException("Recurso no encontrado"); }

        @GetMapping("/test/400")
        void lanzar400() { throw new ReglaNegocioException("Regla violada"); }

        @GetMapping("/test/409")
        void lanzar409() { throw new HabitacionNoDisponibleException("101"); }

        @GetMapping("/test/500")
        void lanzar500() { throw new RuntimeException("Error interno inesperado"); }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /test/404 → HTTP 404 con codigoHttp=404")
    void debeRetornar404() throws Exception {
        mockMvc.perform(get("/test/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoHttp").value(404));
    }

    @Test
    @DisplayName("GET /test/400 → HTTP 400 con codigoHttp=400")
    void debeRetornar400() throws Exception {
        mockMvc.perform(get("/test/400"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoHttp").value(400));
    }

    @Test
    @DisplayName("GET /test/409 → HTTP 409 con codigoHttp=409")
    void debeRetornar409() throws Exception {
        mockMvc.perform(get("/test/409"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoHttp").value(409));
    }

    @Test
    @DisplayName("GET /test/500 → HTTP 500, mensaje contiene 'soporte'")
    void debeRetornar500() throws Exception {
        mockMvc.perform(get("/test/500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("soporte")));
    }
}
