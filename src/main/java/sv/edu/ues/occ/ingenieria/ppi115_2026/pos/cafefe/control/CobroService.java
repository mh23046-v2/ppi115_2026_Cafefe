package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.ApplicationException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;

/**
 * Lógica del REST de cocina y caja (HU3 y HU4).
 *
 * Es un EJB @Stateless: cada método público corre en UNA transacción, y los
 * DAOs que llama se unen a esa misma transacción. Por eso en cobrar() se
 * guardan la factura, sus productos, el pago y sus detalles juntos: si algo
 * falla, el contenedor revierte todo y no queda nada guardado a medias.
 */
@Stateless
public class CobroService {

    @Inject
    OrdenDAO ordenDAO;

    @Inject
    OrdenProductoDAO ordenProductoDAO;

    @Inject
    CajaDAO cajaDAO;

    @Inject
    EmpleadoRolDAO empleadoRolDAO;

    @Inject
    FacturaDAO facturaDAO;

    @Inject
    FacturaOrdenProductoDAO facturaOrdenProductoDAO;

    @Inject
    PagoDAO pagoDAO;

    @Inject
    PagoDetalleDAO pagoDetalleDAO;

    // ---------------------------------------------------------------- errores

    /** Dato del cuerpo inválido o regla incumplida → 422 con header Wrong-Param. */
    @ApplicationException(rollback = true)
    public static class SolicitudCobroInvalidaException extends ReglaNegocioException {

        private static final long serialVersionUID = 1L;
        private final String wrongParam;

        public SolicitudCobroInvalidaException(String wrongParam, String mensaje) {
            super(mensaje);
            this.wrongParam = wrongParam;
        }

        public String getWrongParam() {
            return wrongParam;
        }
    }

    /** La orden o la factura no existe → 404 con header Entity-Not-Found. */
    @ApplicationException(rollback = true)
    public static class NoEncontradoException extends ReglaNegocioException {

        private static final long serialVersionUID = 1L;
        private final UUID id;

        public NoEncontradoException(UUID id, String mensaje) {
            super(mensaje);
            this.id = id;
        }

        public UUID getId() {
            return id;
        }
    }

    // ---------------------------------------------------------------- cuerpo del POST

    /** Cuerpo de POST orden/{id}/cobro. JSON-B llena los campos públicos. */
    public static class SolicitudCobro {

        public String idCaja;
        public String idEmpleadoRol;
        public String cliente;
        public List<PagoSolicitado> pagos;
    }

    /** Una forma de pago: {"tipoPago": "TARJETA", "monto": 5.00, "referenciaExterna": "AUT-1"}. */
    public static class PagoSolicitado {

        public String tipoPago;
        public BigDecimal monto;
        public String referenciaExterna;
    }

    // ---------------------------------------------------------------- HU3 y HU4: pendientes

    /**
     * Órdenes con productos sin cobrar, cada una con SOLO sus productos
     * pendientes. Dos consultas en total: una para la página de órdenes y otra
     * para todas sus líneas.
     */
    public List<Map<String, Object>> pendientes(int first, int max) {
        List<Orden> ordenes = ordenDAO.findPendientesDeCobro(first, max);
        List<OrdenProducto> lineas = ordenProductoDAO.findPendientesDeOrdenes(ordenes);

        Map<UUID, List<OrdenProducto>> porOrden = new LinkedHashMap<>();
        for (Orden o : ordenes) {
            porOrden.put(o.getIdOrden(), new ArrayList<>());
        }
        for (OrdenProducto op : lineas) {
            List<OrdenProducto> destino = porOrden.get(op.getIdOrden().getIdOrden());
            if (destino != null) {
                destino.add(op);
            }
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Orden o : ordenes) {
            resultado.add(ordenDto(o, porOrden.get(o.getIdOrden())));
        }
        return resultado;
    }

    public int contarPendientes() {
        return ordenDAO.countPendientesDeCobro();
    }

