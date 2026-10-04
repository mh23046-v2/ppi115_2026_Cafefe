package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.model.SelectItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class EmpleadoRolModelTest {

    @Mock
    private EmpleadoRolDAO empleadoRolDAO;

    @Mock
    private RolDAO rolDAO;

    @Mock
    private Etiquetas etiquetas;

    @InjectMocks
    private EmpleadoRolModel empleadoRolModel;

    @Test
    @DisplayName("nuevoRegistro asigna el rol al empleado seleccionado y lo deja activo")
    void testNuevoRegistro() {
        Empleado empleado = new Empleado(UUID.randomUUID());
        empleadoRolModel.setEmpleado(empleado);

        EmpleadoRol resultado = empleadoRolModel.nuevoRegistro();

        assertNotNull(resultado.getIdEmpleadoRol());
        assertSame(empleado, resultado.getIdEmpleado());
        assertTrue(resultado.getActivo());
    }

    @Test
    @DisplayName("Sin empleado seleccionado, la tabla queda vacía y no se consulta la BD")
    void testCargarDatosSinEmpleado() {
        assertTrue(empleadoRolModel.cargarDatos(0, 10).isEmpty());
        assertEquals(0, empleadoRolModel.contarDatos());

        verifyNoInteractions(empleadoRolDAO);
    }

    @Test
    @DisplayName("Con empleado seleccionado, solo carga los roles de ese empleado")
    void testCargarDatosConEmpleado() {
        Empleado empleado = new Empleado(UUID.randomUUID());
        empleadoRolModel.setEmpleado(empleado);
        List<EmpleadoRol> esperados = Collections.singletonList(new EmpleadoRol(UUID.randomUUID()));
        when(empleadoRolDAO.findByIdEmpleado(empleado.getIdEmpleado(), 0, 10)).thenReturn(esperados);
        when(empleadoRolDAO.countByIdEmpleado(empleado.getIdEmpleado())).thenReturn(1L);

        assertEquals(esperados, empleadoRolModel.cargarDatos(0, 10));
        assertEquals(1, empleadoRolModel.contarDatos());
    }

    @Test
    @DisplayName("En el combo de roles, los roles inactivos aparecen deshabilitados")
    void testCargarOpcionesSoloRolesActivosSeleccionables() {
        Rol activo = new Rol(UUID.randomUUID());
        activo.setActivo(true);
        Rol inactivo = new Rol(UUID.randomUUID());
        inactivo.setActivo(false);
        when(rolDAO.findAll()).thenReturn(Arrays.asList(activo, inactivo));
        when(etiquetas.rol(any())).thenReturn("rol");

        empleadoRolModel.cargarOpciones();

        List<SelectItem> opciones = empleadoRolModel.getOpcionesIdRol();
        assertEquals(2, opciones.size());
        assertFalse(opciones.get(0).isDisabled(), "Un rol activo se puede elegir");
        assertTrue(opciones.get(1).isDisabled(), "Un rol inactivo no se puede elegir");
    }

    @Test
    @DisplayName("nombreBean y getId funcionan para la vista y la tabla")
    void testNombreBeanYGetId() {
        UUID id = UUID.randomUUID();

        assertEquals("Rol de Empleado", empleadoRolModel.nombreBean());
        assertEquals(id, empleadoRolModel.getId(new EmpleadoRol(id)));
    }
}
