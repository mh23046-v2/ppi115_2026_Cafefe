package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DescuentoProductoModelTest {

    @Mock
    private DescuentoProductoDAO descuentoProductoDAO;

    @Mock
    private DescuentoDAO descuentoDAO;

    @Mock
    private TipoDescuentoDAO tipoDescuentoDAO;

    @Mock
    private Etiquetas etiquetas;

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
        assertTrue(descuentoProductoModel.getOpcionesIdDescuento().isEmpty());
    }

    @Test
    @DisplayName("nombreBean debe retornar 'Descuento de producto'")
    void testNombreBean() {
        assertEquals("Descuento de producto", descuentoProductoModel.nombreBean());
    }

    @Test
    @DisplayName("cargarDatos y contarDatos deben retornar vacío / 0 si producto o idProducto son nulos")
    void testCargarYContarDatosNull() {
        assertTrue(descuentoProductoModel.cargarDatos(0, 10).isEmpty());
        assertEquals(0, descuentoProductoModel.contarDatos());

        descuentoProductoModel.setProducto(new Producto());
        assertTrue(descuentoProductoModel.cargarDatos(0, 10).isEmpty());
        assertEquals(0, descuentoProductoModel.contarDatos());
    }

    @Test
    @DisplayName("cargarDatos y contarDatos delegar a DAO cuando producto es válido")
    void testCargarYContarDatosExitoso() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        descuentoProductoModel.setProducto(producto);

        List<DescuentoProducto> lista = List.of(new DescuentoProducto());
        when(descuentoProductoDAO.findByProducto(producto)).thenReturn(lista);

        List<DescuentoProducto> resultado = descuentoProductoModel.cargarDatos(0, 10);
        int total = descuentoProductoModel.contarDatos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, total);
        verify(descuentoProductoDAO, atLeastOnce()).findByProducto(producto);
    }

    @Test
    @DisplayName("cargarOpciones puebla los tipos de descuento y deshabilita los inactivos")
    void testCargarOpciones() {
        TipoDescuento td1 = new TipoDescuento();
        td1.setNombre("Promoción");
        td1.setActivo(true);

        TipoDescuento td2 = new TipoDescuento();
        td2.setNombre("Liquidación");
        td2.setActivo(false);

        when(tipoDescuentoDAO.findAll()).thenReturn(List.of(td1, td2));

        descuentoProductoModel.cargarOpciones();

        assertNotNull(descuentoProductoModel.getOpcionesTipoDescuento());
        assertEquals(2, descuentoProductoModel.getOpcionesTipoDescuento().size());
        assertFalse(descuentoProductoModel.getOpcionesTipoDescuento().get(0).isDisabled());
        assertTrue(descuentoProductoModel.getOpcionesTipoDescuento().get(1).isDisabled());
    }

    @Test
    @DisplayName("cambioTipoDescuento puebla opcionesDescuento según el tipo seleccionado")
    void testCambioTipoDescuento() {
        TipoDescuento td = new TipoDescuento();
        descuentoProductoModel.setTipoDescuentoSeleccionado(td);

        Descuento d1 = new Descuento();
        when(descuentoDAO.findByIdTipoDescuento(td)).thenReturn(List.of(d1));
        when(etiquetas.descuento(d1)).thenReturn("10% Navideño");

        descuentoProductoModel.cambioTipoDescuento();

        assertNotNull(descuentoProductoModel.getOpcionesIdDescuento());
        assertEquals(1, descuentoProductoModel.getOpcionesIdDescuento().size());
        assertEquals("10% Navideño", descuentoProductoModel.getOpcionesIdDescuento().get(0).getLabel());
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
    @DisplayName("validarFechaDesde lanza ValidatorException si fecha está fuera del rango del Descuento padre")
    void testValidarFechaDesdeError() {
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_MONTH, -5);
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
        td.setDescuentoMaximo(15); // Tipo de dato primitivo / Integer int

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