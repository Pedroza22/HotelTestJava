package com.hotel.Hotel.mapper;

import com.hotel.Hotel.domain.*;
import com.hotel.Hotel.dto.response.ReservaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReservaMapper - pruebas unitarias")
class ReservaMapperTest {

    private final ReservaMapper mapper = Mappers.getMapper(ReservaMapper.class);

    private Cliente cliente;
    private Habitacion habitacion;
    private RangoFechas periodo;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        cliente    = new Cliente("Ana López", "ana@test.com");
        habitacion = new HabitacionEstandar("201", 2, 150.0, 2);
        periodo    = new RangoFechas(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(4));
        reserva    = new Reserva(cliente, habitacion, periodo);
    }

    @Test
    @DisplayName("toResponse: nombreHuesped mapeado desde cliente.nombre")
    void nombreHuespedMapeado() {
        ReservaResponse response = mapper.toResponse(reserva);
        assertThat(response.nombreHuesped()).isEqualTo("Ana López");
    }

    @Test
    @DisplayName("toResponse: habitacionNumero mapeado desde habitacion.numero")
    void habitacionNumeroMapeado() {
        ReservaResponse response = mapper.toResponse(reserva);
        assertThat(response.habitacionNumero()).isEqualTo("201");
    }

    @Test
    @DisplayName("toResponse: fechaInicio y fechaFin no son null")
    void fechasNoNulas() {
        ReservaResponse response = mapper.toResponse(reserva);
        assertThat(response.fechaInicio()).isNotNull();
        assertThat(response.fechaFin()).isNotNull();
    }

    @Test
    @DisplayName("toResponse: estado = PENDIENTE, costoTotal = 150.0 * 3 = 450.0")
    void estadoYCostoTotal() {
        ReservaResponse response = mapper.toResponse(reserva);
        assertThat(response.estado()).isEqualTo("PENDIENTE");
        assertThat(response.costoTotal()).isEqualTo(450.0);
    }

    @Test
    @DisplayName("toResponse(null): retorna null")
    void toResponseNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}
