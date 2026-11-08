package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import jakarta.faces.validator.ValidatorException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@ExtendWith(MockitoExtension.class)
class DescuentoProductoModelTest {

    @Mock
    private DescuentoProductoDAO descuentoProductoDAO;

    @Mock
    private DescuentoDAO descuentoDAO;

    @Mock
    private TipoDescuentoDAO tipoDescuentoDAO;

    @InjectMocks
    private DescuentoProductoModel descuentoProductoModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada de DescuentoProductoDAO")
    void testGetDAO() {
        assertNotNull(descuentoProductoModel.getDAO());
        assertEquals(descuentoProductoDAO, descuentoProductoModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de DescuentoProducto")
    void testGetId() {
        UUID uuid = UUID.randomUUID();
        DescuentoProducto dp = new DescuentoProducto(uuid);

        assertEquals(uuid, descuentoProductoModel.getId(dp));
    }

    @Test
    @DisplayName("nuevoRegistro debe inicializar entidad con UUID, asociar producto y limpiar selecciones")
    void testNuevoRegistro() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        descuentoProductoModel.setProducto(producto);

        DescuentoProducto resultado = descuentoProductoModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdDescuentoProducto());
        assertEquals(producto, resultado.getIdProducto());
        assertNull(descuentoProductoModel.getTipoDescuentoSeleccionado());
        assertNull(descuentoProductoModel.getDescuentoSeleccionado());
    }

    @Test
    @DisplayName("nombreBean debe retornar 'Descuento de producto'")
    void testNombreBean() {
        assertEquals("Descuento de producto", descuentoProductoModel.nombreBean());
    }

    @Test
    @DisplayName("cargarDatos y contarDatos retornan vacío / 0 si producto o idProducto son nulos")
    void testCargarYContarDatosNull() {
        assertTrue(descuentoProductoModel.cargarDatos(0, 10).isEmpty());
        assertEquals(0, descuentoProductoModel.contarDatos());

        descuentoProductoModel.setProducto(new Producto());
        assertTrue(descuentoProductoModel.cargarDatos(0, 10).isEmpty());
        assertEquals(0, descuentoProductoModel.contarDatos());
    }

    @Test
    @DisplayName("cargarDatos y contarDatos delegan a DAO paginado cuando producto es válido")
    void testCargarYContarDatosExitoso() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        descuentoProductoModel.setProducto(producto);

        List<DescuentoProducto> lista = List.of(new DescuentoProducto());
        when(descuentoProductoDAO.findByProducto(producto, 0, 10)).thenReturn(lista);
        when(descuentoProductoDAO.countByProducto(producto)).thenReturn(1);

