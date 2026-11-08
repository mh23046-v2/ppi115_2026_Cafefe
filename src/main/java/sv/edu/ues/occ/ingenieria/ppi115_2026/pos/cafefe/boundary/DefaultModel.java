package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AccesoDatosException;

public abstract class DefaultModel<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(DefaultModel.class.getName());

    protected ESTADO_CRUD estado = ESTADO_CRUD.NINGUNO;
    protected T registro;
    protected LazyDataModel<T> lazyModel;

    protected abstract AbstractDataAccess<T> getDAO();
    protected abstract UUID getId(T entidad);

    protected abstract T nuevoRegistro();
    public abstract String nombreBean();

    //Llena los combos (selectOneMenu) de relaciones. Por defecto no hace nada. 
    protected void cargarOpciones() {
    }

    //Registros de la página que pide la tabla. Por defecto, todos los de la tabla.
    protected List<T> cargarDatos(int first, int max) {
        return getDAO().findRange(first, max);
    }

    //Total de registros (para el paginador). Debe ser coherente con cargarDatos. 
    protected int contarDatos() {
        return (int) Math.min(getDAO().count(), Integer.MAX_VALUE);
    }

    // Gancho: se llama cada vez que cambia el registro seleccionado.
    protected void registroCambio() {
    }

    // Devuelve null si el registro es válido, o el mensaje de error.
    protected String validarRegistro(T registro) {
        return null;
    }

    @PostConstruct
    public void inicializar() {
        this.lazyModel = new LazyDataModel<T>() {
            private static final long serialVersionUID = 1L;

            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                try {
                    return contarDatos();
                } catch (RuntimeException ex) {
                    notificarError("No se pudo contar los registros", ex);
                    return 0;
                }
            }

            @Override
            public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                try {
                    return cargarDatos(first, pageSize);
                } catch (RuntimeException ex) {
                    notificarError("No se pudieron cargar los registros", ex);
                    return new ArrayList<>();
                }
            }

            @Override
            public String getRowKey(T entidad) {
                return getId(entidad).toString();
            }

            @Override
            public T getRowData(String rowKey) {
                try {
                    return getDAO().findById(UUID.fromString(rowKey));
                } catch (RuntimeException ex) {
                    notificarError("No se pudo recuperar el registro", ex);
                    return null;
                }
            }
        };

        try {
            cargarOpciones();
        } catch (RuntimeException ex) {
            notificarError("No se pudieron cargar las opciones", ex);
        }
    }

    //Al hacer clic en una fila de la tabla se abre el formulario en modo MODIFICAR.
    public void onRowSelect(SelectEvent<T> event) {
        if (event != null && event.getObject() != null) {
            setRegistro(event.getObject());
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
    }

    public void btnNuevoHandler() {
        setRegistro(nuevoRegistro());
        this.estado = ESTADO_CRUD.CREAR;
    }

    public void btnCrearHandler() {
        ejecutarAccion(() -> getDAO().create(registro), "creado", true);
    }

    public void btnModificarHandler() {
        ejecutarAccion(() -> getDAO().modify(registro), "actualizado", true);
    }

    public void btnEliminarHandler() {
        ejecutarAccion(() -> getDAO().delete(registro), "eliminado", false);
    }

    public void btnCancelarHandler() {
        setRegistro(null);
        this.estado = ESTADO_CRUD.NINGUNO;
    }

    // Ejecuta la acción sobre el registro, verifica validaciones pre-inserción, muestra el mensaje de éxito o error y regresa al listado.
    private void ejecutarAccion(Runnable accion, String verbo, boolean validar) {
        if (registro == null) {
            notificar(FacesMessage.SEVERITY_ERROR, "Registro no puede ser nulo", "Seleccione algún registro");
            return;
        }
        
        if (validar) {
            String error = validarRegistro(registro);
            if (error != null) {
                notificar(FacesMessage.SEVERITY_ERROR, "El registro no fue " + verbo, error);
                return;
            }
        }
        
        try {
            accion.run();
            notificar(FacesMessage.SEVERITY_INFO, "Registro " + verbo + " con éxito", null);
            this.estado = ESTADO_CRUD.NINGUNO;
            setRegistro(null);
        } catch (RuntimeException ex) {
            notificarError("El registro no fue " + verbo, ex);
        }
    }

    // Envía notificaciones a la interfaz web
    protected void notificar(FacesMessage.Severity severidad, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, resumen, detalle));
    }

    // Identifica el tipo de error capturado y extrae un mensaje comprensible
    protected void notificarError(String resumen, Exception ex) {
        AccesoDatosException acceso = buscarCausa(ex, AccesoDatosException.class);
        if (acceso != null) {
            notificar(FacesMessage.SEVERITY_ERROR, resumen, acceso.getMessage() + ": " + causaRaiz(acceso));
            return;
        }

        ConstraintViolationException cv = buscarCausa(ex, ConstraintViolationException.class);
        if (cv != null) {
            for (ConstraintViolation<?> v : cv.getConstraintViolations()) {
                notificar(FacesMessage.SEVERITY_ERROR, resumen, v.getMessage());
            }
            return;
        }

        IllegalArgumentException invalido = buscarCausa(ex, IllegalArgumentException.class);
        if (invalido != null) {
            notificar(FacesMessage.SEVERITY_ERROR, resumen, invalido.getMessage());
            return;
        }

        LOG.log(Level.SEVERE, resumen, ex);
        notificar(FacesMessage.SEVERITY_ERROR, resumen, "Error inesperado, revise el registro del servidor");
    }

    // Busca en la cadena de excepciones si el error fue provocado por una clase específica.

    private static <E extends Throwable> E buscarCausa(Throwable t, Class<E> tipo) {
        for (int i = 0; t != null && i < 10; i++, t = t.getCause()) {
            if (tipo.isInstance(t)) {
                return tipo.cast(t);
            }
        }
        return null;
    }

    // Las excepciones de JPA llegan envueltas. La causa raíz trae el mensaje útil de PostgreSQL.
    private String causaRaiz(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage();
    }

    public LazyDataModel<T> getLazyModel() {
        return lazyModel;
    }

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
        registroCambio();
    }

    public ESTADO_CRUD getEstado() {
        return estado;
    }
}