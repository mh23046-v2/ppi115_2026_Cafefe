package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.PagoDetalleDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasPago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;

/**
 * Pestaña "Detalles" del pago (cuánto se paga con cada medio). Mismo patrón
 * que FacturaOrdenProductoModel: @Dependent, sin @Named, inyectado en PagoModel.
 */
@Dependent
public class PagoDetalleModel extends DefaultModel<PagoDetalle> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PagoDetalleDAO pagoDetalleDAO;

    private Pago pago;

    @Override
    protected AbstractDataAccess<PagoDetalle> getDAO() {
        return pagoDetalleDAO;
    }

    @Override
    protected UUID getId(PagoDetalle entidad) {
        return entidad.getIdPagoDetalle();
    }

    @Override
    protected PagoDetalle nuevoRegistro() {
        PagoDetalle nuevo = new PagoDetalle(UUID.randomUUID());
        nuevo.setIdPago(pago);
        nuevo.setTipoPago(ReglasPago.EFECTIVO);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Detalle de pago";
    }

    @Override
    protected List<PagoDetalle> cargarDatos(int first, int max) {
        if (!hayPago()) {
            return Collections.emptyList();
        }
        return pagoDetalleDAO.findByPagoOrdenado(pago, first, max);
    }

    @Override
    protected int contarDatos() {
        if (!hayPago()) {
            return 0;
        }
        return pagoDetalleDAO.countByPago(pago);
    }

    @Override
    protected String validarRegistro(PagoDetalle d) {
        if (d == null) {
            return "El registro no puede ser nulo";
        }
        if (d.getIdPago() == null || d.getIdPago().getIdPago() == null) {
            return "Guarde primero el pago";
        }
        if (!ReglasPago.esEditable(d.getIdPago())) {
            return "El pago está " + d.getIdPago().getEstado() + ": ya no admite cambios en sus detalles";
        }
        String error = ReglasPago.validarDetalle(d);
        if (error != null) {
            return error;
        }
        d.setTipoPago(ReglasPago.normalizarTipo(d.getTipoPago()));
        if (!ReglasPago.requiereReferencia(d.getTipoPago())) {
            d.setReferenciaExterna(null);   // el efectivo no lleva referencia
        } else {
            d.setReferenciaExterna(d.getReferenciaExterna().trim());
        }
        return null;
    }

    @Override
    public void btnEliminarHandler() {
        if (registro != null && !ReglasPago.esEditable(pago)) {
            notificar(FacesMessage.SEVERITY_ERROR, "El registro no fue eliminado",
                    "El pago ya no admite cambios en sus detalles");
            return;
        }
        super.btnEliminarHandler();
    }

    public List<String> getTiposPago() {
        return ReglasPago.TIPOS_PAGO;
    }

    /** Para mostrar u ocultar el campo de referencia según el tipo elegido. */
    public boolean isReferenciaRequerida() {
        return registro != null && ReglasPago.requiereReferencia(registro.getTipoPago());
    }

    public boolean isEditable() {
        return ReglasPago.esEditable(pago);
    }

    public BigDecimal getPagado() {
        return hayPago() ? pagoDetalleDAO.totalByPago(pago) : BigDecimal.ZERO;
    }

    private boolean hayPago() {
        return pago != null && pago.getIdPago() != null;
    }

    // ---- getters y setters para la vista ----
    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }
}
