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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class TipoProductoModelTest {
    
    @Mock
    private TipoProductoDAO tipoProductoDAO;
 
    @Mock
    private FacesContext facesContext;
 
    @InjectMocks
    private TipoProductoModel tipoProductoModel;
 
    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada del DAO")
    void testGetDAO() {
        assertEquals(tipoProductoDAO, tipoProductoModel.getDAO());
    }
 
    @Test
    @DisplayName("getId debe retornar el UUID de la entidad")
    void testGetId() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, tipoProductoModel.getId(new TipoProducto(id)));
    }
 
    @Test
    @DisplayName("nuevoRegistro crea la entidad con ID y activa por defecto")
    void testNuevoRegistro() {
        TipoProducto resultado = tipoProductoModel.nuevoRegistro();
 
        assertNotNull(resultado.getIdTipoProducto());
        assertTrue(resultado.getActivo());
    }
 
    @Test
    @DisplayName("nombreBean debe retornar el nombre descriptivo para la vista")
    void testNombreBean() {
        assertEquals("Tipo de Producto", tipoProductoModel.nombreBean());
    }
 
    @Test
    @DisplayName("Crear (caso feliz): guarda con el DAO, muestra éxito y vuelve al listado")
    void testCrearExitoso() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            tipoProductoModel.btnNuevoHandler();
            TipoProducto nuevo = tipoProductoModel.getRegistro();
            nuevo.setNombre("Bebidas calientes");
 
            tipoProductoModel.btnCrearHandler();
 
            verify(tipoProductoDAO).create(nuevo);
            assertEquals(ESTADO_CRUD.NINGUNO, tipoProductoModel.getEstado());
            assertNull(tipoProductoModel.getRegistro());
            assertEquals(FacesMessage.SEVERITY_INFO, severidadDelMensaje());
        }
    }
 
    @Test
    @DisplayName("Crear con error de BD: muestra error y conserva lo escrito en el formulario")
    void testCrearConError() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            tipoProductoModel.btnNuevoHandler();
            TipoProducto nuevo = tipoProductoModel.getRegistro();
            doThrow(new RuntimeException("llave duplicada")).when(tipoProductoDAO).create(any());
 
            tipoProductoModel.btnCrearHandler();
 
            assertEquals(ESTADO_CRUD.CREAR, tipoProductoModel.getEstado());
            assertSame(nuevo, tipoProductoModel.getRegistro());
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
