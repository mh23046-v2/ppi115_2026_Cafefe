package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.NoEncontradoException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RegistroDespachos;

/**
 * HU3 (Cocina) y HU4 (Caja): órdenes que tienen productos sin cobrar.
 *
 * GET orden/pendiente?first=0&max=20 → lista + header Total-registros
 * GET orden/pendiente/{id}           → una orden con sus productos pendientes
 * GET    orden/pendiente/pagada        → pagadas por adelantado que Cocina aún no despacha
 * PUT    orden/pendiente/{id}/despacho → la cocina la entregó (204)
 * DELETE orden/pendiente/{id}/despacho → deshacer el despacho (204)
 *
 * Cada orden trae "despachada": true/false. Ese dato NO está en la base de
 * datos: lo guarda RegistroDespachos en la memoria del servidor, para que
 * Cocina y Caja lo vean igual desde cualquier computadora.
 *
 * Una orden deja de estar pendiente cuando la caja cobra todos sus productos.
 * Convive con OrdenResource (@Path("orden")): JAX-RS elige la ruta más
 * específica, así que POST /orden sigue llegando al recurso de Johnny.
 */
@Path("orden/pendiente")
@Produces(MediaType.APPLICATION_JSON)
public class OrdenPendienteResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    CobroService cobroService;

    @Inject
    RegistroDespachos despachos;

    @GET
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("20") int max) {
        if (first < 0 || max <= 0) {
            return Response.status(422)
                    .header("Wrong-Param", first < 0 ? "first" : "max")
                    .build();
        }
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Map<String, Object> orden : cobroService.pendientes(first, Math.min(max, MAX_REGISTROS))) {
            lista.add(conDespacho(orden));
        }
        return Response.ok(lista)
                .header("Total-registros", cobroService.contarPendientes())
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") UUID id) {
        try {
            return Response.ok(conDespacho(cobroService.pendiente(id))).build();
        } catch (NoEncontradoException ex) {
            return Respuestas.noEncontrado(ex.getId());
        }
    }

    /**
     * Órdenes que la Caja cobró por adelantado y que Cocina todavía no despacha,
     * con todos sus productos. Cada una trae "pagada": true y "despachada": false.
     * Va antes de {id} en el código, pero JAX-RS igual prefiere la ruta fija.
     */
    @GET
    @Path("pagada")
    public Response pagadasSinDespachar() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (UUID id : despachos.pagadasSinDespachar()) {
            try {
                Map<String, Object> orden = new LinkedHashMap<>(cobroService.ordenCompleta(id));
                orden.put("despachada", false);
                orden.put("pagada", true);
                lista.add(orden);
            } catch (NoEncontradoException ex) {
                despachos.olvidar(id);      // la orden ya no existe
            }
        }
        return Response.ok(lista).build();
    }

    /** La cocina entregó la orden. 404 si la orden no existe. */
    @PUT
    @Path("{id}/despacho")
    public Response despachar(@PathParam("id") UUID id) {
        try {
            cobroService.pendiente(id);     // solo para comprobar que existe
        } catch (NoEncontradoException ex) {
            return Respuestas.noEncontrado(ex.getId());
        }
        despachos.despachar(id);
        return Response.noContent().build();
    }

    /** "Volver a la lista": la orden regresa a Cocina. Si no estaba despachada, no pasa nada. */
    @DELETE
    @Path("{id}/despacho")
    public Response deshacerDespacho(@PathParam("id") UUID id) {
        despachos.deshacer(id);
        return Response.noContent().build();
    }

    /** Copia de la orden con "despachada" (la original puede ser inmutable). */
    private Map<String, Object> conDespacho(Map<String, Object> orden) {
        Map<String, Object> copia = new LinkedHashMap<>(orden);
        boolean despachada;
        try {
            despachada = despachos.estaDespachada(UUID.fromString(String.valueOf(orden.get("idOrden"))));
        } catch (IllegalArgumentException ex) {
            despachada = false;     // id ausente o mal formado
        }
        copia.put("despachada", despachada);
        return copia;
    }
}
