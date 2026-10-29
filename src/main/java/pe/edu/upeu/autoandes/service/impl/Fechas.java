package pe.edu.upeu.autoandes.service.impl;

import java.time.LocalDate;
import pe.edu.upeu.autoandes.exception.SolicitudInvalidaException;

final class Fechas {
    private Fechas() {}
    static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta))
        throw new SolicitudInvalidaException("La fecha desde no puede ser posterior a hasta");
    }
}
