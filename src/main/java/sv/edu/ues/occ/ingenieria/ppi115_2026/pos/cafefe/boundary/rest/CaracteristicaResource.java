package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@Path("tipo_caracteristica/{idTipoCaracteristica}/caracteristica")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CaracteristicaResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    private CaracteristicaDAO caracteristicaDAO;

    @GET
    public Response findByTipo(
            @PathParam("idTipoCaracteristica") UUID idTipo,
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max <= 0) {
            return Response.status(422)
                    .header("Wrong-Param", first < 0 ? "first" : "max")
                    .build();
        }
        TipoCaracteristica tipo = new TipoCaracteristica(idTipo);
        List<Caracteristica> registros
                = caracteristicaDAO.findByTipo(tipo, first, Math.min(max, MAX_REGISTROS));
        return Response.ok(registros)
                .header("Total-registros", caracteristicaDAO.countByTipo(tipo))
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(
            @PathParam("idTipoCaracteristica") UUID idTipo,
            @PathParam("id") UUID id) {
        Caracteristica encontrada = caracteristicaDAO.findById(id);
        if (!pertenece(encontrada, idTipo)) {
            return noEncontrado(id);
        }
        return Response.ok(encontrada).build();
    }

    @POST
    public Response crear(
            @PathParam("idTipoCaracteristica") UUID idTipo,
            @Valid Caracteristica registro,
            @Context UriInfo uriInfo) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        registro.setIdCaracteristica(UUID.randomUUID());
        registro.setIdTipoCaracteristica(new TipoCaracteristica(idTipo));
        caracteristicaDAO.create(registro);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                .path(registro.getIdCaracteristica().toString()).build())
                .entity(registro)
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(
            @PathParam("idTipoCaracteristica") UUID idTipo,
            @PathParam("id") UUID id,
            @Valid Caracteristica registro) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        if (!pertenece(caracteristicaDAO.findById(id), idTipo)) {
            return noEncontrado(id);
        }
        registro.setIdCaracteristica(id);
        registro.setIdTipoCaracteristica(new TipoCaracteristica(idTipo));
        return Response.ok(caracteristicaDAO.modify(registro)).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(
            @PathParam("idTipoCaracteristica") UUID idTipo,
            @PathParam("id") UUID id) {
        Caracteristica existente = caracteristicaDAO.findById(id);
        if (!pertenece(existente, idTipo)) {
            return noEncontrado(id);
        }
        caracteristicaDAO.delete(existente);
        return Response.noContent().build();
    }

    private static boolean pertenece(Caracteristica registro, UUID idTipo) {
        return registro != null
                && registro.getIdTipoCaracteristica() != null
                && idTipo.equals(registro.getIdTipoCaracteristica().getIdTipoCaracteristica());
    }

    private static Response noEncontrado(UUID id) {
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id.toString())
                .build();
    }
}