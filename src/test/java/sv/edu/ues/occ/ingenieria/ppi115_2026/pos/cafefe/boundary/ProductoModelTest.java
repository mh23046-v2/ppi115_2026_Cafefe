package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class ProductoModelTest {

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
    @DisplayName("getDAO debe retornar la instancia inyectada del ProductoDAO")
    void testGetDAO() {
        assertNotNull(productoModel.getDAO());
        assertEquals(productoDAO, productoModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad Producto")
    void testGetId() {
        UUID expectedUuid = UUID.randomUUID();
        Producto producto = new Producto();
        producto.setIdProducto(expectedUuid);

        assertEquals(expectedUuid, productoModel.getId(producto));
    }

    @Test
    @DisplayName("nuevoRegistro debe instanciar un Producto con ID no nulo y activo en true")
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
    @DisplayName("registroCambio debe sincronizar el producto seleccionado con los modelos hijos y reiniciar el tabActiva")
    void testRegistroCambio() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());

        productoModel.setRegistro(producto);
        productoModel.registroCambio();

        // Verificaciones para ProductoTipoProductoModel
        verify(productoTipoProductoModel, org.mockito.Mockito.atLeastOnce()).setProducto(producto);
        verify(productoTipoProductoModel, org.mockito.Mockito.atLeastOnce()).btnCancelarHandler();

        // Verificaciones para ProductoCaracteristicaModel
        verify(productoCaracteristicaModel, org.mockito.Mockito.atLeastOnce()).setProducto(producto);
        verify(productoCaracteristicaModel, org.mockito.Mockito.atLeastOnce()).btnCancelarHandler();

        // Verificaciones para DescuentoProductoModel
        verify(descuentoProductoModel, org.mockito.Mockito.atLeastOnce()).setProducto(producto);
        verify(descuentoProductoModel, org.mockito.Mockito.atLeastOnce()).btnCancelarHandler();

        assertEquals(0, productoModel.getTabActiva());
    }

    @Test
    @DisplayName("Getter y Setter de tabActiva deben funcionar adecuadamente")
    void testGetSetTabActiva() {
        productoModel.setTabActiva(2);
        assertEquals(2, productoModel.getTabActiva());
    }

    @Test
    @DisplayName("Getters de los modelos hijos deben retornar sus respectivas instancias inyectadas")
    void testGettersSubModelos() {
        assertEquals(productoTipoProductoModel, productoModel.getProductoTipoProductoModel());
        assertEquals(productoCaracteristicaModel, productoModel.getProductoCaracteristicaModel());
        assertEquals(descuentoProductoModel, productoModel.getDescuentoProductoModel());
    }
}
