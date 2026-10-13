package com.hotel.Hotel.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reserva - pruebas de dominio")
class ReservaTest {

    private static Cliente cliente() {
        return new Cliente("Carlos Gómez", "carlos@correo.com");
    }

    private static Habitacion habitacion() {
        return new HabitacionEstandar("101", 2, 100.0, 1);
    }

    private static RangoFechas periodo(int inicioDias, int finDias) {
        return new RangoFechas(
                LocalDateTime.now().plusDays(inicioDias),
                LocalDateTime.now().plusDays(finDias));
    }

    // ---- caso original mantenido ----
    @Test
    @DisplayName("Debe confirmar una reserva en estado PENDIENTE y cambiar estado de habitación")
    void debeConfirmarReservaPendiente() {
        Cliente cliente = new Cliente("Carlos Gómez", "carlos@correo.com");
        Habitacion habitacion = new HabitacionEstandar("101", 2, 100.0, 1);
        RangoFechas periodo = new RangoFechas(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(4));
        Reserva reserva = new Reserva(cliente, habitacion, periodo);

        reserva.confirmar();

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(EstadoHabitacion.OCUPADA, habitacion.getEstado());
    }

    // ---- nuevos casos ----

    @Test
    @DisplayName("Constructor válido: estado inicial PENDIENTE")
    void constructorEstadoPendiente() {
        Reserva reserva = new Reserva(cliente(), habitacion(), periodo(1, 4));
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
        assertThat(reserva.getId()).isNotNull();
    }

    @Test
    @DisplayName("Constructor: cliente null lanza IllegalArgumentException")
    void constructorClienteNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Reserva(null, habitacion(), periodo(1, 4)));
    }

    @Test
    @DisplayName("Constructor: habitacion null lanza IllegalArgumentException")
    void constructorHabitacionNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Reserva(cliente(), null, periodo(1, 4)));
    }

    @Test
    @DisplayName("Constructor: periodo null lanza IllegalArgumentException")
    void constructorPeriodoNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Reserva(cliente(), habitacion(), null));
    }

    @Test
    @DisplayName("costoTotal = precioPorNoche * dias (100.0 * 3 = 300.0)")
    void costoTotalCalculado() {
        // 3 días: plusDays(1) a plusDays(4) = 3 días
        Habitacion hab = new HabitacionEstandar("101", 2, 100.0, 1);
        Reserva reserva = new Reserva(cliente(), hab, periodo(1, 4));
        assertThat(reserva.getCostoTotal()).isEqualTo(300.0);
    }

    @Test
    @DisplayName("costoTotal mínimo: getDias()==0 -> precioPorNoche * 1")
    void costoTotalMinimo() {
        // mismo día (0 días) -> max(1, 0) = 1
        LocalDateTime mismo = LocalDateTime.now().plusDays(5);
        Habitacion hab = new HabitacionEstandar("101", 2, 100.0, 1);
        Reserva reserva = new Reserva(cliente(), hab, new RangoFechas(mismo, mismo));
        assertThat(reserva.getCostoTotal()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("cancelar: estado CANCELADA y habitación DISPONIBLE")
    void cancelarCambiaEstados() {
        Habitacion hab = new HabitacionEstandar("101", 2, 100.0, 1);
        Reserva reserva = new Reserva(cliente(), hab, periodo(1, 4));

        reserva.cancelar();

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(hab.getEstado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
    }

    @Test
    @DisplayName("confirmar en habitación ya ocupada lanza IllegalStateException")
    void confirmarHabitacionOcupada() {
        Habitacion hab = new HabitacionEstandar("101", 2, 100.0, 1);
        Reserva reserva1 = new Reserva(cliente(), hab, periodo(1, 4));
        Reserva reserva2 = new Reserva(cliente(), hab, periodo(1, 4));

        reserva1.confirmar(); // ocupa la habitación

        assertThatIllegalStateException()
                .isThrownBy(reserva2::confirmar); // falla porque ya está OCUPADA
    }
}
