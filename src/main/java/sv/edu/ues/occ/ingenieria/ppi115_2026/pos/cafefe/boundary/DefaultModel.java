package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;

public abstract class DefaultModel<T> implements Serializable {

    private static final long serialVersionUID = 1L;

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
        return (int) getDAO().count();
    }

    // Gancho: se llama cada vez que cambia el registro seleccionado.
    protected void registroCambio() {
    }


    @PostConstruct
    public void inicializar() {
        this.lazyModel = new LazyDataModel<T>() {
            private static final long serialVersionUID = 1L;

            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return contarDatos();
            }

            @Override
            public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                return cargarDatos(first, pageSize);
            }

            @Override
            public String getRowKey(T entidad) {
                return getId(entidad).toString();
            }

            @Override
            public T getRowData(String rowKey) {
                return getDAO().findById(UUID.fromString(rowKey));
            }
        };
        cargarOpciones();
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
        ejecutarAccion(() -> getDAO().create(registro), "creado");
    }

    public void btnModificarHandler() {
        ejecutarAccion(() -> getDAO().modify(registro), "actualizado");
    }

    public void btnEliminarHandler() {
        ejecutarAccion(() -> getDAO().delete(registro), "eliminado");
    }

    public void btnCancelarHandler() {
        setRegistro(null);
        this.estado = ESTADO_CRUD.NINGUNO;
    }

    /**
     * Ejecuta la acción sobre el registro, muestra el mensaje de éxito o
     * error y, si todo salió bien, regresa al listado.
     */
    private void ejecutarAccion(Runnable accion, String verbo) {
        FacesMessage mensaje;
        if (registro != null) {
            try {
                accion.run();
                mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Registro " + verbo + " con éxito", null);
                this.estado = ESTADO_CRUD.NINGUNO;
                setRegistro(null);
                cargarOpciones(); // por si el cambio afecta a los combos
            } catch (Exception ex) {
                mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "El registro no fue " + verbo, causaRaiz(ex));
            }
        } else {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Registro no puede ser nulo", "Seleccione algún registro");
        }
        FacesContext.getCurrentInstance().addMessage(null, mensaje);
    }

    /**
     * Las excepciones de JPA llegan envueltas (EJBException → PersistenceException
     * → PSQLException...). La causa raíz trae el mensaje útil de PostgreSQL.
     */
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
