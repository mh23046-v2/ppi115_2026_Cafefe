package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

/**
 *
 * @author hernandez
 */
public final class ReglasOrden {

    /** Roles que pueden tomar órdenes (en minúscula y sin tildes). */
    public static final Set<String> ROLES_ADMITIDOS = Set.of("camarero", "atencion al cliente", "barista");

    private ReglasOrden() {
    }

    /** "Atención al Cliente " → "atencion al cliente" (para comparar nombres sin importar tildes ni mayúsculas). */
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public static boolean rolAdmitido(Rol rol) {
        return rol != null && ROLES_ADMITIDOS.contains(normalizar(rol.getNombre()));
    }

    /**
     * Un empleado puede tomar órdenes si la asignación, el empleado y el rol
     * están activos y el rol es uno de los admitidos.
     */
    public static boolean puedeTomarOrdenes(EmpleadoRol er) {
        return er != null
                && Boolean.TRUE.equals(er.getActivo())
                && er.getIdEmpleado() != null && Boolean.TRUE.equals(er.getIdEmpleado().getActivo())
                && er.getIdRol() != null && Boolean.TRUE.equals(er.getIdRol().getActivo())
                && rolAdmitido(er.getIdRol());
    }

    /**
     * ¿La fecha cae dentro de [desde, hasta]? Ambos extremos cuentan.
     * Un extremo nulo se toma como "sin límite" por ese lado.
     */
    public static boolean dentroDeVigencia(Date desde, Date hasta, Date fecha) {
        if (fecha == null) {
            return false;
        }
        boolean despuesDelInicio = desde == null || !fecha.before(desde);
        boolean antesDelFin = hasta == null || !fecha.after(hasta);
        return despuesDelInicio && antesDelFin;
    }

    /**
     * Un descuento de producto se puede aplicar a una orden si, en la fecha
     * de la orden: el descuento general está vigente (sus dos fechas son
     * obligatorias), la asignación al producto también lo está, y el tipo de
     * descuento está activo.
     */
    public static boolean descuentoAplicable(DescuentoProducto dp, Date fechaOrden) {
        if (dp == null || dp.getIdDescuento() == null || dp.getValor() == null) {
            return false;
        }
        Descuento d = dp.getIdDescuento();
        TipoDescuento tipo = d.getIdTipoDescuento();
        boolean tipoActivo = tipo == null || !Boolean.FALSE.equals(tipo.getActivo());
        return tipoActivo
                && dentroDeVigencia(d.getFechaDesde(), d.getFechaHasta(), fechaOrden)
                && dentroDeVigencia(dp.getFechaDesde(), dp.getFechaHasta(), fechaOrden);
    }

    /**
     * Porcentaje que realmente se aplica: el valor del descuento, sin pasar
     * del máximo de su tipo y siempre entre 0 y 100.
     */
    public static int porcentajeEfectivo(DescuentoProducto dp) {
        int valor = dp.getValor() == null ? 0 : dp.getValor();
        TipoDescuento tipo = dp.getIdDescuento() == null ? null : dp.getIdDescuento().getIdTipoDescuento();
        if (tipo != null && tipo.getDescuentoMaximo() != null) {
            valor = Math.min(valor, tipo.getDescuentoMaximo());
        }
        return Math.max(0, Math.min(100, valor));
    }

    /** precioBase × (1 − porcentaje/100), redondeado a 2 decimales. */
    public static BigDecimal precioConDescuento(BigDecimal precioBase, int porcentaje) {
        if (precioBase == null) {
            return null;
        }
        BigDecimal factor = BigDecimal.valueOf(100L - porcentaje).divide(BigDecimal.valueOf(100));
        return precioBase.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