    /** Una sola orden con sus productos pendientes (para refrescarla en la caja). */
    public Map<String, Object> pendiente(UUID idOrden) {
        Orden orden = buscarOrden(idOrden);
        return ordenDto(orden, ordenProductoDAO.findPendientesByOrden(orden));
    }

    static Map<String, Object> ordenDto(Orden o, List<OrdenProducto> lineas) {
        BigDecimal total = BigDecimal.ZERO;
        List<Map<String, Object>> productos = new ArrayList<>();
        for (OrdenProducto op : lineas) {
            BigDecimal precio = ReglasPago.escala(op.getPrecio());
            total = total.add(precio);
            Map<String, Object> linea = new LinkedHashMap<>();
            linea.put("idOrdenProducto", op.getIdOrdenProducto().toString());
            linea.put("idProducto", op.getIdProducto() != null ? op.getIdProducto().getIdProducto().toString() : null);
            linea.put("producto", op.getIdProducto() != null ? op.getIdProducto().getNombre() : null);
            linea.put("precio", precio);
            linea.put("nota", ReglasCobro.notaDeLinea(op.getObservaciones()));
            productos.add(linea);
        }

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("idOrden", o.getIdOrden().toString());
        dto.put("fechaCreacion", o.getFechaCreacion() != null ? o.getFechaCreacion().toInstant().toString() : null);
        dto.put("cliente", ReglasCobro.clienteDeLineas(lineas));
        dto.put("tomadaPor", tomadaPor(o.getIdEmpleadoRol()));
        dto.put("total", ReglasPago.escala(total));
        dto.put("productos", productos);
        return dto;
    }

    /** "Mesero Virtual (Tienda en línea)" o "Ana Gómez (Camarero)". */
    static String tomadaPor(EmpleadoRol er) {
        if (er == null || er.getIdEmpleado() == null) {
            return null;
        }
        String nombre = (er.getIdEmpleado().getNombre() + " "
                + (er.getIdEmpleado().getApellido() != null ? er.getIdEmpleado().getApellido() : "")).trim();
        return er.getIdRol() != null ? nombre + " (" + er.getIdRol().getNombre() + ")" : nombre;
    }

    // ---------------------------------------------------------------- HU4: cobrar

