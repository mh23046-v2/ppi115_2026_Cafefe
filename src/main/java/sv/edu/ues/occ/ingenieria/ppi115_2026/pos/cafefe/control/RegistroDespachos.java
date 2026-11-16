package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
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
 * También recuerda las órdenes PAGADAS POR ADELANTADO (la Caja las cobró
 * antes de que Cocina las despachara). Al pagarse dejan de estar "pendientes"
 * en la BD, pero Cocina todavía tiene que prepararlas: así Cocina las sigue
 * viendo hasta que las despache, y Caja ya no (ya están pagadas).
 *
 * ConcurrentHashMap porque varias peticiones pueden llegar a la vez.
 */
@ApplicationScoped
public class RegistroDespachos {

    static final Duration DURACION = Duration.ofDays(1);

    private final Map<UUID, Instant> despachos = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> pagadasPorAdelantado = new ConcurrentHashMap<>();
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

    /**
     * La Caja cobró la orden. Si Cocina todavía no la despachaba, se recuerda
     * para que siga en su lista. Si ya estaba despachada, no hace falta.
     */
    public void registrarCobro(UUID idOrden) {
        if (idOrden != null && !estaDespachada(idOrden)) {
            pagadasPorAdelantado.put(idOrden, reloj.instant());
        }
    }

    /**
     * Órdenes pagadas por adelantado que Cocina aún no despacha, de la más
     * antigua a la más nueva (según cuándo se pagaron).
     */
    public List<UUID> pagadasSinDespachar() {
        limpiarVencidas();
        return pagadasPorAdelantado.entrySet().stream()
                .filter(e -> !despachos.containsKey(e.getKey()))
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(Map.Entry::getKey)
                .toList();
    }

    /** La orden ya no existe (por ejemplo, la borraron): se deja de recordar. */
    public void olvidar(UUID idOrden) {
        despachos.remove(idOrden);
        pagadasPorAdelantado.remove(idOrden);
    }

    /** Borra las marcas de hace más de un día. */
    void limpiarVencidas() {
        Instant limite = reloj.instant().minus(DURACION);
        despachos.values().removeIf(momento -> momento.isBefore(limite));
        pagadasPorAdelantado.values().removeIf(momento -> momento.isBefore(limite));
    }

    int cantidad() {
        return despachos.size();
    }
}
