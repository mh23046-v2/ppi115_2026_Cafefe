package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmpleadoRolResourceTest {

    private EmpleadoRolDAO empleadoRolDAO;
    private EmpleadoDAO empleadoDAO;
    private RolDAO rolDAO;
    private UriInfo uriInfo;
    private EmpleadoRolResource resource;

    private static final UUID ID_EMPLEADO = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ID_ROL = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_ER = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    @BeforeEach
    void setUp() {
        empleadoRolDAO = Mockito.mock(EmpleadoRolDAO.class);
        empleadoDAO = Mockito.mock(EmpleadoDAO.class);
        rolDAO = Mockito.mock(RolDAO.class);
        uriInfo = Mockito.mock(UriInfo.class);

        resource = new EmpleadoRolResource();
        resource.empleadoRolDAO = empleadoRolDAO;
        resource.empleadoDAO = empleadoDAO;
        resource.rolDAO = rolDAO;
    }

    private Empleado crearEmpleado(UUID id) {
        Empleado e = new Empleado(id);
        e.setNombre("Ana");
        e.setApellido("López");
        e.setActivo(true);
        return e;
    }

    private Rol crearRol(UUID id, String nombre) {
        Rol r = new Rol(id);
        r.setNombre(nombre);
        r.setActivo(true);
        return r;
    }

    private EmpleadoRol crearAsignacion(UUID idEr, Empleado emp, Rol rol) {
        EmpleadoRol er = new EmpleadoRol(idEr);
        er.setIdEmpleado(emp);
        er.setIdRol(rol);
        er.setActivo(true);
        er.setObservaciones("Turno día");
        return er;
    }

    @Test
    void findByEmpleadoDevuelveListaConTotalRegistros() {
        Empleado emp = crearEmpleado(ID_EMPLEADO);
        Rol rol = crearRol(ID_ROL, "Cajero");
        EmpleadoRol er = crearAsignacion(ID_ER, emp, rol);

        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(emp);
        when(empleadoRolDAO.findByEmpleado(emp, 0, 50)).thenReturn(List.of(er));
        when(empleadoRolDAO.countByEmpleado(emp)).thenReturn(1);

        Response resp = resource.findByEmpleado(ID_EMPLEADO, 0, 50);

        assertEquals(200, resp.getStatus());
        assertEquals("1", resp.getHeaderString("Total-registros"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> salida = (List<Map<String, Object>>) resp.getEntity();
        assertEquals(1, salida.size());
        assertEquals("Cajero", salida.get(0).get("rol"));
    }

    @Test
    void findByIdValidaPertenenciaAlEmpleado() {
        Empleado otroEmp = crearEmpleado(UUID.randomUUID());
        EmpleadoRol er = crearAsignacion(ID_ER, otroEmp, crearRol(ID_ROL, "Cajero"));
        when(empleadoRolDAO.findById(ID_ER)).thenReturn(er);

        Response resp = resource.findById(ID_EMPLEADO, ID_ER);
        assertEquals(404, resp.getStatus());
        assertEquals(ID_ER.toString(), resp.getHeaderString("Entity-Not-Found"));
    }

    @Test
    void crearAsignaRolYDevuelve201() {
        Empleado emp = crearEmpleado(ID_EMPLEADO);
        Rol rol = crearRol(ID_ROL, "Cajero");

        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(emp);
        when(rolDAO.findById(ID_ROL)).thenReturn(rol);
        when(empleadoRolDAO.existeAsignacion(emp, rol, null)).thenReturn(false);
        when(uriInfo.getAbsolutePathBuilder())
                .thenReturn(UriBuilder.fromUri("http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/empleado/" + ID_EMPLEADO + "/rol"));

        EmpleadoRol nuevo = new EmpleadoRol();
        nuevo.setIdRol(new Rol(ID_ROL));

        Response resp = resource.crear(ID_EMPLEADO, nuevo, uriInfo);

        assertEquals(201, resp.getStatus());
        assertNotNull(nuevo.getIdEmpleadoRol());
        assertTrue(nuevo.getActivo());
        verify(empleadoRolDAO).create(nuevo);
    }

    @Test
    void crearRechazaAsignacionDuplicadaCon422() {
        Empleado emp = crearEmpleado(ID_EMPLEADO);
        Rol rol = crearRol(ID_ROL, "Cajero");

        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(emp);
        when(rolDAO.findById(ID_ROL)).thenReturn(rol);
        when(empleadoRolDAO.existeAsignacion(emp, rol, null)).thenReturn(true);

        EmpleadoRol nuevo = new EmpleadoRol();
        nuevo.setIdRol(new Rol(ID_ROL));

        Response resp = resource.crear(ID_EMPLEADO, nuevo, uriInfo);

        assertEquals(422, resp.getStatus());
        assertEquals("idRol", resp.getHeaderString("Wrong-Param"));
        verify(empleadoRolDAO, never()).create(any());
    }

    @Test
    void modificarActualizaObservacionesYEstado() {
        Empleado emp = crearEmpleado(ID_EMPLEADO);
        Rol rol = crearRol(ID_ROL, "Cajero");
        EmpleadoRol existente = crearAsignacion(ID_ER, emp, rol);

        when(empleadoRolDAO.findById(ID_ER)).thenReturn(existente);
        when(empleadoRolDAO.modify(existente)).thenReturn(existente);

        EmpleadoRol cambios = new EmpleadoRol();
        cambios.setObservaciones("Turno tarde");
        cambios.setActivo(false);

        Response resp = resource.modificar(ID_EMPLEADO, ID_ER, cambios);

        assertEquals(200, resp.getStatus());
        assertEquals("Turno tarde", existente.getObservaciones());
        assertEquals(false, existente.getActivo());
        verify(empleadoRolDAO).modify(existente);
    }

    @Test
    void eliminarValidaPertenenciaYElimina() {
        Empleado emp = crearEmpleado(ID_EMPLEADO);
        EmpleadoRol existente = crearAsignacion(ID_ER, emp, crearRol(ID_ROL, "Cajero"));
        when(empleadoRolDAO.findById(ID_ER)).thenReturn(existente);

        Response resp = resource.eliminar(ID_EMPLEADO, ID_ER);

        assertEquals(204, resp.getStatus());
        verify(empleadoRolDAO).delete(existente);
    }
}