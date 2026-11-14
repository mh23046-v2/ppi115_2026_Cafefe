package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.math.BigDecimal;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketPdfTest {

    private static String texto(byte[] pdf) throws Exception {
        PdfReader lector = new PdfReader(pdf);
        try {
            return new PdfTextExtractor(lector).getTextFromPage(1);
        } finally {
            lector.close();
        }
    }

    @Test
    void elTicketLlevaLosDatosDeLaFactura() throws Exception {
        String t = texto(TicketPdf.generar(FacturaResourceTest.ticket("PAGADA")));

        assertTrue(t.contains("Cafefe"));
        assertTrue(t.contains("FA000001"), "número corto de factura");
        assertTrue(t.contains("13/11/2026 16:30"), "fecha en hora de El Salvador");
        assertTrue(t.contains("Luisa"));
        assertTrue(t.contains("Juan Pérez (Cajero)"), "tildes en CP1252");
        assertTrue(t.contains("Latte Vainilla"));
        assertTrue(t.contains("$3.75 c/u"), "precio unitario cuando hay más de uno");
        assertTrue(t.contains("$10.25"));
        assertTrue(t.contains("Tarjeta (AUT-100245)"));
        assertTrue(t.contains("$4.75"), "cambio");
        assertFalse(t.contains("Estado:"), "una factura PAGADA no muestra aviso de estado");
    }

    @Test
    void unaFacturaNoPagadaLoAvisa() throws Exception {
        assertTrue(texto(TicketPdf.generar(FacturaResourceTest.ticket("ANULADA"))).contains("Estado: ANULADA"));
    }

    @Test
    void ayudantes() {
        assertEquals("$2.50", TicketPdf.dinero(new BigDecimal("2.5")));
        assertEquals("$0.00", TicketPdf.dinero(null));
        assertEquals("ABCDEF12", TicketPdf.corto("abcdef12-3456"));
        assertEquals("-", TicketPdf.corto(null));
        assertEquals("Efectivo", TicketPdf.capitalizar("EFECTIVO"));
        assertEquals("", TicketPdf.capitalizar(null));
    }

    @Test
    void cabeEnUnaSolaPaginaAunqueTengaMuchosProductos() throws Exception {
        CobroService.Ticket base = FacturaResourceTest.ticket("PAGADA");
        java.util.List<CobroService.LineaTicket> muchas = new java.util.ArrayList<>();
        for (int i = 0; i < 60; i++) {
            muchas.add(new CobroService.LineaTicket(1, "Producto " + i, BigDecimal.ONE, BigDecimal.ONE));
        }
        CobroService.Ticket largo = new CobroService.Ticket(base.idFactura(), base.fecha(), base.cliente(), base.caja(),
                base.cajero(), base.estado(), muchas, base.pagos(), base.total(), base.pagado(), base.cambio());
        PdfReader lector = new PdfReader(TicketPdf.generar(largo));
        try {
            assertEquals(1, lector.getNumberOfPages());
            assertTrue(lector.getPageSize(1).getHeight() > 600f, "la hoja crece con el contenido");
            assertEquals(TicketPdf.ANCHO, lector.getPageSize(1).getWidth(), 0.01f);
        } finally {
            lector.close();
        }
    }
}
