package com.hotel.Hotel.service;

import com.hotel.Hotel.domain.*;
import com.hotel.Hotel.dto.request.CrearReservaRequest;
import com.hotel.Hotel.dto.response.ReservaResponse;
import com.hotel.Hotel.exception.RecursoNoEncontradoException;
import com.hotel.Hotel.exception.ReglaNegocioException;
import com.hotel.Hotel.mapper.ReservaMapper;
import com.hotel.Hotel.repository.ClienteRepository;
import com.hotel.Hotel.repository.HabitacionRepository;
import com.hotel.Hotel.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService - pruebas unitarias")
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private ReservaMapper reservaMapper;

    @InjectMocks
    private ReservaService reservaService;

    // =====================================================================
    @Nested
    @DisplayName("Operaciones de Confirmación")
    class ConfirmacionTests {

        @Test
        @DisplayName("Debe confirmar exitosamente cuando la reserva existe y está PENDIENTE")
        void debeConfirmarReservaExitosamente() {
            UUID reservaId = UUID.randomUUID();
            Cliente cliente = new Cliente("Beatriz Morales", "beatriz@empresa.com");
            Habitacion habitacion = new HabitacionEstandar("201", 2, 200.0, 1);
            RangoFechas fechas = new RangoFechas(
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(3));
            Reserva reservaExistente = new Reserva(cliente, habitacion, fechas);

            ReservaResponse responseEsperado = new ReservaResponse(
                    reservaId, "Beatriz Morales", "201",
                    fechas.fechaInicio(), fechas.fechaFin(), 400.0, "CONFIRMADA");

            when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaExistente));
            when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseEsperado);

            ReservaResponse resultado = reservaService.confirmar(reservaId);

            assertThat(resultado).isNotNull();
            assertThat(resultado.estado()).isEqualTo("CONFIRMADA");
            assertThat(reservaExistente.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);

            verify(reservaRepository).findById(reservaId);
            verify(reservaRepository).save(reservaExistente);
            verify(reservaMapper).toResponse(reservaExistente);
        }

        @Test
        @DisplayName("confirmar: reserva no existe → RecursoNoEncontradoException")
        void confirmarReservaNoExiste() {
            UUID reservaId = UUID.randomUUID();
            when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservaService.confirmar(reservaId))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Operaciones de Creación")
    class CreacionTests {

        private CrearReservaRequest requestValido(UUID clienteId, UUID habitacionId) {
            return new CrearReservaRequest(
                    clienteId,
                    habitacionId,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(4));
        }

        @Test
        @DisplayName("crear exitoso: cliente activo + habitación disponible → retorna ReservaResponse")
        void crearExitoso() {
            UUID clienteId    = UUID.randomUUID();
            UUID habitacionId = UUID.randomUUID();
            UUID reservaId    = UUID.randomUUID();

            Cliente cliente = new Cliente("Pedro", "pedro@test.com");
            Habitacion habitacion = new HabitacionEstandar("301", 2, 100.0, 1);

            ReservaResponse responseEsperado = new ReservaResponse(
                    reservaId, "Pedro", "301",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(4),
                    300.0, "PENDIENTE");

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(habitacionRepository.findById(habitacionId)).thenReturn(Optional.of(habitacion));
            when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseEsperado);

            ReservaResponse resultado = reservaService.crear(requestValido(clienteId, habitacionId));

            assertThat(resultado).isNotNull();
            assertThat(resultado.estado()).isEqualTo("PENDIENTE");
        }

        @Test
        @DisplayName("crear: fechaFin antes que fechaInicio → ReglaNegocioException (sin mocks)")
        void crearFechasInvalidas() {
            CrearReservaRequest request = new CrearReservaRequest(
                    UUID.randomUUID(), UUID.randomUUID(),
                    LocalDateTime.now().plusDays(4),   // inicio posterior
                    LocalDateTime.now().plusDays(1));  // fin anterior

            assertThatThrownBy(() -> reservaService.crear(request))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("crear: cliente no existe → RecursoNoEncontradoException")
        void crearClienteNoExiste() {
            UUID clienteId = UUID.randomUUID();
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservaService.crear(requestValido(clienteId, UUID.randomUUID())))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("crear: habitación no existe → RecursoNoEncontradoException")
        void crearHabitacionNoExiste() {
            UUID clienteId    = UUID.randomUUID();
            UUID habitacionId = UUID.randomUUID();
            Cliente cliente = new Cliente("Pedro", "pedro@test.com");

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(habitacionRepository.findById(habitacionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservaService.crear(requestValido(clienteId, habitacionId)))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("crear: cliente inactivo → ReglaNegocioException")
        void crearClienteInactivo() {
            UUID clienteId    = UUID.randomUUID();
            UUID habitacionId = UUID.randomUUID();

            Cliente clienteInactivo = new Cliente("Inactivo", "inactivo@test.com");
            clienteInactivo.registrarPenalizacion();
            clienteInactivo.registrarPenalizacion();
            clienteInactivo.registrarPenalizacion(); // 3 penalizaciones -> inactivo

            Habitacion habitacion = new HabitacionEstandar("301", 2, 100.0, 1);

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteInactivo));
            when(habitacionRepository.findById(habitacionId)).thenReturn(Optional.of(habitacion));

            assertThatThrownBy(() -> reservaService.crear(requestValido(clienteId, habitacionId)))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("crear: guarda la reserva y llama al mapper")
        void crearVerificaGuardadoYMapper() {
            UUID clienteId    = UUID.randomUUID();
            UUID habitacionId = UUID.randomUUID();
            Cliente cliente   = new Cliente("Pedro", "pedro@test.com");
            Habitacion hab    = new HabitacionEstandar("301", 2, 100.0, 1);

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(habitacionRepository.findById(habitacionId)).thenReturn(Optional.of(hab));
            when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(
                    new ReservaResponse(UUID.randomUUID(), "Pedro", "301",
                            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4),
                            300.0, "PENDIENTE"));

            reservaService.crear(requestValido(clienteId, habitacionId));

            verify(reservaRepository, times(1)).save(any(Reserva.class));
            verify(reservaMapper, times(1)).toResponse(any(Reserva.class));
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Operaciones de Cancelación")
    class CancelacionTests {

        @Test
        @DisplayName("cancelar exitoso: estado CANCELADA")
        void cancelarExitoso() {
            UUID reservaId = UUID.randomUUID();
            Cliente cliente = new Cliente("Luis", "luis@test.com");
            Habitacion hab  = new HabitacionEstandar("101", 2, 100.0, 1);
            RangoFechas fechas = new RangoFechas(
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4));
            Reserva reserva = new Reserva(cliente, hab, fechas);

            ReservaResponse responseEsperado = new ReservaResponse(
                    reservaId, "Luis", "101",
                    fechas.fechaInicio(), fechas.fechaFin(), 300.0, "CANCELADA");

            when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
            when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(reservaMapper.toResponse(any())).thenReturn(responseEsperado);

            ReservaResponse resultado = reservaService.cancelar(reservaId);

            assertThat(resultado.estado()).isEqualTo("CANCELADA");
            assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        }

        @Test
        @DisplayName("cancelar: reserva no existe → RecursoNoEncontradoException")
        void cancelarReservaNoExiste() {
            UUID reservaId = UUID.randomUUID();
            when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservaService.cancelar(reservaId))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    // =====================================================================
    @Nested
    @DisplayName("Consultas")
    class ConsultasTests {

        @Test
        @DisplayName("obtenerPorId exitoso: retorna ReservaResponse")
        void obtenerPorIdExitoso() {
            UUID reservaId = UUID.randomUUID();
            Cliente cliente = new Cliente("María", "maria@test.com");
            Habitacion hab  = new HabitacionEstandar("102", 2, 150.0, 1);
            RangoFechas fechas = new RangoFechas(
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4));
            Reserva reserva = new Reserva(cliente, hab, fechas);

            ReservaResponse response = new ReservaResponse(
                    reservaId, "María", "102",
                    fechas.fechaInicio(), fechas.fechaFin(), 450.0, "PENDIENTE");

            when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
            when(reservaMapper.toResponse(reserva)).thenReturn(response);

            ReservaResponse resultado = reservaService.obtenerPorId(reservaId);

            assertThat(resultado).isNotNull();
            assertThat(resultado.nombreHuesped()).isEqualTo("María");
        }

        @Test
        @DisplayName("obtenerPorId: no existe → RecursoNoEncontradoException")
        void obtenerPorIdNoExiste() {
            UUID reservaId = UUID.randomUUID();
            when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservaService.obtenerPorId(reservaId))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("listarTodas: lista vacía")
        void listarTodasVacia() {
            when(reservaRepository.findAll()).thenReturn(Collections.emptyList());
            when(reservaMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<ReservaResponse> resultado = reservaService.listarTodas();

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("listarTodas: lista con elementos")
        void listarTodasConElementos() {
            Cliente cliente = new Cliente("María", "maria@test.com");
            Habitacion hab  = new HabitacionEstandar("102", 2, 150.0, 1);
            RangoFechas fechas = new RangoFechas(
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(4));
            Reserva reserva = new Reserva(cliente, hab, fechas);

            ReservaResponse response = new ReservaResponse(
                    UUID.randomUUID(), "María", "102",
                    fechas.fechaInicio(), fechas.fechaFin(), 450.0, "PENDIENTE");

            when(reservaRepository.findAll()).thenReturn(List.of(reserva));
            when(reservaMapper.toResponseList(List.of(reserva))).thenReturn(List.of(response));

            List<ReservaResponse> resultado = reservaService.listarTodas();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).nombreHuesped()).isEqualTo("María");
        }
    }
}
