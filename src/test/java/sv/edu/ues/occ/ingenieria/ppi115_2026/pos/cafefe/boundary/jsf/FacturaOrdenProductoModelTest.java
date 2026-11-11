package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.math.BigDecimal;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.FacturaOrdenProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FacturaOrdenProductoModelTest {

    @Mock
    private FacturaOrdenProductoDAO facturaOrdenProductoDAO;
    @Mock
    private OrdenProductoDAO ordenProductoDAO;
    @Mock
    private OrdenDAO ordenDAO;
    @Mock
    private Etiquetas etiquetas;
    @Mock
    private FacesContext facesContext;

    @InjectMocks
    private FacturaOrdenProductoModel model;

    private Factura factura;
    private OrdenProducto latte;

    @BeforeEach
    void preparar() {
        factura = new Factura(UUID.randomUUID());
        factura.setEstado("PENDIENTE");
        model.setFactura(factura);
        latte = new OrdenProducto(UUID.randomUUID());
        latte.setPrecio(new BigDecimal("2.40"));
    }

    @Test
    @DisplayName("Sin factura guardada la tabla queda vacía y no consulta la base")
    void testSinFactura() {
        model.setFactura(null);
        assertTrue(model.cargarDatos(0, 5).isEmpty());
        assertEquals(0, model.contarDatos());
        assertFalse(model.isEditable());
        verifyNoInteractions(facturaOrdenProductoDAO);
    }

    @Test
    @DisplayName("La tabla pagina y cuenta en la base")
    void testPaginacion() {
        when(facturaOrdenProductoDAO.countByFactura(factura)).thenReturn(7);
        model.cargarDatos(5, 5);
        assertEquals(7, model.contarDatos());
        verify(facturaOrdenProductoDAO).findByFactura(factura, 5, 5);
    }

    @Test
    @DisplayName("Seleccionar el producto de la orden copia su precio (ya con descuento)")
    void testSeleccionarCopiaPrecio() {
        model.btnNuevoHandler();
        model.seleccionarOrdenProducto(latte);
        assertSame(latte, model.getRegistro().getIdOrdenProducto());
        assertEquals(new BigDecimal("2.40"), model.getRegistro().getPrecio());
        assertSame(factura, model.getRegistro().getIdFactura());
    }

    @Test
    @DisplayName("Agregar un producto sin facturar: se guarda")
    void testCrear() {
        conFaces(() -> {
            model.btnNuevoHandler();
            model.seleccionarOrdenProducto(latte);
            when(facturaOrdenProductoDAO.countVigentesByOrdenProducto(latte)).thenReturn(0);

            model.btnCrearHandler();

            verify(facturaOrdenProductoDAO).create(any(FacturaOrdenProducto.class));
            assertEquals(FacesMessage.SEVERITY_INFO, severidad());
        });
    }

    @Test
    @DisplayName("Un producto que ya está en otra factura vigente NO se factura dos veces")
    void testNoFacturarDoble() {
        conFaces(() -> {
            model.btnNuevoHandler();
            model.seleccionarOrdenProducto(latte);
            when(facturaOrdenProductoDAO.countVigentesByOrdenProducto(latte)).thenReturn(1);

            model.btnCrearHandler();

            verify(facturaOrdenProductoDAO, never()).create(any());
            assertEquals(FacesMessage.SEVERITY_ERROR, severidad());
        });
    }

    @Test
    @DisplayName("Una factura ANULADA o PAGADA no admite agregar ni quitar productos")
    void testFacturaCerrada() {
        conFaces(() -> {
            factura.setEstado("PAGADA");
            assertFalse(model.isEditable());
            model.btnNuevoHandler();
            model.seleccionarOrdenProducto(latte);
            model.btnCrearHandler();
            verify(facturaOrdenProductoDAO, never()).create(any());

            model.setRegistro(new FacturaOrdenProducto(UUID.randomUUID()));
            model.btnEliminarHandler();
            verify(facturaOrdenProductoDAO, never()).delete(any());
        });
    }

    @Test
    @DisplayName("Agregar orden completa: delega en el DAO (una transacción) y avisa cuántos se agregaron")
    void testAgregarOrden() {
        conFaces(() -> {
            Orden o = new Orden(UUID.randomUUID());
            when(ordenDAO.findConPendientesDeFacturar(anyInt())).thenReturn(List.of(o));
            model.prepararOrdenes();
            assertEquals(1, model.getOpcionesOrden().size());

            model.setOrdenSeleccionada(o);
            when(facturaOrdenProductoDAO.agregarPendientesDeOrden(factura, o)).thenReturn(3);
            model.agregarOrden();

            verify(facturaOrdenProductoDAO).agregarPendientesDeOrden(factura, o);
            assertNull(model.getOrdenSeleccionada());
            assertEquals(FacesMessage.SEVERITY_INFO, severidad());
        });
    }

    @Test
    @DisplayName("Agregar orden sin elegir una, o sin órdenes pendientes: avisa y no guarda")
    void testAgregarOrdenSinSeleccion() {
        conFaces(() -> {
            when(ordenDAO.findConPendientesDeFacturar(anyInt())).thenReturn(List.of());
            model.prepararOrdenes();
            assertEquals(FacesMessage.SEVERITY_WARN, severidad());

            model.agregarOrden();
            verify(facturaOrdenProductoDAO, never()).agregarPendientesDeOrden(any(), any());
        });
    }

    @Test
    @DisplayName("El total lo calcula la base")
    void testTotal() {
        when(facturaOrdenProductoDAO.totalByFactura(factura)).thenReturn(new BigDecimal("8.90"));
        assertEquals(new BigDecimal("8.90"), model.getTotal());
    }

    // ---------------- utilidades ----------------
    private void conFaces(Runnable prueba) {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            prueba.run();
        }
    }

    private FacesMessage.Severity severidad() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext, atLeastOnce()).addMessage(isNull(), captor.capture());
        return captor.getValue().getSeverity();
    }
}
