package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.util.List;
import java.util.Set;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 * Reglas de la facturación que no necesitan consultar la base (mismo estilo
 * que ReglasOrden): así se prueban solas y las usa cualquier pantalla.
 */
public final class ReglasFactura {

    /** Roles que pueden emitir facturas (en minúscula y sin tildes). */
    public static final Set<String> ROLES_ADMITIDOS = Set.of("cajero");

    public static final String PENDIENTE = "PENDIENTE";
    /** La pone PagoDAO.completar cuando la factura queda saldada (y la quita PagoDAO.anular). */
    public static final String PAGADA = "PAGADA";
    public static final String ANULADA = "ANULADA";

    /** Estados que el usuario puede elegir a mano en la pantalla. */
    public static final List<String> ESTADOS_EDITABLES = List.of(PENDIENTE, ANULADA);

    public static final String CLIENTE_POR_DEFECTO = "Consumidor final";

    private ReglasFactura() {
    }

    public static boolean rolAdmitido(Rol rol) {
        return rol != null && ROLES_ADMITIDOS.contains(ReglasOrden.normalizar(rol.getNombre()));
    }

    /** Puede facturar si la asignación, el empleado y el rol están activos y el rol es Cajero. */
    public static boolean puedeFacturar(EmpleadoRol er) {
        return er != null
                && Boolean.TRUE.equals(er.getActivo())
                && er.getIdEmpleado() != null && Boolean.TRUE.equals(er.getIdEmpleado().getActivo())
                && er.getIdRol() != null && Boolean.TRUE.equals(er.getIdRol().getActivo())
                && rolAdmitido(er.getIdRol());
    }

    public static boolean cajaDisponible(Caja caja) {
        return caja != null && Boolean.TRUE.equals(caja.getActivo());
    }

    /** Solo una factura pendiente admite cambios en sus productos o ser eliminada. */
    public static boolean esEditable(Factura f) {
        return f != null && (f.getEstado() == null || PENDIENTE.equals(f.getEstado()));
    }

    /**
     * ¿Se puede pasar del estado guardado al nuevo? Una factura pagada o anulada
     * ya no cambia de estado: reabrir una anulada podría facturar dos veces
     * productos que ya se facturaron en otra.
     */
    public static boolean cambioDeEstadoPermitido(String guardado, String nuevo) {
        if (nuevo == null) {
            return false;
        }
        if (guardado == null) {
            return PENDIENTE.equals(nuevo);      // una factura nueva siempre nace pendiente
        }
        if (guardado.equals(nuevo)) {
            return true;                         // no cambió
        }
        return PENDIENTE.equals(guardado) && ANULADA.equals(nuevo);
    }

    /** Cliente vacío → "Consumidor final"; quita espacios de los extremos. */
    public static String normalizarCliente(String cliente) {
        return cliente == null || cliente.isBlank() ? CLIENTE_POR_DEFECTO : cliente.trim();
    }
}
