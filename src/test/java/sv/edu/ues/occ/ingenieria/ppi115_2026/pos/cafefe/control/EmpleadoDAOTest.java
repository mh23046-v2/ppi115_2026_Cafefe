package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class EmpleadoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Empleado> typedQuery;

    @InjectMocks
    private EmpleadoDAO empleadoDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertEquals(em, empleadoDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByActivo usa la NamedQuery con el parámetro activo")
    void testFindByActivo() {
        List<Empleado> esperados = Collections.singletonList(new Empleado(UUID.randomUUID()));
        when(em.createNamedQuery("Empleado.findByActivo", Empleado.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("activo", true)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        assertEquals(esperados, empleadoDAO.findByActivo(true));
        verify(typedQuery).setParameter("activo", true);
    }

    @Test
    @DisplayName("findByNombre usa la NamedQuery con el parámetro nombre")
    void testFindByNombre() {
        List<Empleado> esperados = Collections.singletonList(new Empleado(UUID.randomUUID()));
        when(em.createNamedQuery("Empleado.findByNombre", Empleado.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("nombre", "Ana")).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        assertEquals(esperados, empleadoDAO.findByNombre("Ana"));
        verify(typedQuery).setParameter("nombre", "Ana");
    }

    @Test
    @DisplayName("findByApellido usa la NamedQuery con el parámetro apellido")
    void testFindByApellido() {
        List<Empleado> esperados = Collections.singletonList(new Empleado(UUID.randomUUID()));
        when(em.createNamedQuery("Empleado.findByApellido", Empleado.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("apellido", "Pérez")).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        assertEquals(esperados, empleadoDAO.findByApellido("Pérez"));
        verify(typedQuery).setParameter("apellido", "Pérez");
    }

    @Test
    @DisplayName("modify actualiza con merge y devuelve la entidad que regresa JPA")
    void testModify() {
        Empleado empleado = new Empleado(UUID.randomUUID());
        Empleado actualizado = new Empleado(empleado.getIdEmpleado());
        when(em.merge(empleado)).thenReturn(actualizado);

        assertSame(actualizado, empleadoDAO.modify(empleado));
    }
}
