package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class ProductoTipoProductoModelTest {

    @Mock
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @Mock
    private TipoProductoDAO tipoProductoDAO;

    @Mock
    private Etiquetas etiquetas;

    @InjectMocks
    private ProductoTipoProductoModel productoTipoProductoModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada de ProductoTipoProductoDAO")
    void testGetDAO() {
        assertNotNull(productoTipoProductoModel.getDAO());
        assertEquals(productoTipoProductoDAO, productoTipoProductoModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad ProductoTipoProducto")
    void testGetId() {
        UUID expectedUuid = UUID.randomUUID();
        ProductoTipoProducto ptp = new ProductoTipoProducto(expectedUuid);

        assertEquals(expectedUuid, productoTipoProductoModel.getId(ptp));
    }

    @Test
    @DisplayName("nuevoRegistro debe crear una instancia con ID, fechaCreacion y vincular el producto actual")
    void testNuevoRegistro() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        productoTipoProductoModel.setProducto(producto);

        ProductoTipoProducto resultado = productoTipoProductoModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdProductoTipoProducto());
        assertNotNull(resultado.getFechaCreacion());
        assertEquals(producto, resultado.getProducto());
    }

    @Test
    @DisplayName("nombreBean debe retornar 'Tipo de Producto'")
    void testNombreBean() {
        assertEquals("Tipo de Producto", productoTipoProductoModel.nombreBean());
    }

    @Test
    @DisplayName("cargarDatos y contarDatos deben retornar vacio / cero si producto o idProducto son nulos")
    void testCargarYContarDatosNull() {
        // Sin producto asignado
        assertTrue(productoTipoProductoModel.cargarDatos(0, 10).isEmpty());
        assertEquals(0, productoTipoProductoModel.contarDatos());

        // Con producto con ID nulo
        productoTipoProductoModel.setProducto(new Producto());
        assertTrue(productoTipoProductoModel.cargarDatos(0, 10).isEmpty());
        assertEquals(0, productoTipoProductoModel.contarDatos());
    }

    @Test
    @DisplayName("cargarDatos y contarDatos deben retornar informacion cuando producto es valido")
    void testCargarYContarDatosExitoso() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        
        List<ProductoTipoProducto> lista = List.of(new ProductoTipoProducto());
        when(productoTipoProductoDAO.findByProducto(producto)).thenReturn(lista);

        productoTipoProductoModel.setProducto(producto);

        List<ProductoTipoProducto> resultado = productoTipoProductoModel.cargarDatos(0, 10);
        
        int total = productoTipoProductoModel.contarDatos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, total);

        verify(productoTipoProductoDAO, org.mockito.Mockito.times(3)).findByProducto(producto);
    }

    @Test
    @DisplayName("cargarOpciones excluye tipos de producto ya asignados pero mantiene el seleccionado en edicion")
    void testCargarOpcionesFiltrado() {
        Producto producto = new Producto();
        UUID idProd = UUID.randomUUID();
        producto.setIdProducto(idProd);

        TipoProducto tp1 = new TipoProducto(UUID.randomUUID());
        tp1.setActivo(true);
        TipoProducto tp2 = new TipoProducto(UUID.randomUUID());
        tp2.setActivo(true);

        // Asignamos tp1 al producto
        ProductoTipoProducto ptpAsignado = new ProductoTipoProducto(UUID.randomUUID());
        ptpAsignado.setIdTipoProducto(tp1);

        when(productoTipoProductoDAO.findByProducto(producto)).thenReturn(List.of(ptpAsignado));
        when(tipoProductoDAO.findAll()).thenReturn(List.of(tp1, tp2));
        when(etiquetas.tipoProducto(tp2)).thenReturn("Bebida");

        // Asignamos producto (dispara cargarOpciones)
        productoTipoProductoModel.setProducto(producto);

        // tp1 debe haber sido excluido por estar asignado, solo tp2 debe estar disponible
        assertNotNull(productoTipoProductoModel.getOpcionesTipoProducto());
        assertEquals(1, productoTipoProductoModel.getOpcionesTipoProducto().size());
        assertEquals(tp2, productoTipoProductoModel.getOpcionesTipoProducto().get(0).getValue());
    }
}