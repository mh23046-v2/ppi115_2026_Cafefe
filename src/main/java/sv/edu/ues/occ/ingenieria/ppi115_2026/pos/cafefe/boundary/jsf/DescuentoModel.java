package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@Named
@ViewScoped
public class DescuentoModel extends DefaultModel<Descuento> {

    private static final long serialVersionUID = 1L;

    @Inject
    private DescuentoDAO descuentoDAO;
    @Inject
    private TipoDescuentoDAO tipoDescuentoDAO;

    @Override
    protected AbstractDataAccess<Descuento> getDAO() {
        return descuentoDAO;
    }

    @Override
    protected UUID getId(Descuento entidad) {
        return entidad.getIdDescuento();
    }

    @Override
    protected Descuento nuevoRegistro() {
        Descuento nuevo = new Descuento(UUID.randomUUID());
        nuevo.setFechaDesde(new Date());
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Descuento";
    }

    public List<TipoDescuento> completarTipoDescuento(String query) {
        try {
            return tipoDescuentoDAO.buscarPorNombre(query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los tipos de descuento", ex);
            return new ArrayList<>();
        }
    }

    @Override
    protected String validarRegistro(Descuento registro) {
        if (registro.getNombre() == null || registro.getNombre().isBlank()) {
            return "El nombre del descuento es obligatorio";
        }

        TipoDescuento tipo = registro.getIdTipoDescuento();
        if (tipo == null || tipo.getIdTipoDescuento() == null) {
            return "Debe seleccionar un Tipo de Descuento válido";
        }
        if (!Boolean.TRUE.equals(tipo.getActivo())) {
            return "El Tipo de Descuento seleccionado se encuentra inactivo";
        }

        if (registro.getFechaDesde() == null) {
            return "La fecha inicial (fecha desde) es obligatoria";
        }

        if (registro.getFechaHasta() != null && registro.getFechaHasta().before(registro.getFechaDesde())) {
            return "La fecha final (hasta) no puede ser anterior a la fecha inicial (desde)";
        }

        return null;
    }
}