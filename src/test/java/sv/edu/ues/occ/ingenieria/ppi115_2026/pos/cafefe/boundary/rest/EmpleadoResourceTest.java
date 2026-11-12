package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmpleadoResourceTest {

    private EmpleadoDAO empleadoDAO;
    private UriInfo uriInfo;
    private EmpleadoResource resource;

    private static final UUID ID_EMPLEADO = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @BeforeEach
    void setUp() {
        empleadoDAO = Mockito.mock(EmpleadoDAO.class);
        uriInfo = Mockito.mock(UriInfo.class);

        resource = new EmpleadoResource();
        resource.empleadoDAO = empleadoDAO;
    }

    private Empleado crearEmpleado(UUID id, String nombre, String apellido, Boolean activo) {
        Empleado e = new Empleado(id);
        e.setNombre(nombre);
        e.setApellido(apellido);
        e.setActivo(activo);
        e.setComentarios("Nota de prueba");
        return e;
    }

    @Test
    void findRangeDevuelveListaConTotalRegistrosYAplicaTope() {
        Empleado e1 = crearEmpleado(ID_EMPLEADO, "Ana", "López", true);
        when(empleadoDAO.findByNombre("Ana", 0, 50)).thenReturn(List.of(e1));
        when(empleadoDAO.countByNombre("Ana")).thenReturn(1L);

        Response resp = resource.findRange(0, 100, "Ana");

        assertEquals(200, resp.getStatus());
        assertEquals("1", resp.getHeaderString("Total-registros"));
        assertEquals(List.of(e1), resp.getEntity());
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
        Empleado e1 = crearEmpleado(ID_EMPLEADO, "Ana", "López", true);
        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(e1);

        Response ok = resource.findById(ID_EMPLEADO);
        assertEquals(200, ok.getStatus());
        assertEquals(e1, ok.getEntity());

        UUID otroId = UUID.randomUUID();
        when(empleadoDAO.findById(otroId)).thenReturn(null);
        Response notFound = resource.findById(otroId);
        assertEquals(404, notFound.getStatus());
        assertEquals(otroId.toString(), notFound.getHeaderString("Entity-Not-Found"));
    }

    @Test
    void crearAsignaUuidYActivoPorDefecto() {
        Empleado nuevo = new Empleado();
        nuevo.setNombre("Carlos");
        nuevo.setApellido("Gómez");

        when(uriInfo.getAbsolutePathBuilder())
                .thenReturn(UriBuilder.fromUri("http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/empleado"));

        Response resp = resource.crear(nuevo, uriInfo);

        assertEquals(201, resp.getStatus());
        assertNotNull(nuevo.getIdEmpleado());
        assertTrue(nuevo.getActivo());
        assertEquals(
                "http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/empleado/" + nuevo.getIdEmpleado(),
                resp.getHeaderString("Location")
        );
        verify(empleadoDAO).create(nuevo);
    }

    @Test
    void crearRechazaRegistroNulo() {
        Response resp = resource.crear(null, uriInfo);
        assertEquals(422, resp.getStatus());
        assertEquals("registro", resp.getHeaderString("Wrong-Param"));
        verify(empleadoDAO, never()).create(any());
    }

    @Test
    void modificarActualizaCamposExistentes() {
        Empleado existente = crearEmpleado(ID_EMPLEADO, "Ana", "López", true);
        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(existente);
        when(empleadoDAO.modify(existente)).thenReturn(existente);

        Empleado cambios = new Empleado();
        cambios.setNombre("Ana María");
        cambios.setApellido("López");
        cambios.setComentarios("Actualizado");
        cambios.setActivo(false);

        Response resp = resource.modificar(ID_EMPLEADO, cambios);

        assertEquals(200, resp.getStatus());
        assertEquals("Ana María", existente.getNombre());
        assertEquals("Actualizado", existente.getComentarios());
        assertEquals(false, existente.getActivo());
        verify(empleadoDAO).modify(existente);
    }

    @Test
    void eliminarExitoYNoEncontrado() {
        Empleado existente = crearEmpleado(ID_EMPLEADO, "Ana", "López", true);
        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(existente);

        Response ok = resource.eliminar(ID_EMPLEADO);
        assertEquals(204, ok.getStatus());
        verify(empleadoDAO).delete(existente);

        UUID otroId = UUID.randomUUID();
        when(empleadoDAO.findById(otroId)).thenReturn(null);
        Response notFound = resource.eliminar(otroId);
        assertEquals(404, notFound.getStatus());
    }
}