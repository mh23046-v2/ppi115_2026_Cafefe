package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

@ExtendWith(MockitoExtension.class)
class ProductoModelTest {

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private ProductoTipoProductoModel productoTipoProductoModel;

    @Mock
    private ProductoCaracteristicaModel productoCaracteristicaModel;

    @Mock
    private DescuentoProductoModel descuentoProductoModel;

    @InjectMocks
    private ProductoModel productoModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada de ProductoDAO")
    void testGetDAO() {
        assertNotNull(productoModel.getDAO());
        assertEquals(productoDAO, productoModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de Producto")
    void testGetId() {
        UUID uuid = UUID.randomUUID();
        Producto p = new Producto();
        p.setIdProducto(uuid);

        assertEquals(uuid, productoModel.getId(p));
    }

    @Test
    @DisplayName("nuevoRegistro debe inicializar la entidad con ID no nulo y activo en true")
    void testNuevoRegistro() {
        Producto resultado = productoModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdProducto());
        assertTrue(resultado.getActivo());
    }

    @Test
    @DisplayName("nombreBean debe retornar 'Producto'")
    void testNombreBean() {
        assertEquals("Producto", productoModel.nombreBean());
    }

    @Test
    @DisplayName("setRegistro sincroniza el producto con los modelos hijos, resetea sus formularios y reinicia la pestaña activa")
    void testSetRegistroSincronizaHijos() {
        // Simular que el usuario estaba en otra pestaña (ej. Descuentos = 3)
        productoModel.setTabActiva(3);
        Producto producto = new Producto(UUID.randomUUID());

        // Al invocar setRegistro, internamente se dispara el gancho registroCambio()
        productoModel.setRegistro(producto);

        // Verificamos que se delegue exactamente una vez a cada modelo hijo
        verify(productoTipoProductoModel, times(1)).setProducto(producto);
        verify(productoTipoProductoModel, times(1)).btnCancelarHandler();

        verify(productoCaracteristicaModel, times(1)).setProducto(producto);
        verify(productoCaracteristicaModel, times(1)).btnCancelarHandler();

        verify(descuentoProductoModel, times(1)).setProducto(producto);
        verify(descuentoProductoModel, times(1)).btnCancelarHandler();

        // Verificamos que la pestaña haya vuelto a 0 (Generalidades)
        assertEquals(0, productoModel.getTabActiva());
    }

    @Test
    @DisplayName("validarRegistro evalúa que el nombre del producto sea obligatorio y no nulo/blanco")
    void testValidarRegistro() {
        Producto p = new Producto();

        // 1. Nombre nulo
        p.setNombre(null);
        String err1 = productoModel.validarRegistro(p);
        assertNotNull(err1);
        assertTrue(err1.contains("obligatorio"));

        // 2. Nombre en blanco
        p.setNombre("   ");
        String err2 = productoModel.validarRegistro(p);
        assertNotNull(err2);
        assertTrue(err2.contains("obligatorio"));

        // 3. Registro válido
        p.setNombre("Espresso Doble");
        assertNull(productoModel.validarRegistro(p));
    }

    @Test
    @DisplayName("Getters y setters de modelos hijos y pestaña activa responden correctamente")
    void testGettersAndSetters() {
        assertEquals(productoTipoProductoModel, productoModel.getProductoTipoProductoModel());
        assertEquals(productoCaracteristicaModel, productoModel.getProductoCaracteristicaModel());
        assertEquals(descuentoProductoModel, productoModel.getDescuentoProductoModel());

        productoModel.setTabActiva(2);
        assertEquals(2, productoModel.getTabActiva());
    }
}