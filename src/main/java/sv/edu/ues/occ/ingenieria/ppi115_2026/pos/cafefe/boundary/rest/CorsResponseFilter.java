package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class CorsResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String ORIGEN_PERMITIDO = "*";
    public static final String METODOS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
    public static final int MAX_AGE = 2 * 60 * 60; // Chrome limita a 2 h
    public static final String CABECERAS_PERMITIDAS_BASE = "origin, accept, content-type";
    public static final String CABECERAS_EXPUESTAS
            = "Location, Total-registros, Wrong-Param, App-Error, Entity-Not-Found, Content-Disposition";

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        if (esPreflight(req)) {
            req.abortWith(Response.ok().build());
        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
        MultivaluedMap<String, Object> headers = resp.getHeaders();
        headers.putSingle("Access-Control-Allow-Origin", ORIGEN_PERMITIDO);
        headers.putSingle("Access-Control-Allow-Methods", METODOS);
        headers.putSingle("Access-Control-Allow-Headers", cabecerasPermitidas(req));
        headers.putSingle("Access-Control-Expose-Headers", CABECERAS_EXPUESTAS);
        headers.putSingle("Access-Control-Max-Age", MAX_AGE);
    }

    static boolean esPreflight(ContainerRequestContext req) {
        return "OPTIONS".equalsIgnoreCase(req.getMethod())
                && req.getHeaderString("Access-Control-Request-Method") != null;
    }

    static String cabecerasPermitidas(ContainerRequestContext req) {
        String pedidas = req.getHeaderString("Access-Control-Request-Headers");
        if (pedidas == null || pedidas.isBlank()) {
            return CABECERAS_PERMITIDAS_BASE;
        }
        return pedidas;
    }
}