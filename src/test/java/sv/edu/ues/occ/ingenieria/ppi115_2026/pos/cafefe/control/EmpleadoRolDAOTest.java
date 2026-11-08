package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoRolDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<EmpleadoRol> queryList;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private EmpleadoRolDAO dao;

    @Test
    @DisplayName("findByEmpleado: Validaciones de argumentos nulos e inválidos")
    void testFindByEmpleadoValidaciones() {
        Empleado eSinId = new Empleado();
        Empleado eValido = new Empleado(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class, () -> dao.findByEmpleado(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByEmpleado(eSinId, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByEmpleado(eValido, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByEmpleado(eValido, 0, 0));
    }

    @Test
    @DisplayName("findByEmpleado: Ejecuta NamedQuery paginada exitosamente")
    void testFindByEmpleadoExitoso() {
        Empleado empleado = new Empleado(UUID.randomUUID());

        when(em.createNamedQuery("EmpleadoRol.findByEmpleado", EmpleadoRol.class)).thenReturn(queryList);
        when(queryList.setParameter("empleado", empleado)).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(10)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new EmpleadoRol()));

        List<EmpleadoRol> resultado = dao.findByEmpleado(empleado, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(10);
    }

    @Test
    @DisplayName("countByEmpleado: Retorna 0 si empleado es nulo/sin ID o entrega conteo válido")
    void testCountByEmpleado() {
        assertEquals(0, dao.countByEmpleado(null));
        assertEquals(0, dao.countByEmpleado(new Empleado()));

        Empleado empleado = new Empleado(UUID.randomUUID());
        when(em.createNamedQuery("EmpleadoRol.countByEmpleado", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("empleado", empleado)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(4L);

        assertEquals(4, dao.countByEmpleado(empleado));
    }
}