package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;
//Imports del parche donde arreglo los errores
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author johnyv
 */

/**
 * Model de la pantalla de mantenimiento de TipoCaracteristica.
 * Toda la lógica CRUD (listado paginado, crear, modificar, eliminar)
 * se hereda de {@link DefaultModel}; aquí solo va lo propio de la entidad.
 */
@Named
@ViewScoped
public class TipoCaracteristicaModel extends DefaultModel<TipoCaracteristica> {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;
    
    @Override
    protected AbstractDataAccess<TipoCaracteristica> getDAO() {
        return tipoCaracteristicaDAO;
    }
    
    @Override
    protected UUID getId(TipoCaracteristica entidad) {
        return entidad.getIdTipoCaracteristica();
    }
    
    @Override
    protected TipoCaracteristica nuevoRegistro() {
        TipoCaracteristica nuevo = new TipoCaracteristica(UUID.randomUUID());
        nuevo.setActivo(true);
        nuevo.setExpresionRegular(".*");
        return nuevo;
    }
    
    @Override
    public String nombreBean() {
        return "Tipo de Característica";
    }
    
    /** Rechaza expresiones regulares mal formadas (ej. "[" o "(abc"). */
    public void validarExpresionRegular(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {
        if (value == null || value.toString().isBlank()) {
            return; // vacío se permite: Producto lo trata como ".*"
        }
        try {
            Pattern.compile(value.toString());
        } catch (PatternSyntaxException ex) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "La expresión regular no es válida",
                    ex.getDescription()));
    }
}
}
