package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.json.bind.JsonbException;
import jakarta.json.stream.JsonParsingException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AccesoDatosException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglaNegocioException;

@Provider
public class ErrorMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(ErrorMapper.class.getName());
    static final int MAX_CABECERA = 200;
    static final int PROFUNDIDAD_MAXIMA = 10;

    @Override
    public Response toResponse(Throwable ex) {
        // 404 por UUID mal formado, 415, 405, etc.: ya traen su respuesta.
        WebApplicationException web = buscar(ex, WebApplicationException.class);
        if (web != null) {
            return web.getResponse();
        }

        // Validación de Bean Validation (@Valid o al persistir).
        ConstraintViolationException cve = buscar(ex, ConstraintViolationException.class);
        if (cve != null) {
            return construir(422, resumir(cve));
        }

        // JSON del cuerpo mal formado.
        if (buscar(ex, JsonbException.class) != null || buscar(ex, JsonParsingException.class) != null) {
            return construir(400, "El cuerpo de la petición no es un JSON válido");
        }

        // Errores de base de datos envueltos por el DAO.
        AccesoDatosException acceso = buscar(ex, AccesoDatosException.class);
        if (acceso != null) {
            return desdeAccesoDatos(acceso);
        }

        // Violaciones a reglas del negocio (mensaje pensado para el usuario).
        ReglaNegocioException regla = buscar(ex, ReglaNegocioException.class);
        if (regla != null) {
            return construir(422, regla.getMessage());
        }

        // Parámetros inválidos que lanzan el DAO o la lógica de negocio.
        IllegalArgumentException arg = buscar(ex, IllegalArgumentException.class);
        if (arg != null) {
            return construir(422, arg.getMessage());
        }

        LOG.log(Level.SEVERE, "Error no controlado en el REST", ex);
        return construir(500, "Error interno del servidor");
    }

    private Response desdeAccesoDatos(AccesoDatosException acceso) {
        SQLException sql = buscar(acceso, SQLException.class);
        String estadoSql = sql == null ? null : sql.getSQLState();
        if (estadoSql != null) {
            switch (estadoSql) {
                case "23505": // unique_violation
                    return construir(409, "Ya existe un registro con esos datos");
                case "23503": // foreign_key_violation
                    return construir(409, "El registro está relacionado con otros datos o referencia uno que no existe");
                case "23502": // not_null_violation
                    return construir(422, "Falta un dato obligatorio");
                case "23514": // check_violation
                    return construir(422, "Un valor no cumple las restricciones de la base de datos");
                default:
                    break;
            }
        }
        LOG.log(Level.SEVERE, acceso.getMessage(), acceso);
        // El mensaje de AccesoDatosException lo escribimos nosotros en cada DAO.
        return construir(500, acceso.getMessage());
    }

    static Response construir(int estado, String mensaje) {
        String texto = mensaje == null || mensaje.isBlank() ? "Error en la petición" : mensaje;
        return Response.status(estado)
                .type(MediaType.APPLICATION_JSON)
                .header("App-Error", limpiarCabecera(texto))
                .entity(Map.of("error", texto))
                .build();
    }

    static String resumir(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
    }

    static String limpiarCabecera(String texto) {
        String s = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replaceAll("[\\r\\n\\t]+", " ").replaceAll("[^\\x20-\\x7E]", "?").trim();
        return s.length() > MAX_CABECERA ? s.substring(0, MAX_CABECERA) : s;
    }

    static <T extends Throwable> T buscar(Throwable ex, Class<T> tipo) {
        Throwable actual = ex;
        for (int i = 0; actual != null && i < PROFUNDIDAD_MAXIMA; i++) {
            if (tipo.isInstance(actual)) {
                return tipo.cast(actual);
            }
            if (actual.getCause() == actual) {
                break;
            }
            actual = actual.getCause();
        }
        return null;
    }
}