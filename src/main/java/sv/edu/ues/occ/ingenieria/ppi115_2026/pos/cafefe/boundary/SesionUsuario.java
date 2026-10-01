package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author hernandez
 */

/**
 * Datos de la sesión del usuario. Por ahora solo guarda el idioma elegido;
 * la plantilla general lo usa en &lt;f:view locale="..."&gt;.
 * (Tomado del proyecto de clase / versión de Johnny.)
 */
@Named
@SessionScoped
public class SesionUsuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idiomaSeleccionado = "es";
    private Map<String, Locale> idiomasSoportados;
    private List<SelectItem> opcionesIdioma;

    @PostConstruct
    public void inicializar() {
        idiomasSoportados = new LinkedHashMap<>();
        idiomasSoportados.put("Español", Locale.of("es"));
        idiomasSoportados.put("English", Locale.of("en", "US"));
        idiomasSoportados.put("Deutsch", Locale.of("de", "DE"));

        opcionesIdioma = new ArrayList<>();
        for (Map.Entry<String, Locale> entry : idiomasSoportados.entrySet()) {
            opcionesIdioma.add(new SelectItem(entry.getValue().toString(), entry.getKey()));
        }
    }

    public void cambiarIdioma(AjaxBehaviorEvent event) {
        for (Map.Entry<String, Locale> entry : idiomasSoportados.entrySet()) {
            if (entry.getValue().toString().equals(idiomaSeleccionado)) {
                FacesContext.getCurrentInstance().getViewRoot().setLocale(entry.getValue());
            }
        }
    }

    public String getIdiomaSeleccionado() {
        return idiomaSeleccionado;
    }

    public void setIdiomaSeleccionado(String idiomaSeleccionado) {
        this.idiomaSeleccionado = idiomaSeleccionado;
    }

    public List<SelectItem> getOpcionesIdioma() {
        return opcionesIdioma;
    }
}
