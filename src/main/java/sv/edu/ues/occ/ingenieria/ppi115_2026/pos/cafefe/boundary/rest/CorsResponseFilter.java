package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

@Provider
public class CorsResponseFilter implements ContainerResponseFilter {

    public static final String METODOS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
    public final static int MAX_AGE = 60 * 60 * 45;
    public final static String DEFAULT_ALLOWED_HEADERS = "origin, accept, content-type, Total-registros";
    public final static String DEFAULT_EXPOSED_HEADERS = "location,info, Total-registros";

    @Override
    public void filter(ContainerRequestContext reqCtx, ContainerResponseContext respCtx) throws IOException {
        MultivaluedMap<String, Object> headers = respCtx.getHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Headers", getCabecerasPermitidas(reqCtx));
        headers.add("Access-Control-Expose-Headers", getCabecerasExpuestas(reqCtx));
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Methods", METODOS);
        headers.add("Access-Control-Max-Age", MAX_AGE);
    }

    String getCabecerasPermitidas(ContainerRequestContext ctx) {
        List<String> headers = ctx.getHeaders().get("Access-Control-Allow-Headers");
        return crearListaCabeceras(headers, DEFAULT_ALLOWED_HEADERS);
    }

    String getCabecerasExpuestas(ContainerRequestContext ctx) {
        List<String> headers = ctx.getHeaders().get("Access-Control-Expose-Headers");
        return crearListaCabeceras(headers, DEFAULT_EXPOSED_HEADERS);
    }

    String crearListaCabeceras(List<String> cabeceras, String cabecerasDefault) {
        if (cabeceras == null || cabeceras.isEmpty()) {
            return cabecerasDefault;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cabeceras.size(); i++) {
            String cabecera = cabeceras.get(i);
            sb.append(cabecera);
            sb.append(",");
        }
        sb.append(cabecerasDefault);
        return sb.toString();
    }
}