package com.hotel.Hotel.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Cliente - pruebas de dominio")
class ClienteTest {

    @Test
    @DisplayName("Constructor válido: id no nulo, activo=true, penalizaciones=0")
    void constructorValido() {
        Cliente cliente = new Cliente("Juan Pérez", "juan@test.com");

        assertThat(cliente.getId()).isNotNull();
        assertThat(cliente.getNombre()).isEqualTo("Juan Pérez");
        assertThat(cliente.getEmail()).isEqualTo("juan@test.com");
        assertThat(cliente.isActivo()).isTrue();
        assertThat(cliente.getPenalizaciones()).isEqualTo(0);
    }

    @Test
    @DisplayName("Constructor: nombre null lanza IllegalArgumentException")
    void constructorNombreNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Cliente(null, "juan@test.com"));
    }

    @Test
    @DisplayName("Constructor: nombre en blanco lanza IllegalArgumentException")
    void constructorNombreBlanco() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Cliente("   ", "juan@test.com"));
    }

    @Test
    @DisplayName("Constructor: email null lanza IllegalArgumentException")
    void constructorEmailNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Cliente("Juan Pérez", null));
    }

    @Test
    @DisplayName("Constructor: email en blanco lanza IllegalArgumentException")
    void constructorEmailBlanco() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Cliente("Juan Pérez", "  "));
    }

    @Test
    @DisplayName("registrarPenalizacion 1 vez: activo sigue true")
    void unaPenalizacionSigueActivo() {
        Cliente cliente = new Cliente("Ana", "ana@test.com");
        cliente.registrarPenalizacion();

        assertThat(cliente.isActivo()).isTrue();
        assertThat(cliente.getPenalizaciones()).isEqualTo(1);
    }

    @Test
    @DisplayName("registrarPenalizacion 2 veces: activo sigue true")
    void dosPenalizacionesSigueActivo() {
        Cliente cliente = new Cliente("Ana", "ana@test.com");
        cliente.registrarPenalizacion();
        cliente.registrarPenalizacion();

        assertThat(cliente.isActivo()).isTrue();
        assertThat(cliente.getPenalizaciones()).isEqualTo(2);
    }

    @Test
    @DisplayName("registrarPenalizacion 3 veces: activo=false")
    void tresPenalizacionesDesactiva() {
        Cliente cliente = new Cliente("Ana", "ana@test.com");
        cliente.registrarPenalizacion();
        cliente.registrarPenalizacion();
        cliente.registrarPenalizacion();

        assertThat(cliente.isActivo()).isFalse();
        assertThat(cliente.getPenalizaciones()).isEqualTo(3);
    }

    @Test
    @DisplayName("reactivar: restablece activo=true y penalizaciones=0")
    void reactivarRestablece() {
        Cliente cliente = new Cliente("Ana", "ana@test.com");
        cliente.registrarPenalizacion();
        cliente.registrarPenalizacion();
        cliente.registrarPenalizacion();

        cliente.reactivar();

        assertThat(cliente.isActivo()).isTrue();
        assertThat(cliente.getPenalizaciones()).isEqualTo(0);
    }

    @Test
    @DisplayName("puedeRealizarReservas: true cuando activo")
    void puedeRealizarReservasCuandoActivo() {
        Cliente cliente = new Cliente("Ana", "ana@test.com");
        assertThat(cliente.puedeRealizarReservas()).isTrue();
    }

    @Test
    @DisplayName("puedeRealizarReservas: false tras 3 penalizaciones")
    void noPuedeRealizarReservasTresPenalizaciones() {
        Cliente cliente = new Cliente("Ana", "ana@test.com");
        cliente.registrarPenalizacion();
        cliente.registrarPenalizacion();
        cliente.registrarPenalizacion();

        assertThat(cliente.puedeRealizarReservas()).isFalse();
    }

    @Test
    @DisplayName("getReservas: retorna lista inmutable")
    void getReservasInmutable() {
        Cliente cliente = new Cliente("Ana", "ana@test.com");
        List<Reserva> reservas = cliente.getReservas();

        assertThatThrownBy(() -> reservas.add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