        List<DescuentoProducto> resultado = descuentoProductoModel.cargarDatos(0, 10);
        int total = descuentoProductoModel.contarDatos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, total);
        verify(descuentoProductoDAO).findByProducto(producto, 0, 10);
        verify(descuentoProductoDAO).countByProducto(producto);
    }

    @Test
    @DisplayName("completarTipoDescuento delega la búsqueda acotada a 10 al DAO")
    void testCompletarTipoDescuento() {
        TipoDescuento td = new TipoDescuento();
        when(tipoDescuentoDAO.buscarPorNombre("Temp", 10)).thenReturn(List.of(td));

        List<TipoDescuento> res = descuentoProductoModel.completarTipoDescuento("Temp");

        assertFalse(res.isEmpty());
        verify(tipoDescuentoDAO).buscarPorNombre("Temp", 10);
    }

    @Test
    @DisplayName("completarDescuentoPorTipo retorna vacío si no hay TipoDescuento seleccionado")
    void testCompletarDescuentoPorTipoSinSeleccion() {
        descuentoProductoModel.setTipoDescuentoSeleccionado(null);
        assertTrue(descuentoProductoModel.completarDescuentoPorTipo("Nav").isEmpty());

        TipoDescuento tdSinId = new TipoDescuento();
        descuentoProductoModel.setTipoDescuentoSeleccionado(tdSinId);
        assertTrue(descuentoProductoModel.completarDescuentoPorTipo("Nav").isEmpty());
    }

    @Test
    @DisplayName("completarDescuentoPorTipo delega la búsqueda al DAO si TipoDescuento es válido")
    void testCompletarDescuentoPorTipoExitoso() {
        TipoDescuento td = new TipoDescuento(UUID.randomUUID());
        descuentoProductoModel.setTipoDescuentoSeleccionado(td);

        Descuento d = new Descuento();
        when(descuentoDAO.buscarActivosPorTipoYNombre(td, "Nav", 10)).thenReturn(List.of(d));

        List<Descuento> res = descuentoProductoModel.completarDescuentoPorTipo("Nav");

        assertFalse(res.isEmpty());
        verify(descuentoDAO).buscarActivosPorTipoYNombre(td, "Nav", 10);
    }

    @Test
    @DisplayName("asignarDescuento asigna descuentoSeleccionado a la entidad en registro")
    void testAsignarDescuento() {
        DescuentoProducto dp = new DescuentoProducto();
        Descuento descuento = new Descuento();

        descuentoProductoModel.setRegistro(dp);
        descuentoProductoModel.setDescuentoSeleccionado(descuento);

        descuentoProductoModel.asignarDescuento();

        assertEquals(descuento, dp.getIdDescuento());
    }

    @Test
    @DisplayName("validarRegistro verifica validación de producto, descuento, fechas y porcentajes permitidos")
    void testValidarRegistro() {
        DescuentoProducto dp = new DescuentoProducto(UUID.randomUUID());

        // 1. Producto nulo
        assertNotNull(descuentoProductoModel.validarRegistro(dp));

        // 2. Descuento nulo
        Producto producto = new Producto(UUID.randomUUID());
        dp.setIdProducto(producto);
        assertNotNull(descuentoProductoModel.validarRegistro(dp));

        // 3. Configurar descuento y fechaInicio obligatoria
        Descuento d = new Descuento(UUID.randomUUID());
        dp.setIdDescuento(d);

        dp.setFechaDesde(null);
        assertNotNull(descuentoProductoModel.validarRegistro(dp));

        // 4. Porcentaje superado respecto al TipoDescuento
        TipoDescuento td = new TipoDescuento();
        td.setDescuentoMaximo(20);
        d.setIdTipoDescuento(td);

        dp.setFechaDesde(new Date());
        dp.setValor(30); // 30% sobrepasa el 20% máximo
        String errPorcentaje = descuentoProductoModel.validarRegistro(dp);
        assertNotNull(errPorcentaje);
        assertTrue(errPorcentaje.contains("supera el máximo"));

        // 5. Válido
        dp.setValor(15);
        assertNull(descuentoProductoModel.validarRegistro(dp));
    }

    @Test
    @DisplayName("validarFechaDesde lanza ValidatorException si fecha está fuera del rango del Descuento padre")
    void testValidarFechaDesdeError() {
        Calendar cal = Calendar.getInstance();
        Date fechaDesdePadre = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 10);
        Date fechaHastaPadre = cal.getTime();

        Descuento padre = new Descuento();
        padre.setFechaDesde(fechaDesdePadre);
        padre.setFechaHasta(fechaHastaPadre);

        DescuentoProducto dp = new DescuentoProducto();
        dp.setIdDescuento(padre);
        descuentoProductoModel.setRegistro(dp);

        // Caso 1: Anterior a la fecha inicial
        cal.setTime(fechaDesdePadre);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date fechaInvalidaAnterior = cal.getTime();

        assertThrows(ValidatorException.class, () ->
            descuentoProductoModel.validarFechaDesde(null, null, fechaInvalidaAnterior)
        );

        // Caso 2: Posterior a la fecha final
        cal.setTime(fechaHastaPadre);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date fechaInvalidaPosterior = cal.getTime();

        assertThrows(ValidatorException.class, () ->
            descuentoProductoModel.validarFechaDesde(null, null, fechaInvalidaPosterior)
        );
    }

    @Test
    @DisplayName("validarFechaHasta lanza ValidatorException si es requerida/nula o inconsistente")
    void testValidarFechaHastaError() {
        Descuento padre = new Descuento();
        padre.setFechaHasta(new Date());

        DescuentoProducto dp = new DescuentoProducto();
        dp.setIdDescuento(padre);
        descuentoProductoModel.setRegistro(dp);

        // Caso 1: Requerida pero nula
        assertThrows(ValidatorException.class, () ->
            descuentoProductoModel.validarFechaHasta(null, null, null)
        );

        // Caso 2: Anterior a la fecha de inicio del registro
        Calendar cal = Calendar.getInstance();
        dp.setFechaDesde(cal.getTime());

        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date fechaHastaInvalida = cal.getTime();

        assertThrows(ValidatorException.class, () ->
            descuentoProductoModel.validarFechaHasta(null, null, fechaHastaInvalida)
        );
    }

    @Test
    @DisplayName("validarPorcentaje lanza ValidatorException si sobrepasa el límite del TipoDescuento")
    void testValidarPorcentajeExcedido() {
        TipoDescuento td = new TipoDescuento();
        td.setNombre("Descuento Empleado");
        td.setDescuentoMaximo(15);

        Descuento d = new Descuento();
        d.setIdTipoDescuento(td);

        DescuentoProducto dp = new DescuentoProducto();
        dp.setIdDescuento(d);
        descuentoProductoModel.setRegistro(dp);

        // Intenta validar un 20% (sobrepasa el 15% máximo)
        assertThrows(ValidatorException.class, () ->
            descuentoProductoModel.validarPorcentaje(null, null, "20.0")
        );
    }

    @Test
    @DisplayName("validarPorcentaje pasa con éxito si está dentro del rango permitido")
    void testValidarPorcentajeExitoso() {
        TipoDescuento td = new TipoDescuento();
        td.setNombre("Descuento Empleado");
        td.setDescuentoMaximo(15);

        Descuento d = new Descuento();
        d.setIdTipoDescuento(td);

        descuentoProductoModel.setDescuentoSeleccionado(d);

        assertDoesNotThrow(() ->
            descuentoProductoModel.validarPorcentaje(null, null, "10.0")
        );
    }
}