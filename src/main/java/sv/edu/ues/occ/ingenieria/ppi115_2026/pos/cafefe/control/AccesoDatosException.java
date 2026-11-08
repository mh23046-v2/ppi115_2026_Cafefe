package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class AccesoDatosException extends RuntimeException {
    public AccesoDatosException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
