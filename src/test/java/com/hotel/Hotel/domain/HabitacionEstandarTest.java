package com.hotel.Hotel.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HabitacionEstandar - pruebas de dominio")
class HabitacionEstandarTest {

    @Test
    @DisplayName("Constructor válido: estado inicial DISPONIBLE")
    void constructorValido() {
        HabitacionEstandar hab = new HabitacionEstandar("101", 2, 150.0, 2);

        assertThat(hab.getId()).isNotNull();
        assertThat(hab.getNumero()).isEqualTo("101");
        assertThat(hab.getCapacidadMaxima()).isEqualTo(2);
        assertThat(hab.getPrecioPorNoche()).isEqualTo(150.0);
        assertThat(hab.getEstado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
        assertThat(hab.getCamasIndividuales()).isEqualTo(2);
    }

    @Test
    @DisplayName("Constructor: numero null lanza IllegalArgumentException")
    void constructorNumeroNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HabitacionEstandar(null, 2, 150.0, 1));
    }

    @Test
    @DisplayName("Constructor: capacidadMaxima=0 lanza IllegalArgumentException")
    void constructorCapacidadCero() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HabitacionEstandar("101", 0, 150.0, 1));
    }

    @Test
    @DisplayName("Constructor: precioPorNoche negativo lanza IllegalArgumentException")
    void constructorPrecioNegativo() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HabitacionEstandar("101", 2, -10.0, 1));
    }

    @Test
    @DisplayName("asignarAReserva: DISPONIBLE pasa a OCUPADA")
    void asignarAReservaDesdeDisponible() {
        HabitacionEstandar hab = new HabitacionEstandar("101", 2, 150.0, 1);
        hab.asignarAReserva();
        assertThat(hab.getEstado()).isEqualTo(EstadoHabitacion.OCUPADA);
    }

    @Test
    @DisplayName("asignarAReserva: ya OCUPADA lanza IllegalStateException")
    void asignarAReservaOcupada() {
        HabitacionEstandar hab = new HabitacionEstandar("101", 2, 150.0, 1);
        hab.asignarAReserva(); // primera vez ok

        assertThatIllegalStateException()
                .isThrownBy(hab::asignarAReserva); // segunda vez falla
    }

    @Test
    @DisplayName("habilitar: cambia estado a DISPONIBLE")
    void habilitarCambiaADisponible() {
        HabitacionEstandar hab = new HabitacionEstandar("101", 2, 150.0, 1);
        hab.asignarAReserva(); // OCUPADA

        hab.habilitar();

        assertThat(hab.getEstado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
    }

    @Test
    @DisplayName("getCamasIndividuales: devuelve valor del constructor")
    void getCamasIndividuales() {
        HabitacionEstandar hab = new HabitacionEstandar("101", 2, 150.0, 3);
        assertThat(hab.getCamasIndividuales()).isEqualTo(3);
    }

    @Test
    @DisplayName("asignarAReserva dos veces seguidas lanza IllegalStateException la segunda")
    void asignarDosVecesLanzaSegundaVez() {
        HabitacionEstandar hab = new HabitacionEstandar("202", 1, 80.0, 1);
        hab.asignarAReserva();

        assertThatIllegalStateException()
                .isThrownBy(hab::asignarAReserva)
                .withMessageContaining("disponible");
    }
}
