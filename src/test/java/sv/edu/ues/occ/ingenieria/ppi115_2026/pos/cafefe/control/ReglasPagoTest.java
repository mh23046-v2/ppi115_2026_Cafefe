package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;
import static org.junit.jupiter.api.Assertions.*;

class ReglasPagoTest {

    @ParameterizedTest(name = "\"{0}\" → {1}")
    @CsvSource(value = {"efectivo,EFECTIVO", " Tarjeta ,TARJETA", "TRANSFERENCIA,TRANSFERENCIA", "cheque,NULL", "NULL,NULL"},
            nullValues = "NULL")
    @DisplayName("normalizarTipo acepta mayúsculas/minúsculas y rechaza tipos desconocidos")
    void normalizarTipo(String tipo, String esperado) {
        assertEquals(esperado, ReglasPago.normalizarTipo(tipo));
    }

    @Test
    @DisplayName("Solo tarjeta y transferencia piden referencia")
    void requiereReferencia() {
        assertFalse(ReglasPago.requiereReferencia("EFECTIVO"));
        assertTrue(ReglasPago.requiereReferencia("tarjeta"));
        assertTrue(ReglasPago.requiereReferencia("TRANSFERENCIA"));
        assertFalse(ReglasPago.requiereReferencia(null));
    }

    @Test
    @DisplayName("Un pago es editable solo si es nuevo o PENDIENTE")
    void esEditable() {
        Pago p = new Pago(UUID.randomUUID());
        assertTrue(ReglasPago.esEditable(p));
        p.setEstado("PENDIENTE");
        assertTrue(ReglasPago.esEditable(p));
        p.setEstado("COMPLETADO");
        assertFalse(ReglasPago.esEditable(p));
        p.setEstado("ANULADO");
        assertFalse(ReglasPago.esEditable(p));
        assertFalse(ReglasPago.esEditable(null));
    }

    @Test
    @DisplayName("validarDetalle: tipo, monto positivo con 2 decimales y referencia cuando toca")
    void validarDetalle() {
        assertNotNull(ReglasPago.validarDetalle(null));
        assertTrue(ReglasPago.validarDetalle(detalle("cheque", "5.00", null)).contains("tipo de pago"));
        assertTrue(ReglasPago.validarDetalle(detalle("EFECTIVO", null, null)).contains("mayor que cero"));
        assertTrue(ReglasPago.validarDetalle(detalle("EFECTIVO", "0", null)).contains("mayor que cero"));
        assertTrue(ReglasPago.validarDetalle(detalle("EFECTIVO", "1.555", null)).contains("2 decimales"));
        assertTrue(ReglasPago.validarDetalle(detalle("TARJETA", "5.00", " ")).contains("referencia"));
        assertNull(ReglasPago.validarDetalle(detalle("TARJETA", "5.00", "AUT-123")));
        assertNull(ReglasPago.validarDetalle(detalle("efectivo", "5.50", null)));
    }

    @Test
    @DisplayName("validarCierre: lo pagado debe cubrir el total")
    void noCubreElTotal() {
        String error = ReglasPago.validarCierre(new BigDecimal("10.00"), List.of(detalle("EFECTIVO", "4.00", null)));
        assertTrue(error.contains("faltan $6.00"));
    }

    @Test
    @DisplayName("validarCierre: sin productos o sin detalles no se completa")
    void sinTotalOSinDetalles() {
        assertTrue(ReglasPago.validarCierre(BigDecimal.ZERO, List.of(detalle("EFECTIVO", "1", null))).contains("productos"));
        assertTrue(ReglasPago.validarCierre(null, List.of()).contains("productos"));
        assertTrue(ReglasPago.validarCierre(new BigDecimal("5"), List.of()).contains("al menos un detalle"));
    }

    @Test
    @DisplayName("validarCierre: tarjeta no puede pasar del total (el cambio sale del efectivo)")
    void tarjetaNoPasaDelTotal() {
        String error = ReglasPago.validarCierre(new BigDecimal("10.00"), List.of(detalle("TARJETA", "12.00", "A1")));
        assertTrue(error.contains("cambio solo se entrega del efectivo"));
    }

    @Test
    @DisplayName("Pago mixto: $5 efectivo + $10 tarjeta cubre $14 y entrega $1 de cambio")
    void pagoMixtoConCambio() {
        List<PagoDetalle> detalles = List.of(detalle("EFECTIVO", "5.00", null), detalle("TARJETA", "10.00", "A1"));
        BigDecimal total = new BigDecimal("14.00");

        assertNull(ReglasPago.validarCierre(total, detalles));
        assertEquals(new BigDecimal("1.00"), ReglasPago.cambio(total, detalles));
        assertEquals(new BigDecimal("15.00"), ReglasPago.pagado(detalles));
    }

    @Test
    @DisplayName("Pago exacto: cambio cero")
    void pagoExacto() {
        List<PagoDetalle> detalles = List.of(detalle("TRANSFERENCIA", "8.25", "TX-9"));
        assertNull(ReglasPago.validarCierre(new BigDecimal("8.25"), detalles));
        assertEquals(new BigDecimal("0.00"), ReglasPago.cambio(new BigDecimal("8.25"), detalles));
    }

    private static PagoDetalle detalle(String tipo, String monto, String referencia) {
        PagoDetalle d = new PagoDetalle(UUID.randomUUID());
        d.setTipoPago(tipo);
        d.setMonto(monto == null ? null : new BigDecimal(monto));
        d.setReferenciaExterna(referencia);
        return d;
    }
}
