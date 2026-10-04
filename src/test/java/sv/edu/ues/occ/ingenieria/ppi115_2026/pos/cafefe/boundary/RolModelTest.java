package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class RolModelTest {

    @Mock
    private RolDAO rolDAO;

    @Mock
    private FacesContext facesContext;

    @InjectMocks
    private RolModel rolModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada del DAO")
    void testGetDAO() {
        assertEquals(rolDAO, rolModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad")
    void testGetId() {
        UUID id = UUID.randomUUID();

        assertEquals(id, rolModel.getId(new Rol(id)));
    }

    @Test
    @DisplayName("nuevoRegistro crea el rol con ID y activo por defecto")
    void testNuevoRegistro() {
        Rol resultado = rolModel.nuevoRegistro();

        assertNotNull(resultado.getIdRol());
        assertTrue(resultado.getActivo());
    }

    @Test
    @DisplayName("nombreBean debe retornar el nombre descriptivo para la vista")
    void testNombreBean() {
        assertEquals("Rol", rolModel.nombreBean());
    }

    @Test
    @DisplayName("Crear (caso feliz): guarda con el DAO, muestra éxito y vuelve al listado")
    void testCrearExitoso() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            rolModel.btnNuevoHandler();
            Rol nuevo = rolModel.getRegistro();
            nuevo.setNombre("Camarero");

            rolModel.btnCrearHandler();

            verify(rolDAO).create(nuevo);
            assertEquals(ESTADO_CRUD.NINGUNO, rolModel.getEstado());
            assertNull(rolModel.getRegistro());
            assertEquals(FacesMessage.SEVERITY_INFO, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("Eliminar con error de BD (rol en uso): muestra error y no sale del formulario")
    void testEliminarConError() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Rol existente = new Rol(UUID.randomUUID());
            rolModel.setRegistro(existente);
            doThrow(new RuntimeException("violates foreign key constraint")).when(rolDAO).delete(any());

            rolModel.btnEliminarHandler();

            assertSame(existente, rolModel.getRegistro());
            assertEquals(FacesMessage.SEVERITY_ERROR, severidadDelMensaje());
        }
    }

    /** Captura el mensaje que el Model envió a la pantalla y devuelve su severidad. */
    private FacesMessage.Severity severidadDelMensaje() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        return captor.getValue().getSeverity();
    }
}
