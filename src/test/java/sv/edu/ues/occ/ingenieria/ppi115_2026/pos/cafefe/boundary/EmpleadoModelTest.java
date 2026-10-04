package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;

/**
 * 
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class EmpleadoModelTest {

    @Mock
    private EmpleadoDAO empleadoDAO;

    @Mock
    private EmpleadoRolModel empleadoRolModel;

    @InjectMocks
    private EmpleadoModel empleadoModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada del DAO")
    void testGetDAO() {
        assertEquals(empleadoDAO, empleadoModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad")
    void testGetId() {
        UUID id = UUID.randomUUID();

        assertEquals(id, empleadoModel.getId(new Empleado(id)));
    }

    @Test
    @DisplayName("nuevoRegistro crea el empleado con ID y activo por defecto")
    void testNuevoRegistro() {
        Empleado resultado = empleadoModel.nuevoRegistro();

        assertNotNull(resultado.getIdEmpleado());
        assertTrue(resultado.getActivo());
    }

    @Test
    @DisplayName("nombreBean debe retornar el nombre descriptivo para la vista")
    void testNombreBean() {
        assertEquals("Empleado", empleadoModel.nombreBean());
    }

    @Test
    @DisplayName("Al seleccionar un empleado, el detalle de roles pasa a ser el de ese empleado")
    void testSeleccionarEmpleadoActualizaDetalle() {
        Empleado empleado = new Empleado(UUID.randomUUID());
        empleadoModel.setTabActiva(1);

        empleadoModel.setRegistro(empleado);

        verify(empleadoRolModel).setEmpleado(empleado);   // el detalle ahora es de este empleado
        verify(empleadoRolModel).btnCancelarHandler();     // se cierra cualquier formulario de rol abierto
        assertEquals(0, empleadoModel.getTabActiva(), "Vuelve a la pestaña de datos");
    }
}
