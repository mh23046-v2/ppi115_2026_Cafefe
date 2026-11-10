package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.Etiquetas;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.ProductoTipoProductoModel;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

@ExtendWith(MockitoExtension.class)
class ProductoTipoProductoModelTest {

    @Mock
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @Mock
    private TipoProductoDAO tipoProductoDAO;

    @Mock
    private Etiquetas etiquetas;

    @InjectMocks
    private ProductoTipoProductoModel model;

    @Test
    @DisplayName("cargarDatos: Retorna lista vacía si el producto es nulo o no tiene ID")
    void testCargarDatosProductoNulo() {
        model.setProducto(null);
        assertTrue(model.cargarDatos(0, 10).isEmpty());

        model.setProducto(new Producto());
        assertTrue(model.cargarDatos(0, 10).isEmpty());

        verifyNoInteractions(productoTipoProductoDAO);
    }

    @Test
    @DisplayName("cargarDatos: Delega la búsqueda paginada al DAO")
    void testCargarDatosExitoso() {
        Producto p = new Producto(UUID.randomUUID());
        model.setProducto(p);

        when(productoTipoProductoDAO.findByProducto(p, 0, 10)).thenReturn(List.of(new ProductoTipoProducto()));

        List<ProductoTipoProducto> res = model.cargarDatos(0, 10);

        assertFalse(res.isEmpty());
        verify(productoTipoProductoDAO).findByProducto(p, 0, 10);
    }

    @Test
    @DisplayName("contarDatos: Delega el conteo al DAO en lugar de usar .size()")
    void testContarDatos() {
        Producto p = new Producto(UUID.randomUUID());
        model.setProducto(p);

        when(productoTipoProductoDAO.countByProducto(p)).thenReturn(5L);

        assertEquals(5, model.contarDatos());
        verify(productoTipoProductoDAO).countByProducto(p);
    }

    @Test
    @DisplayName("validarRegistro: Valida presencia y estado activo del TipoProducto")
    void testValidarRegistro() {
        ProductoTipoProducto ptp = new ProductoTipoProducto();

        // 1. TipoProducto nulo
        ptp.setIdTipoProducto(null);
        String err1 = model.validarRegistro(ptp);
        assertNotNull(err1);
        assertTrue(err1.contains("obligatorio"));

        // 2. TipoProducto inactivo
        TipoProducto tpInactivo = new TipoProducto(UUID.randomUUID());
        tpInactivo.setActivo(false);
        ptp.setIdTipoProducto(tpInactivo);

        String err2 = model.validarRegistro(ptp);
        assertNotNull(err2);
        assertTrue(err2.contains("inactivo"));

        // 3. Válido
        tpInactivo.setActivo(true);
        assertNull(model.validarRegistro(ptp));
    }
}