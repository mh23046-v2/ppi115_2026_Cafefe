package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class ProductoCaracteristicaModelTest {

    @Mock
    private ProductoCaracteristicaDAO productoCaracteristicaDAO;

    @Mock
    private CaracteristicaDAO caracteristicaDAO;

    @Mock
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    @Mock
    private Etiquetas etiquetas;

    @InjectMocks
    private ProductoCaracteristicaModel productoCaracteristicaModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada de ProductoCaracteristicaDAO")
    void testGetDAO() {
        assertNotNull(productoCaracteristicaModel.getDAO());
        assertEquals(productoCaracteristicaDAO, productoCaracteristicaModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de ProductoCaracteristica")
    void testGetId() {
        UUID uuid = UUID.randomUUID();
        ProductoCaracteristica pc = new ProductoCaracteristica(uuid);

        assertEquals(uuid, productoCaracteristicaModel.getId(pc));
    }

    @Test
    @DisplayName("nuevoRegistro debe inicializar la entidad asociándole el producto y reiniciando filtros")
    void testNuevoRegistro() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        productoCaracteristicaModel.setProducto(producto);

        ProductoCaracteristica resultado = productoCaracteristicaModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdProductoCaracteristica());
        assertEquals(producto, resultado.getIdProducto());
        assertNull(productoCaracteristicaModel.getTipoCaracteristicaSeleccionado());
        assertTrue(productoCaracteristicaModel.getOpcionesCaracteristica().isEmpty());
    }

    @Test
    @DisplayName("nombreBean debe retornar 'Característica de Producto'")
    void testNombreBean() {
        assertEquals("Característica de Producto", productoCaracteristicaModel.nombreBean());
    }

    @Test
    @DisplayName("cargarDatos y contarDatos deben retornar vacío / 0 si producto o idProducto son nulos")
    void testCargarYContarDatosNull() {
        assertTrue(productoCaracteristicaModel.cargarDatos(0, 10).isEmpty());
        assertEquals(0, productoCaracteristicaModel.contarDatos());

        productoCaracteristicaModel.setProducto(new Producto());
        assertTrue(productoCaracteristicaModel.cargarDatos(0, 10).isEmpty());
        assertEquals(0, productoCaracteristicaModel.contarDatos());
    }

    @Test
    @DisplayName("cargarDatos y contarDatos deben delegar a DAO si producto es válido")
    void testCargarYContarDatosExitoso() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        productoCaracteristicaModel.setProducto(producto);

        List<ProductoCaracteristica> lista = List.of(new ProductoCaracteristica());
        when(productoCaracteristicaDAO.findByProducto(producto)).thenReturn(lista);

        List<ProductoCaracteristica> resultado = productoCaracteristicaModel.cargarDatos(0, 10);
        int total = productoCaracteristicaModel.contarDatos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, total);

        verify(productoCaracteristicaDAO, org.mockito.Mockito.times(2)).findByProducto(producto);
    }

    @Test
    @DisplayName("cargarOpciones puebla opcionesTipoCaracteristica deshabilitando inactivos")
    void testCargarOpciones() {
        TipoCaracteristica tc1 = new TipoCaracteristica();
        tc1.setNombre("Tamaño");
        tc1.setActivo(true);

        TipoCaracteristica tc2 = new TipoCaracteristica();
        tc2.setNombre("Temperatura");
        tc2.setActivo(false);

        when(tipoCaracteristicaDAO.findAll()).thenReturn(List.of(tc1, tc2));

        productoCaracteristicaModel.cargarOpciones();

        assertNotNull(productoCaracteristicaModel.getOpcionesTipoCaracteristica());
        assertEquals(2, productoCaracteristicaModel.getOpcionesTipoCaracteristica().size());
        assertFalse(productoCaracteristicaModel.getOpcionesTipoCaracteristica().get(0).isDisabled());
        assertTrue(productoCaracteristicaModel.getOpcionesTipoCaracteristica().get(1).isDisabled());
    }

    @Test
    @DisplayName("cambioTipoCaracteristica llena opcionesCaracteristica según el tipo seleccionado")
    void testCambioTipoCaracteristica() {
        TipoCaracteristica tc = new TipoCaracteristica();
        productoCaracteristicaModel.setTipoCaracteristicaSeleccionado(tc);

        Caracteristica c1 = new Caracteristica();
        c1.setNombre("Grande");
        c1.setActivo(true);

        when(caracteristicaDAO.findByIdTipoCaracteristica(tc)).thenReturn(List.of(c1));

        productoCaracteristicaModel.cambioTipoCaracteristica();

        assertNotNull(productoCaracteristicaModel.getOpcionesCaracteristica());
        assertEquals(1, productoCaracteristicaModel.getOpcionesCaracteristica().size());
        assertEquals("Grande", productoCaracteristicaModel.getOpcionesCaracteristica().get(0).getLabel());
    }

    @Test
    @DisplayName("getRegexCaracteristica retorna la expresión regular configurada o '.*' si es nula/inválida")
    void testGetRegexCaracteristica() {
        // Caso 1: Sin registro o características asocadas -> '.*'
        assertEquals(".*", productoCaracteristicaModel.getRegexCaracteristica());

        // Caso 2: Con expresión regular válida
        TipoTipoCaracteristica regexMock = new TipoTipoCaracteristica();
        regexMock.setExpresionRegular("^[0-9]+$");

        Caracteristica c = new Caracteristica();
        c.setIdTipoCaracteristica(regexMock);

        ProductoCaracteristica pc = new ProductoCaracteristica();
        pc.setIdCaracteristica(c);

        productoCaracteristicaModel.setRegistro(pc);

        assertEquals("^[0-9]+$", productoCaracteristicaModel.getRegexCaracteristica());
    }

    // Helper interno para estructurar la entidad TipoCaracteristica con expresionRegular
    private static class TipoTipoCaracteristica extends TipoCaracteristica {

        private String regex;

        @Override
        public String getExpresionRegular() {
            return regex;
        }

        @Override
        public void setExpresionRegular(String expresionRegular) {
            this.regex = expresionRegular;
        }
    }
}
