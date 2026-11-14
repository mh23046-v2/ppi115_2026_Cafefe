package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;

/**
 * Reglas del REST de cocina y caja (HU3 y HU4) que no consultan la base.
 * Mismo estilo que ReglasOrden, ReglasFactura y ReglasPago: se prueban solas.
 */
public final class ReglasCobro {

    /** Prefijo que pone OrdenService (POST /orden) en las observaciones de cada línea. */
    public static final String PREFIJO_CLIENTE = "Cliente: ";
    /** Separador entre el cliente y las observaciones del producto: "Cliente: Ana | sin azúcar". */
    public static final String SEPARADOR = " | ";

    public static final int MAX_CLIENTE = 155;   // tamaño de factura.cliente
    public static final int MAX_PAGOS = 10;
    public static final int MAX_REFERENCIA = 100;

    private ReglasCobro() {
    }

    /**
     * Saca el nombre del cliente de las observaciones de una línea.
     * "Cliente: Ana | sin azúcar" → "Ana"; "Mesa 4" → null.
     */
    public static String clienteDesdeObservaciones(String observaciones) {
        if (observaciones == null || !observaciones.startsWith(PREFIJO_CLIENTE)) {
            return null;
        }
        String resto = observaciones.substring(PREFIJO_CLIENTE.length());
        int corte = resto.indexOf(SEPARADOR);
        String cliente = (corte >= 0 ? resto.substring(0, corte) : resto).trim();
        return cliente.isEmpty() ? null : cliente;
    }

    /** El cliente de la orden: el de la primera línea que lo tenga, o null. */
    public static String clienteDeLineas(List<OrdenProducto> lineas) {
        if (lineas == null) {
            return null;
        }
        for (OrdenProducto op : lineas) {
            String cliente = op == null ? null : clienteDesdeObservaciones(op.getObservaciones());
            if (cliente != null) {
                return cliente;
            }
        }
        return null;
    }

    /**
     * Lo que la cocina debe leer de la línea, sin el prefijo del cliente.
     * "Cliente: Ana | sin azúcar" → "sin azúcar"; "Cliente: Ana" → null; "Mesa 4" → "Mesa 4".
     */
    public static String notaDeLinea(String observaciones) {
        if (observaciones == null || observaciones.isBlank()) {
            return null;
        }
        if (!observaciones.startsWith(PREFIJO_CLIENTE)) {
            return observaciones.trim();
        }
        int corte = observaciones.indexOf(SEPARADOR);
        if (corte < 0) {
            return null;
        }
        String nota = observaciones.substring(corte + SEPARADOR.length()).trim();
        return nota.isEmpty() ? null : nota;
    }

    /**
     * Elige el nombre que va en la factura: el que manda el cajero; si no manda
     * ninguno, el de la orden; y si tampoco hay, "Consumidor final".
     */
    public static String clienteParaFactura(String delCajero, String deLaOrden) {
        if (delCajero != null && !delCajero.isBlank()) {
            return delCajero.trim();
        }
        return ReglasFactura.normalizarCliente(deLaOrden);
    }

    /** "dddddddd-..." → UUID; null si viene vacío o mal escrito. */
    public static UUID uuidONull(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(texto.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
