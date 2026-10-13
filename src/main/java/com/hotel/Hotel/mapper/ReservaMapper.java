package com.hotel.Hotel.mapper;

import com.hotel.Hotel.domain.Reserva;
import com.hotel.Hotel.dto.response.ReservaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(target = "nombreHuesped",    source = "cliente.nombre")
    @Mapping(target = "habitacionNumero", source = "habitacion.numero")
    @Mapping(target = "fechaInicio",      source = "periodo.fechaInicio")
    @Mapping(target = "fechaFin",         source = "periodo.fechaFin")
    @Mapping(target = "estado",           expression = "java(reserva.getEstado().name())")
    ReservaResponse toResponse(Reserva reserva);

    List<ReservaResponse> toResponseList(List<Reserva> reservas);
}
