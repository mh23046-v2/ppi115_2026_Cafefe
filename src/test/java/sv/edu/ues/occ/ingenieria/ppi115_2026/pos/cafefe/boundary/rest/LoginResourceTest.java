package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest.LoginResource.LoginRequest;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class LoginResourceTest {

    private EmpleadoDAO empleadoDAO;
    private EmpleadoRolDAO empleadoRolDAO;
    private LoginResource resource;

    private static final UUID ID_EMPLEADO = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ID_ROL_CAJERO = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_ER_CAJERO = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    @BeforeEach
    void setUp() {
        empleadoDAO = Mockito.mock(EmpleadoDAO.class);
        empleadoRolDAO = Mockito.mock(EmpleadoRolDAO.class);
        resource = new LoginResource();
        resource.empleadoDAO = empleadoDAO;
        resource.empleadoRolDAO = empleadoRolDAO;
    }

    private Empleado crearEmpleadoActivo(UUID id, String nombre, String apellido) {
        Empleado e = new Empleado(id);
        e.setNombre(nombre);
        e.setApellido(apellido);
        e.setActivo(true);
        return e;
    }

    private EmpleadoRol crearAsignacion(UUID idEr, Empleado emp, UUID idRol, String nombreRol) {
        Rol rol = new Rol(idRol);
        rol.setNombre(nombreRol);
        rol.setActivo(true);

        EmpleadoRol er = new EmpleadoRol(idEr);
        er.setIdEmpleado(emp);
        er.setIdRol(rol);
        er.setActivo(true);
        return er;
    }

    @Test
    void autenticarExitoPorNombreSinTildesYMinusculas() {
        Empleado emp = crearEmpleadoActivo(ID_EMPLEADO, "María", "Rivera");
        EmpleadoRol er = crearAsignacion(ID_ER_CAJERO, emp, ID_ROL_CAJERO, "Cajero");

        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(emp);
        when(empleadoRolDAO.findActivosByEmpleadoId(ID_EMPLEADO)).thenReturn(List.of(er));

        LoginRequest req = new LoginRequest();
        req.nombre = "maria";
        req.uid = ID_EMPLEADO.toString();

        Response resp = resource.autenticar(req);
        assertEquals(200, resp.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getEntity();
        assertNotNull(body.get("empleado"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> roles = (List<Map<String, Object>>) body.get("roles");
        assertEquals(1, roles.size());
        assertEquals("Cajero", roles.get(0).get("rol"));
    }

    @Test
    void autenticarExitoPorNombreCompleto() {
        Empleado emp = crearEmpleadoActivo(ID_EMPLEADO, "Juan", "Pérez");
        EmpleadoRol er = crearAsignacion(ID_ER_CAJERO, emp, ID_ROL_CAJERO, "Cajero");

        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(emp);
        when(empleadoRolDAO.findActivosByEmpleadoId(ID_EMPLEADO)).thenReturn(List.of(er));

        LoginRequest req = new LoginRequest();
        req.nombre = "Juan Perez";
        req.uid = ID_EMPLEADO.toString();

        Response resp = resource.autenticar(req);
        assertEquals(200, resp.getStatus());
    }

    @Test
    void autenticarFiltraRolTiendaEnLineaSiTieneOtrosRoles() {
        Empleado emp = crearEmpleadoActivo(ID_EMPLEADO, "Carla", "Mejía");
        EmpleadoRol erCajero = crearAsignacion(ID_ER_CAJERO, emp, ID_ROL_CAJERO, "Cajero");
        EmpleadoRol erOnline = crearAsignacion(UUID.randomUUID(), emp, UUID.randomUUID(), "Tienda en línea");

        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(emp);
        when(empleadoRolDAO.findActivosByEmpleadoId(ID_EMPLEADO)).thenReturn(List.of(erCajero, erOnline));

        LoginRequest req = new LoginRequest();
        req.nombre = "Carla";
        req.uid = ID_EMPLEADO.toString();

        Response resp = resource.autenticar(req);
        assertEquals(200, resp.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getEntity();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> roles = (List<Map<String, Object>>) body.get("roles");
        assertEquals(1, roles.size());
        assertEquals("Cajero", roles.get(0).get("rol"));
    }

    @Test
    void autenticarFallaSiSoloTieneRolTiendaEnLinea() {
        UUID idMesero = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Empleado mesero = crearEmpleadoActivo(idMesero, "Mesero", "Virtual");
        EmpleadoRol erOnline = crearAsignacion(UUID.randomUUID(), mesero, UUID.randomUUID(), "Tienda en línea");

        when(empleadoDAO.findById(idMesero)).thenReturn(mesero);
        when(empleadoRolDAO.findActivosByEmpleadoId(idMesero)).thenReturn(List.of(erOnline));

        LoginRequest req = new LoginRequest();
        req.nombre = "Mesero";
        req.uid = idMesero.toString();

        Response resp = resource.autenticar(req);
        assertEquals(401, resp.getStatus());
    }

    @Test
    void autenticarFallaRequestNuloOVacio() {
        assertEquals(401, resource.autenticar(null).getStatus());

        LoginRequest vacio = new LoginRequest();
        assertEquals(401, resource.autenticar(vacio).getStatus());
    }

    @Test
    void autenticarFallaUidMalFormado() {
        LoginRequest req = new LoginRequest();
        req.nombre = "Juan";
        req.uid = "no-es-uuid";

        assertEquals(401, resource.autenticar(req).getStatus());
    }

    @Test
    void autenticarFallaEmpleadoInexistenteOInactivo() {
        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(null);
        LoginRequest req = new LoginRequest();
        req.nombre = "Juan";
        req.uid = ID_EMPLEADO.toString();
        assertEquals(401, resource.autenticar(req).getStatus());

        Empleado inactivo = crearEmpleadoActivo(ID_EMPLEADO, "Pedro", "Alas");
        inactivo.setActivo(false);
        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(inactivo);
        req.nombre = "Pedro";
        assertEquals(401, resource.autenticar(req).getStatus());
    }

    @Test
    void autenticarFallaNombreIncorrecto() {
        Empleado emp = crearEmpleadoActivo(ID_EMPLEADO, "Juan", "Pérez");
        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(emp);

        LoginRequest req = new LoginRequest();
        req.nombre = "Carlos";
        req.uid = ID_EMPLEADO.toString();

        assertEquals(401, resource.autenticar(req).getStatus());
    }

    @Test
    void autenticarFallaSinRolesActivos() {
        Empleado emp = crearEmpleadoActivo(ID_EMPLEADO, "Sofía", "Ramos");
        when(empleadoDAO.findById(ID_EMPLEADO)).thenReturn(emp);
        when(empleadoRolDAO.findActivosByEmpleadoId(ID_EMPLEADO)).thenReturn(List.of());

        LoginRequest req = new LoginRequest();
        req.nombre = "Sofía";
        req.uid = ID_EMPLEADO.toString();

        assertEquals(401, resource.autenticar(req).getStatus());
    }
}