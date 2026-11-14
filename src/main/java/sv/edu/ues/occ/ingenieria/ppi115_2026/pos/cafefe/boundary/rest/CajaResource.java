package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CajaDAO;

/**
 * HU4: cajas activas, para que el cajero elija en cuál cobra.
 * Solo lectura: las cajas se administran en la pantalla JSF.
 *
 * GET caja?first=0&max=50 → lista + header Total-registros
 */
@Path("caja")
@Produces(MediaType.APPLICATION_JSON)
public class CajaResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    CajaDAO cajaDAO;

    @GET
    public Response activas(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max <= 0) {
            return Response.status(422)
                    .header("Wrong-Param", first < 0 ? "first" : "max")
                    .build();
        }
        return Response.ok(cajaDAO.findByActivo(true, first, Math.min(max, MAX_REGISTROS)))
                .header("Total-registros", cajaDAO.countByActivo(true))
                .build();
    }
}
