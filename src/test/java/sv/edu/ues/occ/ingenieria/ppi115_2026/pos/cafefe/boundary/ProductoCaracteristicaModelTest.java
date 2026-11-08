package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@ExtendWith(MockitoExtension.class)
class ProductoCaracteristicaModelTest {

    @Mock
    private ProductoCaracteristicaDAO productoCaracteristicaDAO;

    @Mock
    private CaracteristicaDAO caracteristicaDAO;

    @Mock
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

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
    @DisplayName("nuevoRegistro debe inicializar entidad asociándole el producto y reiniciando selecciones")
    void testNuevoRegistro() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        productoCaracteristicaModel.setProducto(producto);

        ProductoCaracteristica resultado = productoCaracteristicaModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdProductoCaracteristica());
        assertEquals(producto, resultado.getIdProducto());
        assertNull(productoCaracteristicaModel.getTipoCaracteristicaSeleccionado());
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
    @DisplayName("cargarDatos y contarDatos deben delegar a DAO paginado cuando producto es válido")
    void testCargarYContarDatosExitoso() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        productoCaracteristicaModel.setProducto(producto);

        List<ProductoCaracteristica> lista = List.of(new ProductoCaracteristica());
        when(productoCaracteristicaDAO.findByProducto(producto, 0, 10)).thenReturn(lista);
        when(productoCaracteristicaDAO.countByProducto(producto)).thenReturn(1);

        List<ProductoCaracteristica> resultado = productoCaracteristicaModel.cargarDatos(0, 10);
        int total = productoCaracteristicaModel.contarDatos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, total);

        verify(productoCaracteristicaDAO).findByProducto(producto, 0, 10);
        verify(productoCaracteristicaDAO).countByProducto(producto);
    }

    @Test
    @DisplayName("completarTipoCaracteristica delega la búsqueda acotada a 10 al DAO")
    void testCompletarTipoCaracteristica() {
        TipoCaracteristica tc = new TipoCaracteristica();
        when(tipoCaracteristicaDAO.buscarPorNombre("Origen", 10)).thenReturn(List.of(tc));

        List<TipoCaracteristica> res = productoCaracteristicaModel.completarTipoCaracteristica("Origen");

        assertFalse(res.isEmpty());
        verify(tipoCaracteristicaDAO).buscarPorNombre("Origen", 10);
    }

    @Test
    @DisplayName("completarCaracteristicaPorTipo retorna vacío si no hay TipoCaracteristica seleccionado")
    void testCompletarCaracteristicaPorTipoSinSeleccion() {
        productoCaracteristicaModel.setTipoCaracteristicaSeleccionado(null);
        assertTrue(productoCaracteristicaModel.completarCaracteristicaPorTipo("Bourbon").isEmpty());

        TipoCaracteristica tcSinId = new TipoCaracteristica();
        productoCaracteristicaModel.setTipoCaracteristicaSeleccionado(tcSinId);
        assertTrue(productoCaracteristicaModel.completarCaracteristicaPorTipo("Bourbon").isEmpty());
    }

    @Test
    @DisplayName("completarCaracteristicaPorTipo delega la búsqueda al DAO si TipoCaracteristica es válido")
    void testCompletarCaracteristicaPorTipoExitoso() {
        TipoCaracteristica tc = new TipoCaracteristica(UUID.randomUUID());
        productoCaracteristicaModel.setTipoCaracteristicaSeleccionado(tc);

        Caracteristica c = new Caracteristica();
        when(caracteristicaDAO.buscarActivasPorTipoYNombre(tc, "Bourbon", 10)).thenReturn(List.of(c));

        List<Caracteristica> res = productoCaracteristicaModel.completarCaracteristicaPorTipo("Bourbon");

        assertFalse(res.isEmpty());
        verify(caracteristicaDAO).buscarActivasPorTipoYNombre(tc, "Bourbon", 10);
    }

    @Test
    @DisplayName("getRegexCaracteristica retorna la expresión regular configurada o '.*' si es nula o vacía")
    void testGetRegexCaracteristica() {
        // Caso 1: Sin registro -> '.*'
        assertEquals(".*", productoCaracteristicaModel.getRegexCaracteristica());

        // Caso 2: Con expresión regular válida
        TipoCaracteristica tc = new TipoCaracteristica();
        tc.setExpresionRegular("^[0-9]+$");

        Caracteristica c = new Caracteristica();
        c.setIdTipoCaracteristica(tc);

        ProductoCaracteristica pc = new ProductoCaracteristica();
        pc.setIdCaracteristica(c);

        productoCaracteristicaModel.setRegistro(pc);

        assertEquals("^[0-9]+$", productoCaracteristicaModel.getRegexCaracteristica());
    }

    @Test
    @DisplayName("validarRegistro evalúa todas las restricciones para la caracteristica de un producto")
    void testValidarRegistro() {
        ProductoCaracteristica pc = new ProductoCaracteristica(UUID.randomUUID());

        // 1. Producto nulo
        assertNotNull(productoCaracteristicaModel.validarRegistro(pc));

        // 2. Caracteristica nula
        Producto producto = new Producto(UUID.randomUUID());
        pc.setIdProducto(producto);
        assertNotNull(productoCaracteristicaModel.validarRegistro(pc));

        // 3. Caracteristica inactiva
        Caracteristica c = new Caracteristica(UUID.randomUUID());
        c.setActivo(false);
        pc.setIdCaracteristica(c);
        String errInactiva = productoCaracteristicaModel.validarRegistro(pc);
        assertNotNull(errInactiva);
        assertTrue(errInactiva.contains("inactiva"));

        // Configurar Caracteristica activa
        c.setActivo(true);

        // 4. Valor nulo o en blanco
        pc.setValor("   ");
        String errValor = productoCaracteristicaModel.validarRegistro(pc);
        assertNotNull(errValor);
        assertTrue(errValor.contains("obligatorio"));

        // 5. Expresión regular no cumplida
        TipoCaracteristica tcRegex = new TipoCaracteristica(UUID.randomUUID());
        tcRegex.setExpresionRegular("^[0-9]+$");
        c.setIdTipoCaracteristica(tcRegex);

        // Sincronizar el registro en el modelo para que getRegexCaracteristica() lea el tipo
        productoCaracteristicaModel.setRegistro(pc);

        pc.setValor("TextoInvalido");
        String errRegex = productoCaracteristicaModel.validarRegistro(pc);
        assertNotNull(errRegex);
        assertTrue(errRegex.contains("regla o patrón"));

        // 6. Registro Válido
        pc.setValor("12345");
        assertNull(productoCaracteristicaModel.validarRegistro(pc));
    }
}
