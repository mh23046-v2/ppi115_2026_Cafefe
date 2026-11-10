package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@Named
@ViewScoped
public class CaracteristicaModel extends DefaultModel<Caracteristica> {

    private static final long serialVersionUID = 1L;

    @Inject
    private CaracteristicaDAO caracteristicaDAO;
    @Inject
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    @Override
    protected AbstractDataAccess<Caracteristica> getDAO() {
        return caracteristicaDAO;
    }

    @Override
    protected UUID getId(Caracteristica entidad) {
        return entidad.getIdCaracteristica();
    }

    @Override
    protected Caracteristica nuevoRegistro() {
        Caracteristica nuevo = new Caracteristica(UUID.randomUUID());
        nuevo.setActivo(true);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Característica";
    }

    public List<TipoCaracteristica> completarTipoCaracteristica(String query) {
        try {
            return tipoCaracteristicaDAO.buscarActivosPorNombre(query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los tipos de característica", ex);
            return new ArrayList<>();
        }
    }

    @Override
    protected String validarRegistro(Caracteristica registro) {
        if (registro.getNombre() == null || registro.getNombre().isBlank()) {
            return "El nombre de la característica es obligatorio";
        }
        
        TipoCaracteristica tipo = registro.getIdTipoCaracteristica();
        if (tipo == null || tipo.getIdTipoCaracteristica() == null) {
            return "Debe seleccionar un Tipo de Característica válido";
        }
        if (!Boolean.TRUE.equals(tipo.getActivo())) {
            return "El Tipo de Característica seleccionado se encuentra inactivo";
        }
        return null;
    }
}