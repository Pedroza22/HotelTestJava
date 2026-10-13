package com.hotel.Hotel.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RangoFechas - pruebas de dominio")
class RangoFechasTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 6, 1, 14, 0);
    private static final LocalDateTime FIN    = LocalDateTime.of(2030, 6, 4, 12, 0);

    @Test
    @DisplayName("Constructor válido: no lanza excepción")
    void constructorValido() {
        assertThatNoException().isThrownBy(() -> new RangoFechas(INICIO, FIN));
    }

    @Test
    @DisplayName("fechaInicio null lanza IllegalArgumentException")
    void fechaInicioNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new RangoFechas(null, FIN));
    }

    @Test
    @DisplayName("fechaFin null lanza IllegalArgumentException")
    void fechaFinNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new RangoFechas(INICIO, null));
    }

    @Test
    @DisplayName("fechaFin antes que fechaInicio lanza IllegalArgumentException")
    void fechaFinAntesDeInicio() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new RangoFechas(FIN, INICIO));
    }

    @Test
    @DisplayName("Fechas iguales (fin == inicio): válido, no lanza")
    void fechasIgualesValido() {
        assertThatNoException()
                .isThrownBy(() -> new RangoFechas(INICIO, INICIO));
    }

    @Test
    @DisplayName("getDias: 3 días de diferencia retorna 3L")
    void getDiasTresDias() {
        // Usar misma hora para evitar truncamiento parcial de días
        LocalDateTime inicio3 = LocalDateTime.of(2030, 6, 1, 14, 0);
        LocalDateTime fin3    = LocalDateTime.of(2030, 6, 4, 14, 0); // exactamente 3 días
        RangoFechas rango = new RangoFechas(inicio3, fin3);
        assertThat(rango.getDias()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getDias: mismo día retorna 0L")
    void getDiasMismoDia() {
        RangoFechas rango = new RangoFechas(INICIO, INICIO);
        assertThat(rango.getDias()).isEqualTo(0L);
    }
}
