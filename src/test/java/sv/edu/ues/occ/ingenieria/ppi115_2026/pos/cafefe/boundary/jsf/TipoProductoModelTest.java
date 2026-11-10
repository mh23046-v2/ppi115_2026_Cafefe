package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.TipoProductoModel;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

@ExtendWith(MockitoExtension.class)
class TipoProductoModelTest {

    @Mock
    private TipoProductoDAO tipoProductoDAO;

    @InjectMocks
    private TipoProductoModel tipoProductoModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada del DAO")
    void testGetDAO() {
        assertNotNull(tipoProductoModel.getDAO());
        assertEquals(tipoProductoDAO, tipoProductoModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad")
    void testGetId() {
        UUID expectedUuid = UUID.randomUUID();
        TipoProducto entidad = new TipoProducto(expectedUuid);

        assertEquals(expectedUuid, tipoProductoModel.getId(entidad));
    }

    @Test
    @DisplayName("nuevoRegistro debe inicializar entidad con ID no nulo y activo en true")
    void testNuevoRegistro() {
        TipoProducto resultado = tipoProductoModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdTipoProducto());
        assertTrue(resultado.getActivo());
    }

    @Test
    @DisplayName("nombreBean debe retornar 'Tipo de Producto'")
    void testNombreBean() {
        assertEquals("Tipo de Producto", tipoProductoModel.nombreBean());
    }

    @Test
    @DisplayName("validarRegistro rechaza nombres nulos o en blanco")
    void testValidarRegistroNombreInvalido() {
        TipoProducto tp = new TipoProducto(UUID.randomUUID());

        // 1. Nombre nulo
        tp.setNombre(null);
        String err1 = tipoProductoModel.validarRegistro(tp);
        assertNotNull(err1);
        assertTrue(err1.contains("obligatorio"));

        // 2. Nombre en blanco
        tp.setNombre("   ");
        String err2 = tipoProductoModel.validarRegistro(tp);
        assertNotNull(err2);
        assertTrue(err2.contains("obligatorio"));
    }

    @Test
    @DisplayName("validarRegistro retorna null cuando el tipo de producto es válido")
    void testValidarRegistroExitoso() {
        TipoProducto tp = new TipoProducto(UUID.randomUUID());
        tp.setNombre("Bebida Caliente");

        assertNull(tipoProductoModel.validarRegistro(tp));
    }
}