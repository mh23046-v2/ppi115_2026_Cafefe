package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.sql.SQLException;

/**
 * Traduce los errores de PostgreSQL a mensajes que entiende el usuario, sin
 * exponer nombres de tablas, columnas ni restricciones. El detalle técnico
 * completo se queda en el log del servidor.
 * Códigos SQLState: https://www.postgresql.org/docs/current/errcodes-appendix.html
 */
public final class TraductorErrores {

    public static final String GENERICO = "Ocurrió un error al acceder a los datos. Revise el registro del servidor";

    private TraductorErrores() {
    }

    public static String mensaje(Throwable ex) {
        String estado = sqlState(ex);
        if (estado == null) {
            return GENERICO;
        }
        return switch (estado) {
            case "23503" -> "El registro está relacionado con otros datos. Desactívelo en lugar de eliminarlo, o revise que lo seleccionado exista";
            case "23505" -> "Ya existe un registro con esos datos";
            case "23502" -> "Falta un dato obligatorio";
            case "23514" -> "Algún dato no cumple las reglas de la base de datos";
            case "22001" -> "Un texto supera el largo permitido";
            case "22003" -> "Un número está fuera del rango permitido";
            case "08001", "08006", "57P01" -> "No hay conexión con la base de datos. Intente más tarde";
            default -> GENERICO;
        };
    }

    /** Busca el SQLState en la cadena de causas (JPA envuelve la SQLException). */
    static String sqlState(Throwable t) {
        for (int i = 0; t != null && i < 15; i++, t = t.getCause()) {
            if (t instanceof SQLException sql && sql.getSQLState() != null) {
                return sql.getSQLState();
            }
        }
        return null;
    }
}
