package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TraductorErroresTest {

    private static AccesoDatosException errorBD(String sqlState, String detalle) {
        SQLException sql = new SQLException(detalle, sqlState);
        return new AccesoDatosException("No se pudo eliminar el registro", new PersistenceException(sql));
    }

    @Test
    @DisplayName("Llave foránea (23503): sugiere desactivar y no expone la tabla")
    void testLlaveForanea() {
        String msg = TraductorErrores.mensaje(errorBD("23503",
                "update or delete on table \"rol\" violates foreign key constraint \"fk_empleado_rol_rol\""));
        assertTrue(msg.contains("Desactívelo"));
        assertFalse(msg.contains("fk_"));
        assertFalse(msg.contains("rol\""));
    }

    @Test
    @DisplayName("Duplicado, nulo y texto largo tienen su propio mensaje")
    void testOtrosCodigos() {
        assertEquals("Ya existe un registro con esos datos", TraductorErrores.mensaje(errorBD("23505", "dup")));
        assertEquals("Falta un dato obligatorio", TraductorErrores.mensaje(errorBD("23502", "null")));
        assertEquals("Un texto supera el largo permitido", TraductorErrores.mensaje(errorBD("22001", "long")));
    }

    @Test
    @DisplayName("Código desconocido o sin SQLException: mensaje genérico")
    void testGenerico() {
        assertEquals(TraductorErrores.GENERICO, TraductorErrores.mensaje(errorBD("99999", "x")));
        assertEquals(TraductorErrores.GENERICO, TraductorErrores.mensaje(new RuntimeException("x")));
        assertEquals(TraductorErrores.GENERICO, TraductorErrores.mensaje(null));
    }
}
