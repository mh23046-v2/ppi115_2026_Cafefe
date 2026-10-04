package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
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

/**
 *
 * @author johnyv
 */
@Dependent
public class DescuentoProductoModel extends DefaultModel<DescuentoProducto> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DescuentoProductoDAO descuentoProductoDAO;
    @Inject
    private DescuentoDAO descuentoDAO;
    @Inject
    private TipoDescuentoDAO tipoDescuentoDAO;
    @Inject
    private Etiquetas etiquetas;

    private TipoDescuento tipoDescuentoSeleccionado;
    private Descuento descuentoSeleccionado;

    private List<SelectItem> opcionesTipoDescuento;
    private List<SelectItem> opcionesDescuento;

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
        this.opcionesDescuento = new ArrayList<>();
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
        return descuentoProductoDAO.findByProducto(producto);
    }

    @Override
    protected int contarDatos() {
        if (producto == null || producto.getIdProducto() == null) {
            return 0;
        }
        return descuentoProductoDAO.findByProducto(producto).size();
    }

    @Override
    protected void cargarOpciones() {
        opcionesTipoDescuento = new ArrayList<>();
        List<TipoDescuento> tipos = tipoDescuentoDAO.findAll();
        for (TipoDescuento td : tipos) {
            // TipoDescuento SÍ tiene campo 'activo'
            boolean deshabilitado = !Boolean.TRUE.equals(td.getActivo());
            opcionesTipoDescuento.add(new SelectItem(td, td.getNombre(), null, deshabilitado));
        }
        opcionesDescuento = new ArrayList<>();
    }

// Listener al cambiar TipoDescuento en el modal
    public void cambioTipoDescuento() {
        opcionesDescuento = new ArrayList<>();
        this.descuentoSeleccionado = null;

        if (this.tipoDescuentoSeleccionado != null) {
            List<Descuento> lista = descuentoDAO.findByIdTipoDescuento(this.tipoDescuentoSeleccionado);
            for (Descuento d : lista) {
                // Descuento NO tiene campo 'activo', se agregan directamente
                opcionesDescuento.add(new SelectItem(d, etiquetas.descuento(d)));
            }
        }
    }

    // Asigna la selección hecha en el modal al registro principal de DescuentoProducto
    public void asignarDescuento() {
        if (this.registro != null && this.descuentoSeleccionado != null) {
            this.registro.setIdDescuento(this.descuentoSeleccionado);
        }
    }

    private String formatearFecha(Date fecha) {
        if (fecha == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(fecha);
    }
// Validador asignado a 'dtDesde'

    public void validarFechaDesde(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }

        Descuento descuento = (registro != null && registro.getIdDescuento() != null)
                ? registro.getIdDescuento()
                : descuentoSeleccionado;

        if (descuento == null) {
            return;
        }

        Date fechaDesdeIngresada = (Date) value;

        // 1. Validar límite inferior del Descuento Padre
        if (descuento.getFechaDesde() != null && fechaDesdeIngresada.before(descuento.getFechaDesde())) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "La fecha inicial no es válida",
                    "La fecha de inicio debe ser posterior o igual a la fecha inicial del descuento (" + formatearFecha(descuento.getFechaDesde()) + ")"
            ));
        }

        // 2. Validar límite superior del Descuento Padre
        if (descuento.getFechaHasta() != null && fechaDesdeIngresada.after(descuento.getFechaHasta())) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "La fecha inicial no es válida",
                    "La fecha de inicio no puede superar la fecha final del descuento (" + formatearFecha(descuento.getFechaHasta()) + ")"
            ));
        }
    }

// Validador asignado a 'dtHasta'
    public void validarFechaHasta(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Descuento descuento = (registro != null && registro.getIdDescuento() != null)
                ? registro.getIdDescuento()
                : descuentoSeleccionado;

        if (descuento == null) {
            return;
        }

        // 1. Si el descuento Padre tiene fecha límite definida y el usuario la dejó vacía
        if (descuento.getFechaHasta() != null && value == null) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Fecha de finalización requerida",
                    "El descuento seleccionado tiene una fecha de término (" + formatearFecha(descuento.getFechaHasta()) + "). Debe especificar una fecha de finalización."
            ));
        }

        // 2. Validaciones si el usuario ingresó un valor
        if (value != null) {
            Date fechaHastaIngresada = (Date) value;

            // Validar límite superior del Descuento Padre
            if (descuento.getFechaHasta() != null && fechaHastaIngresada.after(descuento.getFechaHasta())) {
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "La fecha final no es válida",
                        "La fecha de finalización no puede superar la fecha límite del descuento (" + formatearFecha(descuento.getFechaHasta()) + ")"
                ));
            }

            // Validar coherencia frente a la Fecha de Inicio registrada en la entidad
            if (registro != null && registro.getFechaDesde() != null) {
                if (fechaHastaIngresada.before(registro.getFechaDesde())) {
                    throw new ValidatorException(new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "La fecha final no es válida",
                            "La fecha final no puede ser anterior a la fecha de inicio"
                    ));
                }
            }
        }
    }

// Validador para Porcentaje actualizado con fallback de descuentoSeleccionado
    public void validarPorcentaje(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }

        Descuento descuento = (registro != null && registro.getIdDescuento() != null)
                ? registro.getIdDescuento()
                : descuentoSeleccionado;

        if (descuento != null && descuento.getIdTipoDescuento() != null) {
            TipoDescuento tipoDescuento = descuento.getIdTipoDescuento();
            if (tipoDescuento.getDescuentoMaximo() != null) {
                try {
                    double porcentajeIngresado = Double.parseDouble(value.toString());
                    double porcentajeMaximo = tipoDescuento.getDescuentoMaximo().doubleValue();

                    if (porcentajeIngresado > porcentajeMaximo) {
                        String mensajeDetalle = String.format(
                                "El valor no cumple la validación de %s: debe ser menor o igual que %.2f",
                                tipoDescuento.getNombre(),
                                porcentajeMaximo
                        );
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

    // Getters y setters para la vista
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

    public List<SelectItem> getOpcionesTipoDescuento() {
        return opcionesTipoDescuento;
    }

    public List<SelectItem> getOpcionesIdDescuento() {
        return opcionesDescuento;
    }
}
