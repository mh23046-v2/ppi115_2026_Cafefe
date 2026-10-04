package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

/**
 * Estado en el que se encuentra la pantalla de mantenimiento (CRUD).
 * NINGUNO: se muestra el listado.
 * CREAR: se muestra el formulario vacío para un registro nuevo.
 * MODIFICAR: se muestra el formulario con el registro seleccionado.
 * ELIMINAR: reservado (la eliminación se hace desde MODIFICAR).
 * 
 */
public enum ESTADO_CRUD {

    NINGUNO, CREAR, MODIFICAR, ELIMINAR
}
