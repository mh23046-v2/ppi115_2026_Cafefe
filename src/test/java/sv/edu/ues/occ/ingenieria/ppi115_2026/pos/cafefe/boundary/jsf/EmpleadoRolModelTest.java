package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.EmpleadoRolModel;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoRolModelTest {

    @Mock
    private EmpleadoRolDAO empleadoRolDAO;

    @Mock
    private RolDAO rolDAO;

    @InjectMocks
    private EmpleadoRolModel model;

    @Test
    @DisplayName("nuevoRegistro: Asigna UUID y vincula el empleado padre")
    void testNuevoRegistro() {
        Empleado e = new Empleado(UUID.randomUUID());
        model.setEmpleado(e);

        EmpleadoRol nuevo = model.nuevoRegistro();

        assertNotNull(nuevo);
        assertNotNull(nuevo.getIdEmpleadoRol());
        assertEquals(e, nuevo.getIdEmpleado());
        assertTrue(nuevo.getActivo());
    }

    @Test
    @DisplayName("cargarDatos y contarDatos: Retornan vacíos/0 si el empleado es nulo")
    void testCargarYContarSinEmpleado() {
        model.setEmpleado(null);

        assertTrue(model.cargarDatos(0, 10).isEmpty());
        assertEquals(0, model.contarDatos());
        verifyNoInteractions(empleadoRolDAO);
    }

    @Test
    @DisplayName("cargarDatos y contarDatos: Delegan al DAO usando la entidad Empleado")
    void testCargarYContarConEmpleado() {
        Empleado e = new Empleado(UUID.randomUUID());
        model.setEmpleado(e);

        when(empleadoRolDAO.findByEmpleado(e, 0, 10)).thenReturn(List.of(new EmpleadoRol()));
        when(empleadoRolDAO.countByEmpleado(e)).thenReturn(3);

        List<EmpleadoRol> datos = model.cargarDatos(0, 10);
        int total = model.contarDatos();

        assertFalse(datos.isEmpty());
        assertEquals(3, total);
        verify(empleadoRolDAO).findByEmpleado(e, 0, 10);
        verify(empleadoRolDAO).countByEmpleado(e);
    }

    @Test
    @DisplayName("validarRegistro: Valida presencia de empleado, rol y estado activo del rol")
    void testValidarRegistro() {
        EmpleadoRol er = new EmpleadoRol();
        Empleado e = new Empleado(UUID.randomUUID());
        Rol rInactivo = new Rol(UUID.randomUUID());
        rInactivo.setActivo(false);

        Rol rActivo = new Rol(UUID.randomUUID());
        rActivo.setActivo(true);

        assertNotNull(model.validarRegistro(null));

        // Sin empleado
        er.setIdEmpleado(null);
        er.setIdRol(rActivo);
        assertNotNull(model.validarRegistro(er));

        // Sin rol
        er.setIdEmpleado(e);
        er.setIdRol(null);
        assertNotNull(model.validarRegistro(er));

        // Con rol inactivo
        er.setIdRol(rInactivo);
        assertNotNull(model.validarRegistro(er));

        // Válido
        er.setIdRol(rActivo);
        assertNull(model.validarRegistro(er));
    }

    @Test
    @DisplayName("completarRol: Retorna la lista acotada desde RolDAO")
    void testCompletarRol() {
        when(rolDAO.findByNombre("admin", 10)).thenReturn(List.of(new Rol()));

        List<Rol> resultado = model.completarRol("admin");

        assertFalse(resultado.isEmpty());
        verify(rolDAO).findByNombre("admin", 10);
    }
}