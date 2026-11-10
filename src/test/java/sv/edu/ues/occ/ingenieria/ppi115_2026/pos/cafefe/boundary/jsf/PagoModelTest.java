package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.FacturaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.FacturaOrdenProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.PagoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglaNegocioException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoModelTest {

    @Mock
    private PagoDAO pagoDAO;
    @Mock
    private FacturaDAO facturaDAO;
    @Mock
    private FacturaOrdenProductoDAO facturaOrdenProductoDAO;
    @Mock
    private PagoDetalleModel pagoDetalleModel;
    @Mock
    private Etiquetas etiquetas;
    @Mock
    private FacesContext facesContext;

    @InjectMocks
    private PagoModel model;

    @Test
    @DisplayName("nuevoRegistro: pago con ID y estado PENDIENTE")
    void nuevoRegistro() {
        Pago p = model.nuevoRegistro();
        assertNotNull(p.getIdPago());
        assertEquals("PENDIENTE", p.getEstado());
        assertEquals("Pago", model.nombreBean());
        assertSame(pagoDAO, model.getDAO());
    }

    @Test
    @DisplayName("Crear con factura PENDIENTE sin otro pago: guarda y salta a la pestaña Detalles")
    void crearValido() {
        conFaces(() -> {
            model.btnNuevoHandler();
            model.getRegistro().setIdFactura(factura("PENDIENTE"));

            model.btnCrearHandler();

            verify(pagoDAO).create(any(Pago.class));
            assertEquals(ESTADO_CRUD.MODIFICAR, model.getEstado());
            assertEquals(1, model.getTabActiva());
        });
    }

    @Test
    @DisplayName("Crear: sin factura, con factura no pendiente o con otro pago vigente no guarda")
    void crearInvalido() {
        conFaces(() -> {
            model.btnNuevoHandler();
            model.btnCrearHandler();

            model.getRegistro().setIdFactura(factura("PAGADA"));
            model.btnCrearHandler();

            Factura f = factura("PENDIENTE");
            model.getRegistro().setIdFactura(f);
            when(pagoDAO.countVigentesByFactura(eq(f), any())).thenReturn(1);
            model.btnCrearHandler();

            verify(pagoDAO, never()).create(any());
        });
    }

    @Test
    @DisplayName("Completar: marca la factura PAGADA en pantalla y avisa el cambio")
    void completar() {
        conFaces(() -> {
            Pago p = pago("PENDIENTE");
            model.setRegistro(p);
            when(pagoDAO.completar(p)).thenReturn(new BigDecimal("1.00"));

            model.btnCompletarHandler();

            assertEquals("PAGADA", p.getIdFactura().getEstado());
            FacesMessage m = mensaje();
            assertEquals(FacesMessage.SEVERITY_INFO, m.getSeverity());
            assertTrue(m.getDetail().contains("$1.00"));
        });
    }

    @Test
    @DisplayName("Completar: si una regla falla muestra el motivo y no cambia la factura")
    void completarFalla() {
        conFaces(() -> {
            Pago p = pago("PENDIENTE");
            model.setRegistro(p);
            when(pagoDAO.completar(p)).thenThrow(new ReglaNegocioException("Lo pagado no cubre el total"));

            model.btnCompletarHandler();

            assertEquals("PENDIENTE", p.getIdFactura().getEstado());
            assertEquals("Lo pagado no cubre el total", mensaje().getDetail());
        });
    }

    @Test
    @DisplayName("Anular un pago completado devuelve la factura a PENDIENTE en pantalla")
    void anular() {
        conFaces(() -> {
            Pago p = pago("COMPLETADO");
            p.getIdFactura().setEstado("PAGADA");
            model.setRegistro(p);

            model.btnAnularHandler();

            verify(pagoDAO).anular(p);
            assertEquals("PENDIENTE", p.getIdFactura().getEstado());
        });
    }

    @Test
    @DisplayName("Eliminar: solo un pago PENDIENTE")
    void eliminar() {
        conFaces(() -> {
            Pago completado = pago("COMPLETADO");
            when(pagoDAO.findById(completado.getIdPago())).thenReturn(completado);
            model.setRegistro(completado);
            model.btnEliminarHandler();
            verify(pagoDAO, never()).delete(any());

            Pago pendiente = pago("PENDIENTE");
            when(pagoDAO.findById(pendiente.getIdPago())).thenReturn(pendiente);
            model.setRegistro(pendiente);
            model.btnEliminarHandler();
            verify(pagoDAO).delete(pendiente);
        });
    }

    @Test
    @DisplayName("Saldo = total de la factura - lo pagado; botones según el estado")
    void saldoYBotones() {
        Pago p = pago("PENDIENTE");
        model.setRegistro(p);
        model.estado = ESTADO_CRUD.MODIFICAR;
        when(facturaOrdenProductoDAO.totalByFactura(p.getIdFactura())).thenReturn(new BigDecimal("14.00"));
        when(pagoDetalleModel.getPagado()).thenReturn(new BigDecimal("15.00"));

        assertEquals(new BigDecimal("-1.00"), model.getSaldo());
        assertTrue(model.isPuedeCompletar());
        assertTrue(model.isPuedeAnular());

        p.setEstado("ANULADO");
        assertFalse(model.isPuedeCompletar());
        assertFalse(model.isPuedeAnular());
    }

    @Test
    @DisplayName("completarFactura: pide las facturas por cobrar; si falla devuelve vacío")
    void completarFactura() {
        when(facturaDAO.findPorCobrar("ana", 10)).thenReturn(List.of(factura("PENDIENTE")));
        assertEquals(1, model.completarFactura("ana").size());

        conFaces(() -> {
            when(facturaDAO.findPorCobrar("x", 10)).thenThrow(new IllegalStateException("caída"));
            assertTrue(model.completarFactura("x").isEmpty());
        });
    }

    // ---------------- utilidades ----------------
    private void conFaces(Runnable prueba) {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            prueba.run();
        }
    }

    private FacesMessage mensaje() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext, atLeastOnce()).addMessage(isNull(), captor.capture());
        return captor.getValue();
    }

    private static Factura factura(String estado) {
        Factura f = new Factura(UUID.randomUUID());
        f.setEstado(estado);
        return f;
    }

    private static Pago pago(String estado) {
        Pago p = new Pago(UUID.randomUUID());
        p.setEstado(estado);
        p.setIdFactura(factura("PENDIENTE"));
        return p;
    }
}
