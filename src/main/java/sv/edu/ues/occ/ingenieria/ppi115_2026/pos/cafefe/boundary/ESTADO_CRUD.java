package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

/**
 * Estado en el que se encuentra la pantalla de mantenimiento (CRUD).
 * <ul>
 * <li>NINGUNO: se muestra el listado.</li>
 * <li>CREAR: se muestra el formulario vacío para un registro nuevo.</li>
 * <li>MODIFICAR: se muestra el formulario con el registro seleccionado.</li>
 * <li>ELIMINAR: reservado (la eliminación se hace desde MODIFICAR).</li>
 * </ul>
 */
public enum ESTADO_CRUD {

    NINGUNO, CREAR, MODIFICAR, ELIMINAR
}
