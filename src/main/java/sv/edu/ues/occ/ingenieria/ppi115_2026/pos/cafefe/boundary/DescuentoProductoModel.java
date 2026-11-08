package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@Dependent
public class DescuentoProductoModel extends DefaultModel<DescuentoProducto> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DescuentoProductoDAO descuentoProductoDAO;
    @Inject
    private DescuentoDAO descuentoDAO;
    @Inject
    private TipoDescuentoDAO tipoDescuentoDAO;

    private TipoDescuento tipoDescuentoSeleccionado;
    private Descuento descuentoSeleccionado;

    private Producto producto;

    @Override
    protected AbstractDataAccess<DescuentoProducto> getDAO() {
        return descuentoProductoDAO;
    }

    @Override
    protected UUID getId(DescuentoProducto entidad) {
        return entidad.getIdDescuentoProducto();
    }

    @Override
    protected DescuentoProducto nuevoRegistro() {
        DescuentoProducto nuevo = new DescuentoProducto(UUID.randomUUID());
        nuevo.setIdProducto(producto);
        this.tipoDescuentoSeleccionado = null;
        this.descuentoSeleccionado = null;
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Descuento de producto";
    }

    @Override
    protected List<DescuentoProducto> cargarDatos(int first, int max) {
        if (producto == null || producto.getIdProducto() == null) {
            return Collections.emptyList();
        }
        return descuentoProductoDAO.findByProducto(producto, first, max);
    }

    @Override
    protected int contarDatos() {
        if (producto == null || producto.getIdProducto() == null) {
            return 0;
        }
        return descuentoProductoDAO.countByProducto(producto);
    }

    // Autocompletado para el selector de TipoDescuento
    public List<TipoDescuento> completarTipoDescuento(String query) {
        try {
            return tipoDescuentoDAO.buscarPorNombre(query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los tipos de descuento", ex);
            return new ArrayList<>();
        }
    }

    // Autocompletado para el selector de Descuento dependiente del TipoDescuento seleccionado.
    public List<Descuento> completarDescuentoPorTipo(String query) {
        if (this.tipoDescuentoSeleccionado == null || this.tipoDescuentoSeleccionado.getIdTipoDescuento() == null) {
            return Collections.emptyList();
        }
        try {
            return descuentoDAO.buscarActivosPorTipoYNombre(this.tipoDescuentoSeleccionado, query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los descuentos para el tipo seleccionado", ex);
            return new ArrayList<>();
        }
    }

    public void asignarDescuento() {
        if (this.registro != null && this.descuentoSeleccionado != null) {
            this.registro.setIdDescuento(this.descuentoSeleccionado);
        }
    }

    @Override
    protected String validarRegistro(DescuentoProducto registro) {
        if (registro.getIdProducto() == null || registro.getIdProducto().getIdProducto() == null) {
            return "Debe especificar un producto válido para asignar el descuento";
        }

        Descuento descuento = registro.getIdDescuento() != null ? registro.getIdDescuento() : descuentoSeleccionado;
        if (descuento == null || descuento.getIdDescuento() == null) {
            return "Debe seleccionar un descuento válido";
        }

        if (registro.getFechaDesde() == null) {
            return "La fecha de inicio de la asignación es obligatoria";
        }

        if (descuento.getFechaDesde() != null && registro.getFechaDesde().before(descuento.getFechaDesde())) {
            return "La fecha inicial debe ser posterior o igual a la fecha inicial del descuento (" + formatearFecha(descuento.getFechaDesde()) + ")";
        }

        if (descuento.getFechaHasta() != null && registro.getFechaDesde().after(descuento.getFechaHasta())) {
            return "La fecha inicial no puede superar la fecha final del descuento (" + formatearFecha(descuento.getFechaHasta()) + ")";
        }

        if (descuento.getFechaHasta() != null && registro.getFechaHasta() == null) {
            return "El descuento seleccionado tiene una fecha límite (" + formatearFecha(descuento.getFechaHasta()) + "). Debe especificar una fecha de finalización.";
        }

        if (registro.getFechaHasta() != null) {
            if (descuento.getFechaHasta() != null && registro.getFechaHasta().after(descuento.getFechaHasta())) {
                return "La fecha de finalización no puede superar la fecha límite del descuento (" + formatearFecha(descuento.getFechaHasta()) + ")";
            }
            if (registro.getFechaHasta().before(registro.getFechaDesde())) {
                return "La fecha final no puede ser anterior a la fecha inicial";
            }
        }

        if (descuento.getIdTipoDescuento() != null && descuento.getIdTipoDescuento().getDescuentoMaximo() != null) {
            if (registro.getValor() != null) {
                double porcentajeIngresado = registro.getValor();
                double porcentajeMaximo = descuento.getIdTipoDescuento().getDescuentoMaximo();
                if (porcentajeIngresado > porcentajeMaximo) {
                    return String.format("El porcentaje asignado (%.2f%%) supera el máximo permitido por el tipo de descuento (%.2f%%)", porcentajeIngresado, porcentajeMaximo);
                }
            }
        }

        return null;
    }

    private String formatearFecha(Date fecha) {
        if (fecha == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(fecha);
    }

    // Validadores para la interfaz web JSF
    public void validarFechaDesde(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        Descuento descuento = (registro != null && registro.getIdDescuento() != null) ? registro.getIdDescuento() : descuentoSeleccionado;
        if (descuento == null) {
            return;
        }
        Date fechaDesdeIngresada = (Date) value;
        if (descuento.getFechaDesde() != null && fechaDesdeIngresada.before(descuento.getFechaDesde())) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "La fecha inicial no es válida",
                    "La fecha de inicio debe ser posterior o igual a la fecha inicial del descuento (" + formatearFecha(descuento.getFechaDesde()) + ")"
            ));
        }
        if (descuento.getFechaHasta() != null && fechaDesdeIngresada.after(descuento.getFechaHasta())) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "La fecha inicial no es válida",
                    "La fecha de inicio no puede superar la fecha final del descuento (" + formatearFecha(descuento.getFechaHasta()) + ")"
            ));
        }
    }

    public void validarFechaHasta(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Descuento descuento = (registro != null && registro.getIdDescuento() != null) ? registro.getIdDescuento() : descuentoSeleccionado;
        if (descuento == null) {
            return;
        }
        if (descuento.getFechaHasta() != null && value == null) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Fecha de finalización requerida",
                    "El descuento seleccionado tiene una fecha de término (" + formatearFecha(descuento.getFechaHasta()) + "). Debe especificar una fecha de finalización."
            ));
        }
        if (value != null) {
            Date fechaHastaIngresada = (Date) value;
            if (descuento.getFechaHasta() != null && fechaHastaIngresada.after(descuento.getFechaHasta())) {
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "La fecha final no es válida",
                        "La fecha de finalización no puede superar la fecha límite del descuento (" + formatearFecha(descuento.getFechaHasta()) + ")"
                ));
            }
            if (registro != null && registro.getFechaDesde() != null && fechaHastaIngresada.before(registro.getFechaDesde())) {
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "La fecha final no es válida",
                        "La fecha final no puede ser anterior a la fecha de inicio"
                ));
            }
        }
    }

    public void validarPorcentaje(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        Descuento descuento = (registro != null && registro.getIdDescuento() != null) ? registro.getIdDescuento() : descuentoSeleccionado;
        if (descuento != null && descuento.getIdTipoDescuento() != null) {
            TipoDescuento tipoDescuento = descuento.getIdTipoDescuento();
            if (tipoDescuento.getDescuentoMaximo() != null) {
                try {
                    double porcentajeIngresado = Double.parseDouble(value.toString());
                    double porcentajeMaximo = tipoDescuento.getDescuentoMaximo().doubleValue();
                    if (porcentajeIngresado > porcentajeMaximo) {
                        String mensajeDetalle = String.format("El valor no cumple la validación de %s: debe ser menor o igual que %.2f", tipoDescuento.getNombre(), porcentajeMaximo);
                        throw new ValidatorException(new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                "El valor no es válido",
                                mensajeDetalle
                        ));
                    }
                } catch (NumberFormatException e) {
                    throw new ValidatorException(new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "El porcentaje debe ser un número válido",
                            null
                    ));
                }
            }
        }
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public TipoDescuento getTipoDescuentoSeleccionado() {
        return tipoDescuentoSeleccionado;
    }

    public void setTipoDescuentoSeleccionado(TipoDescuento tipoDescuentoSeleccionado) {
        this.tipoDescuentoSeleccionado = tipoDescuentoSeleccionado;
    }

    public Descuento getDescuentoSeleccionado() {
        return descuentoSeleccionado;
    }

    public void setDescuentoSeleccionado(Descuento descuentoSeleccionado) {
        this.descuentoSeleccionado = descuentoSeleccionado;
    }
}
