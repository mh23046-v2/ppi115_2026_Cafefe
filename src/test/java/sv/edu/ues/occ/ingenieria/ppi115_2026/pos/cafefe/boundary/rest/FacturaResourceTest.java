package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.LineaTicket;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.NoEncontradoException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.PagoTicket;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.Ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class FacturaResourceTest {

    private CobroService cobroService;
    private FacturaResource resource;

    @BeforeEach
    void setUp() {
        cobroService = Mockito.mock(CobroService.class);
        resource = new FacturaResource();
        resource.cobroService = cobroService;
    }

    static Ticket ticket(String estado) {
        BigDecimal p = new BigDecimal("3.75");
        return new Ticket("fa000001-fafa-fafa-fafa-fafafafafafa", Instant.parse("2026-11-13T22:30:00Z"),
                "Luisa", "Caja Principal", "Juan Pérez (Cajero)", estado,
                List.of(new LineaTicket(2, "Latte Vainilla", p, new BigDecimal("7.50")),
                        new LineaTicket(1, "Brownie", new BigDecimal("2.75"), new BigDecimal("2.75"))),
                List.of(new PagoTicket("TARJETA", new BigDecimal("5.00"), "AUT-100245"),
                        new PagoTicket("EFECTIVO", new BigDecimal("10.00"), null)),
                new BigDecimal("10.25"), new BigDecimal("15.00"), new BigDecimal("4.75"));
    }

    @Test
    void ticketEnPdfInline() {
        UUID id = UUID.randomUUID();
        when(cobroService.ticket(id)).thenReturn(ticket("PAGADA"));

        Response r = resource.ticket(id);

        assertEquals(200, r.getStatus());
        assertEquals("application/pdf", r.getMediaType().toString());
        assertTrue(r.getHeaderString("Content-Disposition").startsWith("inline; filename=\"ticket-" + id));
        byte[] pdf = (byte[]) r.getEntity();
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void facturaQueNoExiste() {
        UUID id = UUID.randomUUID();
        when(cobroService.ticket(id)).thenThrow(new NoEncontradoException(id, "La factura no existe"));
        Response r = resource.ticket(id);
        assertEquals(404, r.getStatus());
        assertEquals(id.toString(), r.getHeaderString("Entity-Not-Found"));
    }
}
