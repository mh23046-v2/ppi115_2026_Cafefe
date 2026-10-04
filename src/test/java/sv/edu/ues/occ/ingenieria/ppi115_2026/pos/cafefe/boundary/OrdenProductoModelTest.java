package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class OrdenProductoModelTest {

    private static final long DIA = 24L * 60 * 60 * 1000;

    @Mock
    private OrdenProductoDAO ordenProductoDAO;

    @Mock
    private FacesContext facesContext;

    @InjectMocks
    private OrdenProductoModel ordenProductoModel;

    private final Date hoy = new Date();
    private Orden orden;
    private Producto cafe;
    private Producto pie;
    private Descuento platinum;

    @BeforeEach
    void preparar() {
        orden = new Orden(UUID.randomUUID());
        orden.setFechaCreacion(hoy);
        ordenProductoModel.setOrden(orden);
        cafe = producto("Café americano", "2.50");
        pie = producto("Pie de queso", "5.00");
        platinum = descuento("Cliente Platinum", new Date(hoy.getTime() - DIA), new Date(hoy.getTime() + DIA));
    }

    @Test
    @DisplayName("nuevoRegistro crea el detalle con ID y ligado a la orden")
    void testNuevoRegistro() {
        OrdenProducto nuevo = ordenProductoModel.nuevoRegistro();

        assertNotNull(nuevo.getIdOrdenProducto());
        assertSame(orden, nuevo.getIdOrden());
    }

    @Test
    @DisplayName("Sin orden seleccionada la tabla queda vacía y no consulta la BD")
    void testSinOrden() {
        ordenProductoModel.setOrden(null);

        assertTrue(ordenProductoModel.cargarDatos(0, 5).isEmpty());
        assertEquals(0, ordenProductoModel.contarDatos());
        verifyNoInteractions(ordenProductoDAO);
    }

    @Test
    @DisplayName("Seleccionar producto pone el producto y su precio sugerido")
    void testSeleccionarProducto() {
        ordenProductoModel.setRegistro(ordenProductoModel.nuevoRegistro());

        ordenProductoModel.seleccionarProducto(pie);

        assertSame(pie, ordenProductoModel.getRegistro().getIdProducto());
        assertEquals(new BigDecimal("5.00"), ordenProductoModel.getRegistro().getPrecio());
    }

    @Test
    @DisplayName("Crear sin producto: NO guarda y muestra error")
    void testCrearSinProducto() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            ordenProductoModel.btnNuevoHandler();

            ordenProductoModel.btnCrearHandler();

            verify(ordenProductoDAO, never()).create(any());
            assertEquals(FacesMessage.SEVERITY_ERROR, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("Crear con producto: guarda con el DAO")
    void testCrearConProducto() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            ordenProductoModel.btnNuevoHandler();
            OrdenProducto nuevo = ordenProductoModel.getRegistro();
            ordenProductoModel.seleccionarProducto(cafe);

            ordenProductoModel.btnCrearHandler();

            verify(ordenProductoDAO).create(nuevo);
            assertEquals(FacesMessage.SEVERITY_INFO, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("El combo lista todos los descuentos, pero deshabilita los que no están vigentes en la fecha de la orden")
    void testPrepararDescuentosDeshabilitaVencidos() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Descuento vencido = descuento("Navidad", new Date(hoy.getTime() - 10 * DIA), new Date(hoy.getTime() - DIA));
            when(ordenProductoDAO.findDescuentosDeProductosEnOrden(orden.getIdOrden()))
                    .thenReturn(List.of(asignar(platinum, pie, 20), asignar(vencido, cafe, 50)));

            ordenProductoModel.prepararDescuentos();

            assertEquals(2, ordenProductoModel.getOpcionesDescuento().size());
            assertEquals("Cliente Platinum (20)%", ordenProductoModel.getOpcionesDescuento().get(0).getLabel());
            assertFalse(ordenProductoModel.getOpcionesDescuento().get(0).isDisabled());
            assertEquals("Navidad (fuera de vigencia)", ordenProductoModel.getOpcionesDescuento().get(1).getLabel());
            assertTrue(ordenProductoModel.getOpcionesDescuento().get(1).isDisabled());
        }
    }

    @Test
    @DisplayName("Si ningún descuento está vigente, todos salen deshabilitados y se avisa")
    void testPrepararDescuentosNingunoVigente() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Descuento vencido = descuento("Navidad", new Date(hoy.getTime() - 10 * DIA), new Date(hoy.getTime() - DIA));
            when(ordenProductoDAO.findDescuentosDeProductosEnOrden(orden.getIdOrden()))
                    .thenReturn(List.of(asignar(vencido, cafe, 50)));

            ordenProductoModel.prepararDescuentos();

            assertEquals(1, ordenProductoModel.getOpcionesDescuento().size());
            assertTrue(ordenProductoModel.getOpcionesDescuento().get(0).isDisabled());
            assertEquals(FacesMessage.SEVERITY_WARN, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("Si el porcentaje cambia según el producto, la etiqueta dice 'hasta'")
    void testEtiquetaHasta() {
        String etiqueta = ordenProductoModel.etiquetaDescuento(platinum,
                List.of(asignar(platinum, pie, 20), asignar(platinum, cafe, 10)));

        assertEquals("Cliente Platinum (hasta 20)%", etiqueta);
    }

    @Test
    @DisplayName("Sin descuentos vigentes: avisa al usuario")
    void testPrepararDescuentosVacio() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            when(ordenProductoDAO.findDescuentosDeProductosEnOrden(orden.getIdOrden())).thenReturn(List.of());

            ordenProductoModel.prepararDescuentos();

            assertTrue(ordenProductoModel.getOpcionesDescuento().isEmpty());
            assertEquals(FacesMessage.SEVERITY_WARN, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("Aplicar descuento: solo cambia el precio de los productos que lo tienen")
    void testAplicarDescuento() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            OrdenProducto lineaPie = linea(pie);
            OrdenProducto lineaCafe = linea(cafe);
            when(ordenProductoDAO.findDescuentosDeProductosEnOrden(orden.getIdOrden()))
                    .thenReturn(List.of(asignar(platinum, pie, 20)));
            when(ordenProductoDAO.findByIdOrden(orden.getIdOrden())).thenReturn(List.of(lineaPie, lineaCafe));
            // el combo devuelve otra instancia con el mismo ID (como hace el converter)
            ordenProductoModel.setDescuentoSeleccionado(new Descuento(platinum.getIdDescuento()));

            ordenProductoModel.aplicarDescuento();

            assertEquals(new BigDecimal("4.00"), lineaPie.getPrecio());
            assertEquals(new BigDecimal("2.50"), lineaCafe.getPrecio());
            verify(ordenProductoDAO).modify(lineaPie);
            verify(ordenProductoDAO, never()).modify(lineaCafe);
            assertEquals(FacesMessage.SEVERITY_INFO, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("Aplicar el mismo descuento dos veces no descuenta doble")
    void testAplicarDosVeces() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            OrdenProducto lineaPie = linea(pie);
            when(ordenProductoDAO.findDescuentosDeProductosEnOrden(orden.getIdOrden()))
                    .thenReturn(List.of(asignar(platinum, pie, 20)));
            when(ordenProductoDAO.findByIdOrden(orden.getIdOrden())).thenReturn(List.of(lineaPie));

            ordenProductoModel.setDescuentoSeleccionado(platinum);
            ordenProductoModel.aplicarDescuento();
            ordenProductoModel.setDescuentoSeleccionado(platinum);
            ordenProductoModel.aplicarDescuento();

            assertEquals(new BigDecimal("4.00"), lineaPie.getPrecio());
        }
    }

    @Test
    @DisplayName("Orden fuera de la vigencia: el descuento NO se aplica")
    void testAplicarFueraDeVigencia() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            orden.setFechaCreacion(new Date(hoy.getTime() + 5 * DIA)); // después de que venció
            when(ordenProductoDAO.findDescuentosDeProductosEnOrden(orden.getIdOrden()))
                    .thenReturn(List.of(asignar(platinum, pie, 20)));
            ordenProductoModel.setDescuentoSeleccionado(platinum);

            ordenProductoModel.aplicarDescuento();

            verify(ordenProductoDAO, never()).modify(any());
            assertEquals(FacesMessage.SEVERITY_ERROR, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("Aplicar sin elegir descuento: muestra error")
    void testAplicarSinSeleccion() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

            ordenProductoModel.aplicarDescuento();

            verifyNoInteractions(ordenProductoDAO);
            assertEquals(FacesMessage.SEVERITY_ERROR, severidadDelMensaje());
        }
    }

    // ---------------- utilidades ----------------

    private Producto producto(String nombre, String precio) {
        Producto p = new Producto(UUID.randomUUID());
        p.setNombre(nombre);
        p.setActivo(true);
        p.setPrecioSugerido(new BigDecimal(precio));
        return p;
    }

    private Descuento descuento(String nombre, Date desde, Date hasta) {
        TipoDescuento tipo = new TipoDescuento(UUID.randomUUID());
        tipo.setActivo(true);
        tipo.setDescuentoMaximo(100);
        Descuento d = new Descuento(UUID.randomUUID());
        d.setNombre(nombre);
        d.setFechaDesde(desde);
        d.setFechaHasta(hasta);
        d.setIdTipoDescuento(tipo);
        return d;
    }

    private DescuentoProducto asignar(Descuento d, Producto p, int valor) {
        DescuentoProducto dp = new DescuentoProducto(UUID.randomUUID());
        dp.setIdDescuento(d);
        dp.setIdProducto(p);
        dp.setValor(valor);
        return dp;
    }

    private OrdenProducto linea(Producto p) {
        OrdenProducto op = new OrdenProducto(UUID.randomUUID());
        op.setIdOrden(orden);
        op.setIdProducto(p);
        op.setPrecio(p.getPrecioSugerido());
        return op;
    }

    /** Captura el último mensaje que el Model envió a la pantalla y devuelve su severidad. */
    private FacesMessage.Severity severidadDelMensaje() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext, atLeastOnce()).addMessage(isNull(), captor.capture());
        return captor.getValue().getSeverity();
    }
}
