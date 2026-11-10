package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;

/**
 * Reglas del cobro que no necesitan consultar la base (mismo estilo que
 * ReglasOrden y ReglasFactura): así se prueban solas.
 *
 * Un Pago cobra una Factura y se compone de uno o varios detalles
 * (efectivo, tarjeta, transferencia), por ejemplo $5 en efectivo + $10 con tarjeta.
 */
public final class ReglasPago {

    // Estados del pago
    public static final String PENDIENTE = "PENDIENTE";
    public static final String COMPLETADO = "COMPLETADO";
    public static final String ANULADO = "ANULADO";

    // Tipos de pago
    public static final String EFECTIVO = "EFECTIVO";
    public static final String TARJETA = "TARJETA";
    public static final String TRANSFERENCIA = "TRANSFERENCIA";
    public static final List<String> TIPOS_PAGO = List.of(EFECTIVO, TARJETA, TRANSFERENCIA);

    private ReglasPago() {
    }

    /** " tarjeta" → "TARJETA"; devuelve null si no es un tipo admitido. */
    public static String normalizarTipo(String tipo) {
        if (tipo == null) {
            return null;
        }
        String t = tipo.trim().toUpperCase(Locale.ROOT);
        return TIPOS_PAGO.contains(t) ? t : null;
    }

    /** Tarjeta y transferencia necesitan el número de autorización o de la transacción. */
    public static boolean requiereReferencia(String tipo) {
        String t = normalizarTipo(tipo);
        return t != null && !EFECTIVO.equals(t);
    }

    /** Solo un pago pendiente admite cambios en sus detalles o ser eliminado. */
    public static boolean esEditable(Pago p) {
        return p != null && (p.getEstado() == null || PENDIENTE.equals(p.getEstado()));
    }

    /** Valida un detalle; devuelve el mensaje de error o null si está bien. */
    public static String validarDetalle(PagoDetalle d) {
        if (d == null) {
            return "El registro no puede ser nulo";
        }
        if (normalizarTipo(d.getTipoPago()) == null) {
            return "Seleccione un tipo de pago: " + String.join(", ", TIPOS_PAGO);
        }
        if (d.getMonto() == null || d.getMonto().signum() <= 0) {
            return "El monto debe ser mayor que cero";
        }
        if (d.getMonto().stripTrailingZeros().scale() > 2) {
            return "El monto admite como máximo 2 decimales";
        }
        if (requiereReferencia(d.getTipoPago())
                && (d.getReferenciaExterna() == null || d.getReferenciaExterna().isBlank())) {
            return "Para " + normalizarTipo(d.getTipoPago()).toLowerCase(Locale.ROOT)
                    + " indique la referencia (número de autorización o de transacción)";
        }
        return null;
    }

    /**
     * ¿Se puede completar el pago? Reglas:
     * la factura tiene un total mayor que cero, hay al menos un detalle,
     * lo pagado cubre el total, y tarjeta + transferencia no pasan del total
     * (el cambio solo se entrega del efectivo).
     *
     * @return el mensaje de error, o null si se puede completar
     */
    public static String validarCierre(BigDecimal total, List<PagoDetalle> detalles) {
        if (total == null || total.signum() <= 0) {
            return "La factura no tiene productos que cobrar";
        }
        if (detalles == null || detalles.isEmpty()) {
            return "Agregue al menos un detalle de pago";
        }
        BigDecimal pagado = pagado(detalles);
        if (pagado.compareTo(total) < 0) {
            return "Lo pagado ($" + escala(pagado) + ") no cubre el total ($" + escala(total) + "): faltan $"
                    + escala(total.subtract(pagado));
        }
        BigDecimal noEfectivo = pagado.subtract(efectivo(detalles));
        if (noEfectivo.compareTo(total) > 0) {
            return "Tarjeta y transferencia no pueden superar el total; el cambio solo se entrega del efectivo";
        }
        return null;
    }

    /** Cuánto hay que devolver al cliente (nunca negativo). */
    public static BigDecimal cambio(BigDecimal total, List<PagoDetalle> detalles) {
        BigDecimal diferencia = pagado(detalles).subtract(total == null ? BigDecimal.ZERO : total);
        return escala(diferencia.signum() > 0 ? diferencia : BigDecimal.ZERO);
    }

    public static BigDecimal pagado(List<PagoDetalle> detalles) {
        BigDecimal suma = BigDecimal.ZERO;
        if (detalles != null) {
            for (PagoDetalle d : detalles) {
                if (d != null && d.getMonto() != null) {
                    suma = suma.add(d.getMonto());
                }
            }
        }
        return suma;
    }

    static BigDecimal efectivo(List<PagoDetalle> detalles) {
        BigDecimal suma = BigDecimal.ZERO;
        for (PagoDetalle d : detalles) {
            if (d != null && d.getMonto() != null && EFECTIVO.equals(normalizarTipo(d.getTipoPago()))) {
                suma = suma.add(d.getMonto());
            }
        }
        return suma;
    }

    public static BigDecimal escala(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }
}
