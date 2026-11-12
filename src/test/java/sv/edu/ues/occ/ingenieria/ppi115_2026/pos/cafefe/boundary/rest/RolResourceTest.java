package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RolResourceTest {

    private RolDAO rolDAO;
    private UriInfo uriInfo;
    private RolResource resource;

    private static final UUID ID_ROL = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        rolDAO = Mockito.mock(RolDAO.class);
        uriInfo = Mockito.mock(UriInfo.class);

        resource = new RolResource();
        resource.rolDAO = rolDAO;
    }

    private Rol crearRol(UUID id, String nombre, Boolean activo) {
        Rol r = new Rol(id);
        r.setNombre(nombre);
        r.setActivo(activo);
        r.setObservaciones("Rol de prueba");
        return r;
    }

    @Test
    void findRangeDevuelveListaConTotalRegistrosYAplicaTope() {
        Rol r1 = crearRol(ID_ROL, "Cajero", true);
        when(rolDAO.findByNombre("Cajero", 0, 50)).thenReturn(List.of(r1));
        when(rolDAO.countByNombre("Cajero")).thenReturn(1L);

        Response resp = resource.findRange(0, 100, "Cajero");

        assertEquals(200, resp.getStatus());
        assertEquals("1", resp.getHeaderString("Total-registros"));
        assertEquals(List.of(r1), resp.getEntity());
    }

    @Test
    void findRangeValidaParametrosDePaginacion() {
        Response respFirst = resource.findRange(-1, 10, null);
        assertEquals(422, respFirst.getStatus());
        assertEquals("first", respFirst.getHeaderString("Wrong-Param"));

        Response respMax = resource.findRange(0, 0, null);
        assertEquals(422, respMax.getStatus());
        assertEquals("max", respMax.getHeaderString("Wrong-Param"));
    }

    @Test
    void findByIdExitoYNoEncontrado() {
        Rol r1 = crearRol(ID_ROL, "Cajero", true);
        when(rolDAO.findById(ID_ROL)).thenReturn(r1);

        Response ok = resource.findById(ID_ROL);
        assertEquals(200, ok.getStatus());
        assertEquals(r1, ok.getEntity());

        UUID otroId = UUID.randomUUID();
        when(rolDAO.findById(otroId)).thenReturn(null);
        Response notFound = resource.findById(otroId);
        assertEquals(404, notFound.getStatus());
        assertEquals(otroId.toString(), notFound.getHeaderString("Entity-Not-Found"));
    }

    @Test
    void crearAsignaUuidYActivoPorDefecto() {
        Rol nuevo = new Rol();
        nuevo.setNombre("Supervisor");

        when(uriInfo.getAbsolutePathBuilder())
                .thenReturn(UriBuilder.fromUri("http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/rol"));

        Response resp = resource.crear(nuevo, uriInfo);

        assertEquals(201, resp.getStatus());
        assertNotNull(nuevo.getIdRol());
        assertTrue(nuevo.getActivo());
        assertEquals(
                "http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/rol/" + nuevo.getIdRol(),
                resp.getHeaderString("Location")
        );
        verify(rolDAO).create(nuevo);
    }

    @Test
    void crearRechazaRegistroNulo() {
        Response resp = resource.crear(null, uriInfo);
        assertEquals(422, resp.getStatus());
        assertEquals("registro", resp.getHeaderString("Wrong-Param"));
        verify(rolDAO, never()).create(any());
    }

    @Test
    void modificarActualizaCamposExistentes() {
        Rol existente = crearRol(ID_ROL, "Cajero", true);
        when(rolDAO.findById(ID_ROL)).thenReturn(existente);
        when(rolDAO.modify(existente)).thenReturn(existente);

        Rol cambios = new Rol();
        cambios.setNombre("Cajero Principal");
        cambios.setObservaciones("Actualizado");
        cambios.setActivo(false);

        Response resp = resource.modificar(ID_ROL, cambios);

        assertEquals(200, resp.getStatus());
        assertEquals("Cajero Principal", existente.getNombre());
        assertEquals("Actualizado", existente.getObservaciones());
        assertEquals(false, existente.getActivo());
        verify(rolDAO).modify(existente);
    }

    @Test
    void eliminarExitoYNoEncontrado() {
        Rol existente = crearRol(ID_ROL, "Cajero", true);
        when(rolDAO.findById(ID_ROL)).thenReturn(existente);

        Response ok = resource.eliminar(ID_ROL);
        assertEquals(204, ok.getStatus());
        verify(rolDAO).delete(existente);

        UUID otroId = UUID.randomUUID();
        when(rolDAO.findById(otroId)).thenReturn(null);
        Response notFound = resource.eliminar(otroId);
        assertEquals(404, notFound.getStatus());
    }
}