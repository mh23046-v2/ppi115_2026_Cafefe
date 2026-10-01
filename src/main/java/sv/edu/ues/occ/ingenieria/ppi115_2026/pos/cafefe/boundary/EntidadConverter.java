package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;

/**
 *
 * @author hernandez
 */
@Named("entidadConverter")
@ApplicationScoped
public class EntidadConverter implements Converter<Object> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        Class<?> tipo = component.getValueExpression("value").getType(context.getELContext());
        try {
            return em.find(tipo, UUID.fromString(value.trim()));
        } catch (IllegalArgumentException e) {
            throw new ConverterException("Valor no válido: " + value, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(value);
        return id == null ? "" : id.toString();
    }
}
