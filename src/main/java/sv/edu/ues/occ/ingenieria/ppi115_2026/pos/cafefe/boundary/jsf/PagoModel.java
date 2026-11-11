package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.FacturaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.FacturaOrdenProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.PagoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasFactura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasPago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;

/**
 * Pantalla de Pagos (maestro). Mismo patrón que FacturaModel: pestaña
 * Generalidades para el pago y pestaña Detalles para los medios de pago
 * (PagoDetalleModel, inyectado y @Dependent).
 *
 * Flujo: se elige una factura PENDIENTE → se agregan los detalles (efectivo,
 * tarjeta, transferencia) → "Completar pago" deja el pago COMPLETADO y la
 * factura PAGADA en una sola transacción y muestra el cambio.
 */
@Named
@ViewScoped
public class PagoModel extends DefaultModel<Pago> {

    private static final long serialVersionUID = 1L;

    @Inject
    private PagoDAO pagoDAO;
    @Inject
    private FacturaDAO facturaDAO;
    @Inject
    private FacturaOrdenProductoDAO facturaOrdenProductoDAO;
    @Inject
    private PagoDetalleModel pagoDetalleModel;
    @Inject
    private Etiquetas etiquetas;

    // Pestaña activa del p:tabView (0 = generalidades, 1 = detalles).
    private int tabActiva;

    @Override
    protected AbstractDataAccess<Pago> getDAO() {
        return pagoDAO;
    }

    @Override
    protected UUID getId(Pago entidad) {
        return entidad.getIdPago();
    }

    @Override
    protected Pago nuevoRegistro() {
        Pago nuevo = new Pago(UUID.randomUUID());
        nuevo.setEstado(ReglasPago.PENDIENTE);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Pago";
    }

    @Override
    protected List<Pago> cargarDatos(int first, int max) {
        return pagoDAO.findRange(first, max);
    }

    @Override
    protected int contarDatos() {
        return (int) Math.min(pagoDAO.count(), Integer.MAX_VALUE);
    }

    @Override
    protected void registroCambio() {
        pagoDetalleModel.setPago(registro);
        pagoDetalleModel.btnCancelarHandler();
        tabActiva = 0;
    }

    @Override
    protected String validarRegistro(Pago p) {
        if (p == null) {
            return "El registro no puede ser nulo";
        }
        Factura f = p.getIdFactura();
        if (f == null || f.getIdFactura() == null) {
            return "Seleccione la factura que se paga";
        }
        if (estado == ESTADO_CRUD.CREAR) {
            // El estado solo cambia con los botones Completar / Anular, nunca a mano.
            p.setEstado(ReglasPago.PENDIENTE);
            if (!ReglasFactura.PENDIENTE.equals(f.getEstado())) {
                return "Solo se cobra una factura PENDIENTE; esta está " + f.getEstado();
            }
            if (pagoDAO.countVigentesByFactura(f, p.getIdPago()) > 0) {
                return "La factura " + etiquetas.factura(f) + " ya tiene un pago vigente";
            }
        }
        return null;
    }

    @Override
    public void btnCrearHandler() {
        Pago guardado = registro;
        super.btnCrearHandler();
        // Igual que Factura: al crear se queda abierto y salta a la pestaña de detalles.
        if (registro == null && guardado != null) {
            setRegistro(guardado);
            estado = ESTADO_CRUD.MODIFICAR;
            tabActiva = 1;
        }
    }

    @Override
    public void btnEliminarHandler() {
        try {
            Pago enBase = registro == null ? null : pagoDAO.findById(registro.getIdPago());
            if (enBase != null && !ReglasPago.PENDIENTE.equals(enBase.getEstado())) {
                notificar(FacesMessage.SEVERITY_ERROR, "El registro no fue eliminado",
                        "Solo se elimina un pago PENDIENTE; uno completado se anula");
                return;
            }
        } catch (RuntimeException ex) {
            notificarError("El registro no fue eliminado", ex);
            return;
        }
        super.btnEliminarHandler();
    }

    /** Botón "Completar pago". */
    public void btnCompletarHandler() {
        if (registro == null) {
            return;
        }
        try {
            BigDecimal cambio = pagoDAO.completar(registro);
            registro.getIdFactura().setEstado(ReglasFactura.PAGADA);
            notificar(FacesMessage.SEVERITY_INFO, "Pago completado",
                    cambio.signum() > 0 ? "Entregue $" + cambio + " de cambio" : "Sin cambio que entregar");
        } catch (RuntimeException ex) {
            notificarError("El pago no se completó", ex);
        }
    }

    /** Botón "Anular pago". */
    public void btnAnularHandler() {
        if (registro == null) {
            return;
        }
        try {
            boolean estabaCompletado = ReglasPago.COMPLETADO.equals(registro.getEstado());
            pagoDAO.anular(registro);
            if (estabaCompletado && registro.getIdFactura() != null) {
                registro.getIdFactura().setEstado(ReglasFactura.PENDIENTE);
            }
            notificar(FacesMessage.SEVERITY_INFO, "Pago anulado",
                    estabaCompletado ? "La factura volvió a PENDIENTE" : null);
        } catch (RuntimeException ex) {
            notificarError("El pago no se anuló", ex);
        }
    }

    public List<Factura> completarFactura(String query) {
        try {
            return facturaDAO.findPorCobrar(query, 10);
        } catch (RuntimeException ex) {
            notificarError("No se pudieron cargar las facturas por cobrar", ex);
            return new ArrayList<>();
        }
    }

    // ---- totales para la vista ----
    public BigDecimal getTotalFactura() {
        if (registro == null || registro.getIdFactura() == null || registro.getIdFactura().getIdFactura() == null) {
            return BigDecimal.ZERO;
        }
        return facturaOrdenProductoDAO.totalByFactura(registro.getIdFactura());
    }

    /** Positivo = falta por pagar; negativo = cambio a entregar. */
    public BigDecimal getSaldo() {
        return getTotalFactura().subtract(pagoDetalleModel.getPagado());
    }

    public boolean isPuedeCompletar() {
        return estado == ESTADO_CRUD.MODIFICAR && registro != null
                && ReglasPago.PENDIENTE.equals(registro.getEstado());
    }

    public boolean isPuedeAnular() {
        return estado == ESTADO_CRUD.MODIFICAR && registro != null
                && !ReglasPago.ANULADO.equals(registro.getEstado());
    }

    // ---- getters y setters para la vista ----
    public PagoDetalleModel getPagoDetalleModel() {
        return pagoDetalleModel;
    }

    public int getTabActiva() {
        return tabActiva;
    }

    public void setTabActiva(int tabActiva) {
        this.tabActiva = tabActiva;
    }
}
