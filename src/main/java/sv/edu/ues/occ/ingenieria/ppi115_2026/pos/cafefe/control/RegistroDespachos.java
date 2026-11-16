package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Órdenes que la cocina ya entregó (botón "Despachar" de la HU3).
 *
 * La base de datos no tiene un estado "despachada" y NO se modifica, así que
 * se guarda en la memoria del servidor: una sola lista para toda la app
 * (@ApplicationScoped), y así la Caja de otra computadora ve lo que despachó
 * la Cocina.
 *
 * Límites, a propósito: si se reinicia Liberty se olvida lo despachado (las
 * órdenes vuelven a "En preparación", pero se pueden cobrar igual), y las
 * marcas de más de 1 día se borran solas para que la lista no crezca.
 *
 * ConcurrentHashMap porque varias peticiones pueden llegar a la vez.
 */
@ApplicationScoped
public class RegistroDespachos {

    static final Duration DURACION = Duration.ofDays(1);

    private final Map<UUID, Instant> despachos = new ConcurrentHashMap<>();
    private Clock reloj = Clock.systemUTC();

    /** Solo para las pruebas: un reloj fijo. */
    void setReloj(Clock reloj) {
        this.reloj = reloj;
    }

    public void despachar(UUID idOrden) {
        despachos.put(idOrden, reloj.instant());
    }

    public void deshacer(UUID idOrden) {
        despachos.remove(idOrden);
    }

    public boolean estaDespachada(UUID idOrden) {
        limpiarVencidas();
        return idOrden != null && despachos.containsKey(idOrden);
    }

    /** Borra las marcas de hace más de un día. */
    void limpiarVencidas() {
        Instant limite = reloj.instant().minus(DURACION);
        despachos.values().removeIf(momento -> momento.isBefore(limite));
    }

    int cantidad() {
        return despachos.size();
    }
}
