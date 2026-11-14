package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.LineaTicket;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.NoEncontradoException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.PagoSolicitado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.SolicitudCobro;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.SolicitudCobroInvalidaException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.Ticket;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CobroServiceTest {

    private OrdenDAO ordenDAO;
    private OrdenProductoDAO ordenProductoDAO;
    private CajaDAO cajaDAO;
    private EmpleadoRolDAO empleadoRolDAO;
    private FacturaDAO facturaDAO;
    private FacturaOrdenProductoDAO facturaOrdenProductoDAO;
    private PagoDAO pagoDAO;
    private PagoDetalleDAO pagoDetalleDAO;
    private CobroService service;

    private static final UUID ID_ORDEN = UUID.fromString("ab000001-abab-abab-abab-abababababab");
    private static final UUID ID_CAJA = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID ID_CAJERO = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    private Orden orden;
    private Caja caja;
    private EmpleadoRol cajero;

    @BeforeEach
    void setUp() {
        ordenDAO = Mockito.mock(OrdenDAO.class);
        ordenProductoDAO = Mockito.mock(OrdenProductoDAO.class);
        cajaDAO = Mockito.mock(CajaDAO.class);
        empleadoRolDAO = Mockito.mock(EmpleadoRolDAO.class);
        facturaDAO = Mockito.mock(FacturaDAO.class);
        facturaOrdenProductoDAO = Mockito.mock(FacturaOrdenProductoDAO.class);
        pagoDAO = Mockito.mock(PagoDAO.class);
        pagoDetalleDAO = Mockito.mock(PagoDetalleDAO.class);

        service = new CobroService();
        service.ordenDAO = ordenDAO;
        service.ordenProductoDAO = ordenProductoDAO;
        service.cajaDAO = cajaDAO;
        service.empleadoRolDAO = empleadoRolDAO;
        service.facturaDAO = facturaDAO;
        service.facturaOrdenProductoDAO = facturaOrdenProductoDAO;
        service.pagoDAO = pagoDAO;
        service.pagoDetalleDAO = pagoDetalleDAO;

        orden = new Orden(ID_ORDEN);
        orden.setFechaCreacion(new Date());
        orden.setIdEmpleadoRol(asignacion(UUID.randomUUID(), "Mesero", "Virtual", "Tienda en línea"));
        caja = new Caja(ID_CAJA);
        caja.setNombre("Caja Principal");
        caja.setActivo(true);
        cajero = asignacion(ID_CAJERO, "Juan", "Pérez", "Cajero");

        when(ordenDAO.findById(ID_ORDEN)).thenReturn(orden);
        when(cajaDAO.findById(ID_CAJA)).thenReturn(caja);
        when(empleadoRolDAO.findById(ID_CAJERO)).thenReturn(cajero);
    }

    private static EmpleadoRol asignacion(UUID id, String nombre, String apellido, String nombreRol) {
        Empleado e = new Empleado(UUID.randomUUID());
        e.setNombre(nombre);
        e.setApellido(apellido);
        e.setActivo(true);
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(nombreRol);
        r.setActivo(true);
        EmpleadoRol er = new EmpleadoRol(id);
        er.setIdEmpleado(e);
        er.setIdRol(r);
        er.setActivo(true);
        return er;
    }

    private static OrdenProducto linea(Orden o, String producto, String precio, String obs) {
        Producto p = new Producto(UUID.randomUUID());
        p.setNombre(producto);
        OrdenProducto op = new OrdenProducto(UUID.randomUUID());
        op.setIdOrden(o);
        op.setIdProducto(p);
        op.setPrecio(new BigDecimal(precio));
        op.setObservaciones(obs);
        return op;
    }

    private static PagoSolicitado pago(String tipo, String monto, String referencia) {
        PagoSolicitado p = new PagoSolicitado();
        p.tipoPago = tipo;
        p.monto = monto == null ? null : new BigDecimal(monto);
        p.referenciaExterna = referencia;
        return p;
    }

    private static SolicitudCobro solicitud(PagoSolicitado... pagos) {
        SolicitudCobro s = new SolicitudCobro();
        s.idCaja = ID_CAJA.toString();
        s.idEmpleadoRol = ID_CAJERO.toString();
        s.pagos = new ArrayList<>(java.util.Arrays.asList(pagos));
        return s;
    }

    /** Orden de Ana: 3.75 + 3.75 + 2.00 = 9.50 */
    private void ordenDeAnaPendiente() {
        when(ordenProductoDAO.findPendientesByOrden(orden)).thenReturn(List.of(
                linea(orden, "Latte Vainilla", "3.75", "Cliente: Ana | sin azúcar"),
                linea(orden, "Latte Vainilla", "3.75", "Cliente: Ana"),
                linea(orden, "Croissant", "2.00", "Cliente: Ana")));
    }

    // ------------------------------------------------------------ pendientes

    @Test
    void pendientesAgrupaLasLineasPorOrdenYCalculaElTotal() {
        Orden otra = new Orden(UUID.randomUUID());
        otra.setFechaCreacion(new Date());
        otra.setIdEmpleadoRol(asignacion(UUID.randomUUID(), "Ana", "Gómez", "Camarero"));
        when(ordenDAO.findPendientesDeCobro(0, 20)).thenReturn(List.of(orden, otra));
        when(ordenProductoDAO.findPendientesDeOrdenes(List.of(orden, otra))).thenReturn(List.of(
                linea(orden, "Latte Vainilla", "3.75", "Cliente: Ana | sin azúcar"),
                linea(otra, "Cappuccino", "3.25", "Mesa 4"),
                linea(orden, "Croissant", "2", "Cliente: Ana")));

        List<Map<String, Object>> resultado = service.pendientes(0, 20);

        assertEquals(2, resultado.size());
        Map<String, Object> primera = resultado.get(0);
        assertEquals(ID_ORDEN.toString(), primera.get("idOrden"));
        assertEquals("Ana", primera.get("cliente"));
        assertEquals("Mesero Virtual (Tienda en línea)", primera.get("tomadaPor"));
        assertEquals(new BigDecimal("5.75"), primera.get("total"));
        List<?> productos = (List<?>) primera.get("productos");
        assertEquals(2, productos.size());
        assertEquals("sin azúcar", ((Map<?, ?>) productos.get(0)).get("nota"));

        Map<String, Object> segunda = resultado.get(1);
        assertNull(segunda.get("cliente"));
        assertEquals("Ana Gómez (Camarero)", segunda.get("tomadaPor"));
        assertEquals("Mesa 4", ((Map<?, ?>) ((List<?>) segunda.get("productos")).get(0)).get("nota"));
    }

    @Test
    void pendienteDeUnaOrdenQueNoExisteLanzaNoEncontrado() {
        UUID id = UUID.randomUUID();
        NoEncontradoException ex = assertThrows(NoEncontradoException.class, () -> service.pendiente(id));
        assertEquals(id, ex.getId());
    }

    @Test
    void contarPendientesDelegaEnElDAO() {
        when(ordenDAO.countPendientesDeCobro()).thenReturn(4);
        assertEquals(4, service.contarPendientes());
    }

    // ------------------------------------------------------------ cobrar

    @Test
    void cobroFraccionadoGuardaFacturaProductosPagoYDetalles() {
        ordenDeAnaPendiente();

        Map<String, Object> r = service.cobrar(ID_ORDEN,
                solicitud(pago("tarjeta", "4.50", "AUT-1"), pago("EFECTIVO", "10.00", null)));

        assertEquals(new BigDecimal("9.50"), r.get("total"));
        assertEquals(new BigDecimal("14.50"), r.get("pagado"));
        assertEquals(new BigDecimal("5.00"), r.get("cambio"));
        assertEquals("Ana", r.get("cliente"));

        ArgumentCaptor<Factura> factura = ArgumentCaptor.forClass(Factura.class);
        verify(facturaDAO).create(factura.capture());
        assertEquals(ReglasFactura.PAGADA, factura.getValue().getEstado());
        assertEquals(caja, factura.getValue().getIdCaja());
        assertEquals(cajero, factura.getValue().getIdEmpleadoRol());
        assertEquals(factura.getValue().getIdFactura().toString(), r.get("idFactura"));

        verify(facturaOrdenProductoDAO, times(3)).create(any(FacturaOrdenProducto.class));

        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoDAO).create(pago.capture());
        assertEquals(ReglasPago.COMPLETADO, pago.getValue().getEstado());

        ArgumentCaptor<PagoDetalle> detalles = ArgumentCaptor.forClass(PagoDetalle.class);
        verify(pagoDetalleDAO, times(2)).create(detalles.capture());
        assertEquals("TARJETA", detalles.getAllValues().get(0).getTipoPago());
        assertEquals(pago.getValue(), detalles.getAllValues().get(1).getIdPago());
    }

    @Test
    void elClienteQueEscribeElCajeroGanaAlDeLaOrden() {
        ordenDeAnaPendiente();
        SolicitudCobro s = solicitud(pago("EFECTIVO", "9.50", null));
        s.cliente = "Ana López";
        assertEquals("Ana López", service.cobrar(ID_ORDEN, s).get("cliente"));
    }

    @Test
    void ordenQueNoExisteDa404() {
        UUID id = UUID.randomUUID();
        assertThrows(NoEncontradoException.class, () -> service.cobrar(id, solicitud(pago("EFECTIVO", "1", null))));
        assertThrows(NoEncontradoException.class, () -> service.cobrar(null, solicitud(pago("EFECTIVO", "1", null))));
    }

    @Test
    void cuerpoVacio() {
        assertWrongParam("registro", () -> service.cobrar(ID_ORDEN, null));
    }

    @Test
    void cajaFaltanteMalEscritaOInactiva() {
        SolicitudCobro s = solicitud(pago("EFECTIVO", "9.50", null));
        s.idCaja = null;
        assertWrongParam("idCaja", () -> service.cobrar(ID_ORDEN, s));
        s.idCaja = "no-es-uuid";
        assertWrongParam("idCaja", () -> service.cobrar(ID_ORDEN, s));
        caja.setActivo(false);
        s.idCaja = ID_CAJA.toString();
        assertWrongParam("idCaja", () -> service.cobrar(ID_ORDEN, s));
    }

    @Test
    void soloUnCajeroActivoPuedeCobrar() {
        SolicitudCobro s = solicitud(pago("EFECTIVO", "9.50", null));
        s.idEmpleadoRol = null;
        assertWrongParam("idEmpleadoRol", () -> service.cobrar(ID_ORDEN, s));

        s.idEmpleadoRol = ID_CAJERO.toString();
        cajero.getIdRol().setNombre("Barista");
        assertWrongParam("idEmpleadoRol", () -> service.cobrar(ID_ORDEN, s));

        cajero.getIdRol().setNombre("Cajero");
        cajero.setActivo(false);
        assertWrongParam("idEmpleadoRol", () -> service.cobrar(ID_ORDEN, s));
    }

    @Test
    void sinPagosOConDemasiados() {
        assertWrongParam("pagos", () -> service.cobrar(ID_ORDEN, solicitud()));
        SolicitudCobro s = solicitud();
        s.pagos = null;
        assertWrongParam("pagos", () -> service.cobrar(ID_ORDEN, s));
        PagoSolicitado[] once = new PagoSolicitado[11];
        for (int i = 0; i < once.length; i++) {
            once[i] = pago("EFECTIVO", "1", null);
        }
        assertWrongParam("pagos", () -> service.cobrar(ID_ORDEN, solicitud(once)));
    }

    @Test
    void cadaPagoSeValidaConReglasPago() {
        assertWrongParam("pagos[0]", () -> service.cobrar(ID_ORDEN, solicitud(pago("BITCOIN", "9.50", null))));
        assertWrongParam("pagos[1]", () -> service.cobrar(ID_ORDEN,
                solicitud(pago("EFECTIVO", "5", null), pago("EFECTIVO", "-1", null))));
        assertWrongParam("pagos[0]", () -> service.cobrar(ID_ORDEN, solicitud(pago("TARJETA", "9.50", " "))));
        assertWrongParam("pagos[0]", () -> service.cobrar(ID_ORDEN, solicitud(pago("EFECTIVO", "1.005", null))));
        assertWrongParam("pagos[0]", () -> service.cobrar(ID_ORDEN, solicitud((PagoSolicitado) null)));
        assertWrongParam("pagos[0].referenciaExterna", () -> service.cobrar(ID_ORDEN,
                solicitud(pago("TARJETA", "9.50", "X".repeat(101)))));
    }

    @Test
    void loPagadoDebeCubrirElTotal() {
        ordenDeAnaPendiente();
        SolicitudCobroInvalidaException ex = assertWrongParam("pagos",
                () -> service.cobrar(ID_ORDEN, solicitud(pago("EFECTIVO", "5.00", null))));
        assertTrue(ex.getMessage().contains("faltan $4.50"));
        verify(facturaDAO, never()).create(any());
    }

    @Test
    void tarjetaNoPuedePasarseDelTotal() {
        ordenDeAnaPendiente();
        assertWrongParam("pagos", () -> service.cobrar(ID_ORDEN, solicitud(pago("TARJETA", "20.00", "AUT-9"))));
    }

    @Test
    void ordenYaCobradaNoSeCobraDosVeces() {
        when(ordenProductoDAO.findPendientesByOrden(orden)).thenReturn(List.of());
        assertWrongParam("idOrden", () -> service.cobrar(ID_ORDEN, solicitud(pago("EFECTIVO", "9.50", null))));
        verify(facturaDAO, never()).create(any());
    }

    @Test
    void clienteMuyLargo() {
        ordenDeAnaPendiente();
        SolicitudCobro s = solicitud(pago("EFECTIVO", "9.50", null));
        s.cliente = "A".repeat(156);
        assertWrongParam("cliente", () -> service.cobrar(ID_ORDEN, s));
    }

    private static SolicitudCobroInvalidaException assertWrongParam(String esperado, Runnable accion) {
        SolicitudCobroInvalidaException ex = assertThrows(SolicitudCobroInvalidaException.class, accion::run);
        assertEquals(esperado, ex.getWrongParam());
        return ex;
    }

    // ------------------------------------------------------------ ticket

    @Test
    void ticketAgrupaProductosYSumaLosPagosCompletados() {
        UUID idFactura = UUID.randomUUID();
        Factura f = new Factura(idFactura);
        f.setCliente("Luisa");
        f.setFechaFacturacion(new Date());
        f.setEstado(ReglasFactura.PAGADA);
        f.setIdCaja(caja);
        f.setIdEmpleadoRol(cajero);
        when(facturaDAO.findById(idFactura)).thenReturn(f);

        List<FacturaOrdenProducto> productos = new ArrayList<>();
        for (OrdenProducto op : List.of(linea(orden, "Latte", "3.75", null), linea(orden, "Latte", "3.75", null),
                linea(orden, "Brownie", "2.75", null))) {
            FacturaOrdenProducto fop = new FacturaOrdenProducto(UUID.randomUUID());
            fop.setIdOrdenProducto(op);
            fop.setPrecio(op.getPrecio());
            productos.add(fop);
        }
        when(facturaOrdenProductoDAO.findByFactura(f, 0, 500)).thenReturn(productos);

        Pago completado = new Pago(UUID.randomUUID());
        completado.setEstado(ReglasPago.COMPLETADO);
        Pago anulado = new Pago(UUID.randomUUID());
        anulado.setEstado(ReglasPago.ANULADO);
        when(pagoDAO.findByFactura(f, 0, 50)).thenReturn(List.of(anulado, completado));
        PagoDetalle efectivo = new PagoDetalle(UUID.randomUUID());
        efectivo.setTipoPago("EFECTIVO");
        efectivo.setMonto(new BigDecimal("20"));
        when(pagoDetalleDAO.findByPagoOrdenado(completado, 0, 50)).thenReturn(List.of(efectivo));

        Ticket t = service.ticket(idFactura);

        assertEquals("Luisa", t.cliente());
        assertEquals("Caja Principal", t.caja());
        assertEquals("Juan Pérez (Cajero)", t.cajero());
        assertEquals(2, t.lineas().size());
        LineaTicket latte = t.lineas().get(0);
        assertEquals(2, latte.cantidad());
        assertEquals(new BigDecimal("7.50"), latte.subtotal());
        assertEquals(new BigDecimal("10.25"), t.total());
        assertEquals(1, t.pagos().size());
        assertEquals(new BigDecimal("9.75"), t.cambio());
        verify(pagoDetalleDAO, never()).findByPagoOrdenado(anulado, 0, 50);
    }

    @Test
    void ticketDeFacturaQueNoExiste() {
        UUID id = UUID.randomUUID();
        assertThrows(NoEncontradoException.class, () -> service.ticket(id));
        assertThrows(NoEncontradoException.class, () -> service.ticket(null));
    }

    @Test
    void tomadaPorSinEmpleado() {
        assertNull(CobroService.tomadaPor(null));
        assertNull(CobroService.tomadaPor(new EmpleadoRol(UUID.randomUUID())));
    }
}
