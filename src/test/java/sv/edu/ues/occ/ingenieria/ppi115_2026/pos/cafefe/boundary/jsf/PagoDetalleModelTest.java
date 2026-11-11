package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.PagoDetalleDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoDetalleModelTest {

    @Mock
    private PagoDetalleDAO pagoDetalleDAO;
    @Mock
    private FacesContext facesContext;

    @InjectMocks
    private PagoDetalleModel model;

    private Pago pago;

    @BeforeEach
    void preparar() {
        pago = new Pago(UUID.randomUUID());
        pago.setEstado("PENDIENTE");
        model.setPago(pago);
    }

    @Test
    @DisplayName("nuevoRegistro: ligado al pago y en EFECTIVO por defecto")
    void nuevoRegistro() {
        PagoDetalle d = model.nuevoRegistro();
        assertNotNull(d.getIdPagoDetalle());
        assertSame(pago, d.getIdPago());
        assertEquals("EFECTIVO", d.getTipoPago());
    }

    @Test
    @DisplayName("Sin pago: tabla vacía y no consulta la base")
    void sinPago() {
        model.setPago(null);
        assertTrue(model.cargarDatos(0, 5).isEmpty());
        assertEquals(0, model.contarDatos());
        assertEquals(BigDecimal.ZERO, model.getPagado());
        verifyNoInteractions(pagoDetalleDAO);
    }

    @Test
    @DisplayName("validarRegistro: normaliza el tipo y quita la referencia del efectivo")
    void validarNormaliza() {
        PagoDetalle d = model.nuevoRegistro();
        d.setTipoPago("efectivo");
        d.setMonto(new BigDecimal("5.00"));
        d.setReferenciaExterna("no aplica");

        assertNull(model.validarRegistro(d));
        assertEquals("EFECTIVO", d.getTipoPago());
        assertNull(d.getReferenciaExterna());
    }

    @Test
    @DisplayName("validarRegistro: tarjeta sin referencia o pago ya completado no se guardan")
    void validarErrores() {
        PagoDetalle d = model.nuevoRegistro();
        d.setTipoPago("TARJETA");
        d.setMonto(new BigDecimal("5.00"));
        assertTrue(model.validarRegistro(d).contains("referencia"));

        d.setReferenciaExterna(" AUT-1 ");
        assertNull(model.validarRegistro(d));
        assertEquals("AUT-1", d.getReferenciaExterna());

        pago.setEstado("COMPLETADO");
        assertTrue(model.validarRegistro(d).contains("ya no admite cambios"));

        d.setIdPago(new Pago());
        assertTrue(model.validarRegistro(d).contains("Guarde primero"));
        assertNotNull(model.validarRegistro(null));
    }

    @Test
    @DisplayName("Eliminar: no se permite si el pago ya no es PENDIENTE")
    void eliminarPagoCerrado() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pago.setEstado("COMPLETADO");
            model.setRegistro(model.nuevoRegistro());

            model.btnEliminarHandler();

            verify(pagoDetalleDAO, never()).delete(any());
            assertFalse(model.isEditable());
        }
    }

    @Test
    @DisplayName("Lista de tipos, referencia según el tipo y total pagado desde el DAO")
    void utilidades() {
        assertEquals(3, model.getTiposPago().size());
        model.setRegistro(model.nuevoRegistro());
        assertFalse(model.isReferenciaRequerida());
        model.getRegistro().setTipoPago("TRANSFERENCIA");
        assertTrue(model.isReferenciaRequerida());
        when(pagoDetalleDAO.totalByPago(pago)).thenReturn(new BigDecimal("9.00"));
        assertEquals(new BigDecimal("9.00"), model.getPagado());
    }
}
