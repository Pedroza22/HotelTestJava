package com.hotel.Hotel.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Jerarquía de excepciones - pruebas unitarias")
class ExcepcionesTest {

    @Test
    @DisplayName("RecursoNoEncontradoException(recurso, id): mensaje formateado correcto")
    void recursoNoEncontradoConIdFormateado() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("Cliente", id);

        assertThat(ex.getMessage())
                .contains("Cliente")
                .contains(id.toString())
                .contains("no fue encontrado en el sistema");
    }

    @Test
    @DisplayName("RecursoNoEncontradoException(mensaje): mensaje directo preservado")
    void recursoNoEncontradoMensajeDirecto() {
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("Recurso no encontrado");
        assertThat(ex.getMessage()).isEqualTo("Recurso no encontrado");
    }

    @Test
    @DisplayName("ReglaNegocioException: es HotelDomainException y RuntimeException")
    void reglaNegocioHerencia() {
        ReglaNegocioException ex = new ReglaNegocioException("Regla violada");

        assertThat(ex).isInstanceOf(HotelDomainException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("Regla violada");
    }

    @Test
    @DisplayName("HabitacionNoDisponibleException(numero): mensaje contiene número de habitación")
    void habitacionNoDisponibleMensaje() {
        HabitacionNoDisponibleException ex = new HabitacionNoDisponibleException("101");

        assertThat(ex.getMessage()).contains("101");
        assertThat(ex).isInstanceOf(HotelDomainException.class);
    }
}
