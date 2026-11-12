package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@Path("tipo_caracteristica")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoCaracteristicaResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    @GET
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max <= 0) {
            return Response.status(422)
                    .header("Wrong-Param", first < 0 ? "first" : "max")
                    .build();
        }
        List<TipoCaracteristica> registros
                = tipoCaracteristicaDAO.findRange(first, Math.min(max, MAX_REGISTROS));
        return Response.ok(registros)
                .header("Total-registros", tipoCaracteristicaDAO.count())
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") UUID id) {
        if (id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        TipoCaracteristica encontrado = tipoCaracteristicaDAO.findById(id);
        if (encontrado == null) {
            return noEncontrado(id);
        }
        return Response.ok(encontrado).build();
    }

    @POST
    public Response crear(TipoCaracteristica registro, @Context UriInfo uriInfo) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        if (!expresionRegularValida(registro.getExpresionRegular())) {
            return Response.status(422).header("Wrong-Param", "expresionRegular").build();
        }
        registro.setIdTipoCaracteristica(UUID.randomUUID());
        
        try {
            if (registro.getActivo() == null) {
                registro.setActivo(Boolean.TRUE);
            }
        } catch (NoSuchMethodError | Exception ignored) {
            // Ignorar si la entidad no contiene el atributo activo
        }

        tipoCaracteristicaDAO.create(registro);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                .path(registro.getIdTipoCaracteristica().toString()).build())
                .entity(registro)
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(@PathParam("id") UUID id, TipoCaracteristica registro) {
        if (id == null || registro == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        if (!expresionRegularValida(registro.getExpresionRegular())) {
            return Response.status(422).header("Wrong-Param", "expresionRegular").build();
        }
        if (tipoCaracteristicaDAO.findById(id) == null) {
            return noEncontrado(id);
        }
        registro.setIdTipoCaracteristica(id);
        TipoCaracteristica modificado = tipoCaracteristicaDAO.modify(registro);
        return Response.ok(modificado != null ? modificado : registro).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        if (id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        TipoCaracteristica existente = tipoCaracteristicaDAO.findById(id);
        if (existente == null) {
            return noEncontrado(id);
        }
        tipoCaracteristicaDAO.delete(existente);
        return Response.noContent().build();
    }

    static boolean expresionRegularValida(String expresion) {
        if (expresion == null || expresion.isBlank()) {
            return true;
        }
        try {
            Pattern.compile(expresion);
            return true;
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

    private static Response noEncontrado(UUID id) {
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id != null ? id.toString() : "null")
                .build();
    }
}