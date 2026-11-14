package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import java.util.UUID;

/**
 * Respuestas de error con las convenciones de Peña, para los recursos de
 * cocina y caja: 404 con Entity-Not-Found y 422 con Wrong-Param.
 * El cuerpo y App-Error los arma ErrorMapper.construir, igual que en el
 * resto del REST.
 */
final class Respuestas {

    private Respuestas() {
    }

    static Response noEncontrado(UUID id) {
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id != null ? id.toString() : "null")
                .build();
    }

    static Response invalido(String wrongParam, String mensaje) {
        Response base = ErrorMapper.construir(422, mensaje);
        if (wrongParam == null || wrongParam.isBlank()) {
            return base;
        }
        return Response.fromResponse(base)
                .header("Wrong-Param", ErrorMapper.limpiarCabecera(wrongParam))
                .build();
    }
}
