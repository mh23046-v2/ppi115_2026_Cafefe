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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class EmpleadoRolDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<EmpleadoRol> typedQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    @InjectMocks
    private EmpleadoRolDAO empleadoRolDAO;

    @Test
    @DisplayName("findByIdEmpleado filtra por empleado y aplica la paginación (first, max)")
    void testFindByIdEmpleadoPaginado() {
        UUID idEmpleado = UUID.randomUUID();
        List<EmpleadoRol> esperados = Collections.singletonList(new EmpleadoRol(UUID.randomUUID()));
        when(em.createNamedQuery("EmpleadoRol.findByIdEmpleado", EmpleadoRol.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("idEmpleado", idEmpleado)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(10)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(5)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<EmpleadoRol> resultado = empleadoRolDAO.findByIdEmpleado(idEmpleado, 10, 5);

        assertEquals(esperados, resultado);
        verify(typedQuery).setFirstResult(10);
        verify(typedQuery).setMaxResults(5);
    }

    @Test
    @DisplayName("findByIdEmpleado con id null lanza error y no consulta la BD")
    void testFindByIdEmpleadoNulo() {
        assertThrows(IllegalArgumentException.class, () -> empleadoRolDAO.findByIdEmpleado(null, 0, 10));

        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("countByIdEmpleado devuelve cuántos roles tiene el empleado")
    void testCountByIdEmpleado() {
        UUID idEmpleado = UUID.randomUUID();
        when(em.createNamedQuery("EmpleadoRol.countByIdEmpleado", Long.class)).thenReturn(countQuery);
        when(countQuery.setParameter("idEmpleado", idEmpleado)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(3L);

        assertEquals(3L, empleadoRolDAO.countByIdEmpleado(idEmpleado));
    }

    @Test
    @DisplayName("countByIdEmpleado con id null lanza error")
    void testCountByIdEmpleadoNulo() {
        assertThrows(IllegalArgumentException.class, () -> empleadoRolDAO.countByIdEmpleado(null));

        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("findByEmpleado consulta con el empleado como parámetro")
    void testFindByEmpleado() {
        Empleado empleado = new Empleado(UUID.randomUUID());
        List<EmpleadoRol> esperados = Collections.singletonList(new EmpleadoRol(UUID.randomUUID()));
        when(em.createQuery(anyString(), eq(EmpleadoRol.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("empleado", empleado)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        assertEquals(esperados, empleadoRolDAO.findByEmpleado(empleado));
        verify(typedQuery).setParameter("empleado", empleado);
    }
}
