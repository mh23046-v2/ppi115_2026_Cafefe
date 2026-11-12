package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ejb.EJBException;
import jakarta.json.bind.JsonbException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AccesoDatosException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglaNegocioException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ErrorMapperTest {

    private ErrorMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ErrorMapper();
    }

    @Test
    void desenrollaSqlStateEnvueltoEnEjbYAccesoDatosException() {
        SQLException duplicado = new SQLException("duplicate key", "23505");
        AccesoDatosException daoEx = new AccesoDatosException("Error al crear", duplicado);
        EJBException ejbEx = new EJBException(daoEx);

        Response resp409 = mapper.toResponse(ejbEx);
        assertEquals(409, resp409.getStatus());
        assertEquals("Ya existe un registro con esos datos", resp409.getHeaderString("App-Error"));

        SQLException fkEx = new SQLException("violates foreign key", "23503");
        Response respFk = mapper.toResponse(new AccesoDatosException("Error FK", fkEx));
        assertEquals(409, respFk.getStatus());
        assertTrue(respFk.getHeaderString("App-Error").contains("relacionado"));

        SQLException notNullEx = new SQLException("null value", "23502");
        Response respNull = mapper.toResponse(new AccesoDatosException("Error null", notNullEx));
        assertEquals(422, respNull.getStatus());
        assertEquals("Falta un dato obligatorio", respNull.getHeaderString("App-Error"));
    }

    @Test
    void resumeYOrdenaConstraintViolationsEn422() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> v1 = Mockito.mock(ConstraintViolation.class);
        Path p1 = Mockito.mock(Path.class);
        when(p1.toString()).thenReturn("nombre");
        when(v1.getPropertyPath()).thenReturn(p1);
        when(v1.getMessage()).thenReturn("es obligatorio");

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> v2 = Mockito.mock(ConstraintViolation.class);
        Path p2 = Mockito.mock(Path.class);
        when(p2.toString()).thenReturn("activo");
        when(v2.getPropertyPath()).thenReturn(p2);
        when(v2.getMessage()).thenReturn("no puede ser nulo");

        ConstraintViolationException cve = new ConstraintViolationException(Set.of(v1, v2));
        Response resp = mapper.toResponse(new EJBException(cve));

        assertEquals(422, resp.getStatus());
        assertEquals("activo: no puede ser nulo; nombre: es obligatorio", resp.getHeaderString("App-Error"));

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getEntity();
        assertEquals("activo: no puede ser nulo; nombre: es obligatorio", body.get("error"));
    }

    @Test
    void sanitizaTildesSaltosDeLineaYTruncaCabeceraAppError() {
        String conTildesYSaltos = "La orden número #15\nno puede modificarse\tporque está pagada €";
        Response resp = mapper.toResponse(new ReglaNegocioException(conTildesYSaltos));

        assertEquals(422, resp.getStatus());
        assertEquals(
                "La orden numero #15 no puede modificarse porque esta pagada ?",
                resp.getHeaderString("App-Error")
        );

        String largo = "X".repeat(350);
        Response respLargo = mapper.toResponse(new IllegalArgumentException(largo));
        assertEquals(422, respLargo.getStatus());
        assertEquals(ErrorMapper.MAX_CABECERA, respLargo.getHeaderString("App-Error").length());
    }

    @Test
    void mapeaJsonInvalidoA400YPreservaWebApplicationException() {
        Response respJson = mapper.toResponse(new JsonbException("Token inesperado"));
        assertEquals(400, respJson.getStatus());
        assertEquals("El cuerpo de la peticion no es un JSON valido", respJson.getHeaderString("App-Error"));

        NotAllowedException notAllowed = new NotAllowedException("POST");
        Response resp405 = mapper.toResponse(notAllowed);
        assertEquals(405, resp405.getStatus());
    }

    @Test
    void soportaCicloEnCausasYDevuelve500ParaErrorDesconocido() {
        RuntimeException ciclo = new RuntimeException("Fallo inesperado") {
            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };

        assertNull(ErrorMapper.buscar(ciclo, SQLException.class));

        Response resp500 = mapper.toResponse(ciclo);
        assertEquals(500, resp500.getStatus());
        assertEquals("Error interno del servidor", resp500.getHeaderString("App-Error"));
    }
}