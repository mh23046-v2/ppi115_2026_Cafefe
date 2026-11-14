package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.NoEncontradoException;

/**
 * HU4: ticket de una factura en PDF.
 *
 * GET factura/{id}/ticket → application/pdf
 *
 * Content-Disposition "inline" hace que el navegador lo muestre en lugar de
 * descargarlo; el nombre sugerido se usa si el usuario lo guarda.
 */
@Path("factura")
public class FacturaResource implements Serializable {

    public static final String PDF = "application/pdf";

    @Inject
    CobroService cobroService;

    @GET
    @Path("{id}/ticket")
    @Produces({PDF, "application/json"})
    public Response ticket(@PathParam("id") UUID id) {
        try {
            byte[] pdf = TicketPdf.generar(cobroService.ticket(id));
            return Response.ok(pdf, PDF)
                    .header("Content-Disposition", "inline; filename=\"ticket-" + id + ".pdf\"")
                    .build();
        } catch (NoEncontradoException ex) {
            return Respuestas.noEncontrado(ex.getId());
        }
    }
}
