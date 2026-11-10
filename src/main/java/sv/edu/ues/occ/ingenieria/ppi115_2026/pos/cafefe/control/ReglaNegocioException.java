package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.ApplicationException;

/**
 * Una regla del negocio no se cumple (por ejemplo, "lo pagado no cubre el
 * total"). El mensaje está pensado para el usuario y se muestra tal cual.
 * rollback = true: si se lanza dentro de un método de un EJB, el contenedor
 * revierte toda la transacción, así no queda nada guardado a medias.
 */
@ApplicationException(rollback = true)
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