    /**
     * Cobra los productos pendientes de la orden. Primero valida TODO; solo
     * después guarda: Factura PAGADA → sus productos → Pago COMPLETADO → detalles.
     *
     * @return {idFactura, idOrden, cliente, total, pagado, cambio}
     */
    public Map<String, Object> cobrar(UUID idOrden, SolicitudCobro solicitud) {
        if (solicitud == null) {
            throw new SolicitudCobroInvalidaException("registro", "El cuerpo del cobro es obligatorio");
        }
        Orden orden = buscarOrden(idOrden);

        Caja caja = buscarCaja(solicitud.idCaja);
        EmpleadoRol cajero = buscarCajero(solicitud.idEmpleadoRol);
        List<PagoDetalle> detalles = validarPagos(solicitud.pagos);

        List<OrdenProducto> pendientes = ordenProductoDAO.findPendientesByOrden(orden);
        if (pendientes.isEmpty()) {
            throw new SolicitudCobroInvalidaException("idOrden", "La orden no tiene productos pendientes de cobro");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrdenProducto op : pendientes) {
            total = total.add(op.getPrecio());
        }
        String error = ReglasPago.validarCierre(total, detalles);
        if (error != null) {
            throw new SolicitudCobroInvalidaException("pagos", error);
        }

        String cliente = ReglasCobro.clienteParaFactura(solicitud.cliente, ReglasCobro.clienteDeLineas(pendientes));
        if (cliente.length() > ReglasCobro.MAX_CLIENTE) {
            throw new SolicitudCobroInvalidaException("cliente", "El cliente admite hasta 155 caracteres");
        }

        // --- A partir de aquí todo está validado: se guarda.
        Factura factura = new Factura(UUID.randomUUID());
        factura.setIdCaja(caja);
        factura.setIdEmpleadoRol(cajero);
        factura.setCliente(cliente);
        factura.setFechaFacturacion(new Date());
        factura.setEstado(ReglasFactura.PAGADA);
        factura.setObservaciones("Cobro de la orden " + orden.getIdOrden());
        facturaDAO.create(factura);

        for (OrdenProducto op : pendientes) {
            FacturaOrdenProducto fop = new FacturaOrdenProducto(UUID.randomUUID());
            fop.setIdFactura(factura);
            fop.setIdOrdenProducto(op);
            fop.setPrecio(op.getPrecio());
            fop.setObservaciones(op.getObservaciones());
            facturaOrdenProductoDAO.create(fop);
        }

        Pago pago = new Pago(UUID.randomUUID());
        pago.setIdFactura(factura);
        pago.setEstado(ReglasPago.COMPLETADO);
        pagoDAO.create(pago);

        for (PagoDetalle d : detalles) {
            d.setIdPago(pago);
            pagoDetalleDAO.create(d);
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("idFactura", factura.getIdFactura().toString());
        respuesta.put("idOrden", orden.getIdOrden().toString());
        respuesta.put("cliente", cliente);
        respuesta.put("total", ReglasPago.escala(total));
        respuesta.put("pagado", ReglasPago.escala(ReglasPago.pagado(detalles)));
        respuesta.put("cambio", ReglasPago.cambio(total, detalles));
        return respuesta;
    }

    Orden buscarOrden(UUID idOrden) {
        Orden orden = idOrden == null ? null : ordenDAO.findById(idOrden);
        if (orden == null) {
            throw new NoEncontradoException(idOrden, "La orden no existe");
        }
        return orden;
    }

    Caja buscarCaja(String idCaja) {
        UUID id = ReglasCobro.uuidONull(idCaja);
        if (id == null) {
            throw new SolicitudCobroInvalidaException("idCaja", "Seleccione la caja");
        }
        Caja caja = cajaDAO.findById(id);
        if (!ReglasFactura.cajaDisponible(caja)) {
            throw new SolicitudCobroInvalidaException("idCaja", "La caja no existe o está inactiva");
        }
        return caja;
    }

    EmpleadoRol buscarCajero(String idEmpleadoRol) {
        UUID id = ReglasCobro.uuidONull(idEmpleadoRol);
        if (id == null) {
            throw new SolicitudCobroInvalidaException("idEmpleadoRol", "Falta el cajero que cobra");
        }
        EmpleadoRol cajero = empleadoRolDAO.findById(id);
        if (!ReglasFactura.puedeFacturar(cajero)) {
            throw new SolicitudCobroInvalidaException("idEmpleadoRol", "Solo un cajero activo puede cobrar");
        }
        return cajero;
    }

    /** Convierte cada pago del cuerpo en un PagoDetalle (sin guardar) y lo valida con ReglasPago. */
    static List<PagoDetalle> validarPagos(List<PagoSolicitado> pagos) {
        if (pagos == null || pagos.isEmpty()) {
            throw new SolicitudCobroInvalidaException("pagos", "Agregue al menos una forma de pago");
        }
        if (pagos.size() > ReglasCobro.MAX_PAGOS) {
            throw new SolicitudCobroInvalidaException("pagos", "Se admiten como máximo 10 formas de pago");
        }
        List<PagoDetalle> detalles = new ArrayList<>();
        for (int i = 0; i < pagos.size(); i++) {
            PagoSolicitado p = pagos.get(i);
            if (p == null) {
                throw new SolicitudCobroInvalidaException("pagos[" + i + "]", "Forma de pago vacía");
            }
            PagoDetalle d = new PagoDetalle(UUID.randomUUID());
            d.setTipoPago(ReglasPago.normalizarTipo(p.tipoPago));
            d.setMonto(p.monto);
            String referencia = p.referenciaExterna == null || p.referenciaExterna.isBlank() ? null : p.referenciaExterna.trim();
            if (referencia != null && referencia.length() > ReglasCobro.MAX_REFERENCIA) {
                throw new SolicitudCobroInvalidaException("pagos[" + i + "].referenciaExterna", "La referencia admite hasta 100 caracteres");
            }
            d.setReferenciaExterna(referencia);
            d.setObservaciones("");
            String error = ReglasPago.validarDetalle(d);
            if (error != null) {
                throw new SolicitudCobroInvalidaException("pagos[" + i + "]", error);
            }
            detalles.add(d);
        }
        return detalles;
    }

    // ---------------------------------------------------------------- HU4: ticket

    /** Lo que se imprime en el ticket. Lo arma el servicio y lo dibuja TicketPdf. */
    public record Ticket(String idFactura, Instant fecha, String cliente, String caja, String cajero,
            String estado, List<LineaTicket> lineas, List<PagoTicket> pagos,
            BigDecimal total, BigDecimal pagado, BigDecimal cambio) {
    }

    /** Productos iguales con el mismo precio se agrupan: "2 x Latte Vainilla". */
    public record LineaTicket(int cantidad, String producto, BigDecimal precioUnitario, BigDecimal subtotal) {
    }

    public record PagoTicket(String tipoPago, BigDecimal monto, String referencia) {
    }

    public Ticket ticket(UUID idFactura) {
        Factura factura = idFactura == null ? null : facturaDAO.findById(idFactura);
        if (factura == null) {
            throw new NoEncontradoException(idFactura, "La factura no existe");
        }
        List<FacturaOrdenProducto> productos = facturaOrdenProductoDAO.findByFactura(factura, 0, 500);
        List<LineaTicket> lineas = agrupar(productos);
        BigDecimal total = BigDecimal.ZERO;
        for (LineaTicket l : lineas) {
            total = total.add(l.subtotal());
        }

        List<PagoDetalle> detalles = new ArrayList<>();
        for (Pago pago : pagoDAO.findByFactura(factura, 0, 50)) {
            if (ReglasPago.COMPLETADO.equals(pago.getEstado())) {
                detalles.addAll(pagoDetalleDAO.findByPagoOrdenado(pago, 0, 50));
            }
        }
        List<PagoTicket> pagos = new ArrayList<>();
        for (PagoDetalle d : detalles) {
            pagos.add(new PagoTicket(d.getTipoPago(), ReglasPago.escala(d.getMonto()), d.getReferenciaExterna()));
        }

        return new Ticket(
                factura.getIdFactura().toString(),
                factura.getFechaFacturacion() != null ? factura.getFechaFacturacion().toInstant() : null,
                factura.getCliente(),
                factura.getIdCaja() != null ? factura.getIdCaja().getNombre() : null,
                tomadaPor(factura.getIdEmpleadoRol()),
                factura.getEstado(),
                lineas,
                pagos,
                ReglasPago.escala(total),
                ReglasPago.escala(ReglasPago.pagado(detalles)),
                ReglasPago.cambio(total, detalles));
    }

    static List<LineaTicket> agrupar(List<FacturaOrdenProducto> productos) {
        Map<String, LineaTicket> grupos = new LinkedHashMap<>();
        for (FacturaOrdenProducto fop : productos) {
            String nombre = fop.getIdOrdenProducto() != null && fop.getIdOrdenProducto().getIdProducto() != null
                    ? fop.getIdOrdenProducto().getIdProducto().getNombre() : "Producto";
            BigDecimal precio = ReglasPago.escala(fop.getPrecio());
            String clave = nombre + "|" + precio;
            LineaTicket previa = grupos.get(clave);
            int cantidad = previa == null ? 1 : previa.cantidad() + 1;
            grupos.put(clave, new LineaTicket(cantidad, nombre, precio, precio.multiply(BigDecimal.valueOf(cantidad))));
        }
        return new ArrayList<>(grupos.values());
    }
}
