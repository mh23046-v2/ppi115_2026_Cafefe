package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.EmpleadoRolModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.EmpleadoModel;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoModelTest {

    @Mock
    private EmpleadoDAO empleadoDAO;

    @Mock
    private EmpleadoRolModel empleadoRolModel;

    @InjectMocks
    private EmpleadoModel model;

    @Test
    @DisplayName("nuevoRegistro: Inicializa un Empleado con UUID y estado activo")
    void testNuevoRegistro() {
        Empleado nuevo = model.nuevoRegistro();
        assertNotNull(nuevo);
        assertNotNull(nuevo.getIdEmpleado());
        assertTrue(nuevo.getActivo());
    }

    @Test
    @DisplayName("registroCambio: Notifica al model de EmpleadoRol y resetea pestaña activa")
    void testRegistroCambio() {
        Empleado e = new Empleado(UUID.randomUUID());
        model.setRegistro(e);
        model.setTabActiva(1);

        model.registroCambio();

        verify(empleadoRolModel, times(2)).setEmpleado(e);
        verify(empleadoRolModel, times(2)).btnCancelarHandler();
        assertEquals(0, model.getTabActiva());
    }

    @Test
    @DisplayName("validarRegistro: Valida presencia de nombre y apellido")
    void testValidarRegistro() {
        Empleado e = new Empleado();

        assertNotNull(model.validarRegistro(null));

        e.setNombre(null);
        e.setApellido("Pérez");
        assertNotNull(model.validarRegistro(e));

        e.setNombre("Juan");
        e.setApellido(null);
        assertNotNull(model.validarRegistro(e));

        e.setNombre("   ");
        e.setApellido("Pérez");
        assertNotNull(model.validarRegistro(e));

        e.setNombre("Juan");
        e.setApellido("Pérez");
        assertNull(model.validarRegistro(e));
    }

    @Test
    @DisplayName("cargarDatos: Consulta la tabla principal paginada vía DAO")
    void testCargarDatos() {
        when(empleadoDAO.findByNombre("", 0, 10)).thenReturn(List.of(new Empleado()));

        List<Empleado> resultado = model.cargarDatos(0, 10);

        assertFalse(resultado.isEmpty());
        verify(empleadoDAO).findByNombre("", 0, 10);
    }

    @Test
    @DisplayName("contarDatos: Obtiene el total de registros para la paginación principal")
    void testContarDatos() {
        when(empleadoDAO.countByNombre("")).thenReturn(8L);

        assertEquals(8, model.contarDatos());
        verify(empleadoDAO).countByNombre("");
    }

    @Test
    @DisplayName("completarEmpleado: Retorna acotado a 10 resultados para autocompletado")
    void testCompletarEmpleado() {
        when(empleadoDAO.findByNombre("juan", 10)).thenReturn(List.of(new Empleado()));

        List<Empleado> resultado = model.completarEmpleado("juan");

        assertFalse(resultado.isEmpty());
        verify(empleadoDAO).findByNombre("juan", 10);
    }
}