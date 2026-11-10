package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.RolModel;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolModelTest {

    @Mock
    private RolDAO rolDAO;

    @InjectMocks
    private RolModel model;

    @Test
    @DisplayName("nuevoRegistro: Inicializa un Rol con UUID y estado activo")
    void testNuevoRegistro() {
        Rol nuevo = model.nuevoRegistro();
        assertNotNull(nuevo);
        assertNotNull(nuevo.getIdRol());
        assertTrue(nuevo.getActivo());
    }

    @Test
    @DisplayName("validarRegistro: Valida presencia del nombre del rol")
    void testValidarRegistro() {
        Rol r = new Rol();

        assertNotNull(model.validarRegistro(null));

        r.setNombre(null);
        assertNotNull(model.validarRegistro(r));

        r.setNombre("   ");
        assertNotNull(model.validarRegistro(r));

        r.setNombre("Administrador");
        assertNull(model.validarRegistro(r));
    }

    @Test
    @DisplayName("cargarDatos: Consulta la tabla principal paginada vía DAO")
    void testCargarDatos() {
        when(rolDAO.findByNombre("", 0, 10)).thenReturn(List.of(new Rol()));

        List<Rol> resultado = model.cargarDatos(0, 10);

        assertFalse(resultado.isEmpty());
        verify(rolDAO).findByNombre("", 0, 10);
    }

    @Test
    @DisplayName("contarDatos: Obtiene el total de registros para la paginación principal")
    void testContarDatos() {
        when(rolDAO.countByNombre("")).thenReturn(5L);

        assertEquals(5, model.contarDatos());
        verify(rolDAO).countByNombre("");
    }

    @Test
    @DisplayName("completarRol: Retorna acotado a 10 resultados para autocompletado")
    void testCompletarRol() {
        when(rolDAO.findByNombre("adm", 10)).thenReturn(List.of(new Rol()));

        List<Rol> resultado = model.completarRol("adm");

        assertFalse(resultado.isEmpty());
        verify(rolDAO).findByNombre("adm", 10);
    }
}