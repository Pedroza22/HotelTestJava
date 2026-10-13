package com.hotel.Hotel.service;

import com.hotel.Hotel.domain.Cliente;
import com.hotel.Hotel.dto.request.CrearClienteRequest;
import com.hotel.Hotel.dto.response.ClienteResponse;
import com.hotel.Hotel.exception.RecursoNoEncontradoException;
import com.hotel.Hotel.mapper.ClienteMapper;
import com.hotel.Hotel.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService - pruebas unitarias")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("crear exitoso: guarda cliente y retorna ClienteResponse")
    void crearExitoso() {
        CrearClienteRequest request = new CrearClienteRequest("María García", "maria@test.com");
        Cliente clienteGuardado = new Cliente("María García", "maria@test.com");
        ClienteResponse responseEsperado = new ClienteResponse(
                clienteGuardado.getId(), "María García", "maria@test.com", true, 0);

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);
        when(clienteMapper.toResponse(clienteGuardado)).thenReturn(responseEsperado);

        ClienteResponse resultado = clienteService.crear(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("María García");
        assertThat(resultado.email()).isEqualTo("maria@test.com");
    }

    @Test
    @DisplayName("crear: verifica que clienteMapper.toResponse se llama con el cliente guardado")
    void crearLlamaMapper() {
        CrearClienteRequest request = new CrearClienteRequest("María García", "maria@test.com");
        Cliente clienteGuardado = new Cliente("María García", "maria@test.com");
        ClienteResponse response = new ClienteResponse(
                clienteGuardado.getId(), "María García", "maria@test.com", true, 0);

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);
        when(clienteMapper.toResponse(clienteGuardado)).thenReturn(response);

        clienteService.crear(request);

        verify(clienteMapper, times(1)).toResponse(clienteGuardado);
    }

    @Test
    @DisplayName("listarTodos: lista vacía → retorna lista vacía")
    void listarTodosVacio() {
        when(clienteRepository.findAll()).thenReturn(Collections.emptyList());
        when(clienteMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ClienteResponse> resultado = clienteService.listarTodos();

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("listarTodos: lista con un cliente → retorna lista con un response")
    void listarTodosConElementos() {
        Cliente cliente = new Cliente("María García", "maria@test.com");
        ClienteResponse response = new ClienteResponse(
                cliente.getId(), "María García", "maria@test.com", true, 0);

        when(clienteRepository.findAll()).thenReturn(List.of(cliente));
        when(clienteMapper.toResponseList(List.of(cliente))).thenReturn(List.of(response));

        List<ClienteResponse> resultado = clienteService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo("María García");
    }

    @Test
    @DisplayName("obtenerPorId exitoso: retorna ClienteResponse")
    void obtenerPorIdExitoso() {
        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente("María García", "maria@test.com");
        ClienteResponse response = new ClienteResponse(id, "María García", "maria@test.com", true, 0);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(response);

        ClienteResponse resultado = clienteService.obtenerPorId(id);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("María García");
    }

    @Test
    @DisplayName("obtenerPorId: no existe → RecursoNoEncontradoException")
    void obtenerPorIdNoExiste() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorId(id))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
