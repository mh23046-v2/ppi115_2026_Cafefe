package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Empleado> queryList;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private EmpleadoDAO dao;

    @Test
    @DisplayName("findByNombre: Lanza IllegalArgumentException ante paginación inválida")
    void testFindByNombrePaginacionInvalida() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByNombre("juan", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByNombre("juan", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dao.findByNombre("juan", 0, -5));
    }

    @Test
    @DisplayName("findByNombre: Ejecuta la NamedQuery paginada correctamente")
    void testFindByNombreExitoso() {
        when(em.createNamedQuery("Empleado.findByNombre", Empleado.class)).thenReturn(queryList);
        when(queryList.setParameter("nombre", "%juan%")).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(10)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new Empleado()));

        List<Empleado> resultado = dao.findByNombre("juan", 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(10);
    }

    @Test
    @DisplayName("findByNombre (sobrecargado): Consulta acotada desde el origen")
    void testFindByNombreSobrecargado() {
        when(em.createNamedQuery("Empleado.findByNombre", Empleado.class)).thenReturn(queryList);
        when(queryList.setParameter("nombre", "%perez%")).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(10)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new Empleado()));

        List<Empleado> resultado = dao.findByNombre("perez", 10);

        assertFalse(resultado.isEmpty());
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(10);
    }

    @Test
    @DisplayName("countByNombre: Retorna el conteo filtrado")
    void testCountByNombre() {
        when(em.createNamedQuery("Empleado.countByNombre", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("nombre", "%perez%")).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(2L);

        assertEquals(2L, dao.countByNombre("perez"));
    }

    @Test
    @DisplayName("ordenar: Criterios por activo DESC, apellido ASC y nombre ASC")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenar() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<Empleado> root = mock(Root.class);
        Path pathActivo = mock(Path.class);
        Path pathApellido = mock(Path.class);
        Path pathNombre = mock(Path.class);

        Order orderActivo = mock(Order.class);
        Order orderApellido = mock(Order.class);
        Order orderNombre = mock(Order.class);

        when(root.get("activo")).thenReturn(pathActivo);
        when(root.get("apellido")).thenReturn(pathApellido);
        when(root.get("nombre")).thenReturn(pathNombre);

        when(cb.desc(pathActivo)).thenReturn(orderActivo);
        when(cb.asc(pathApellido)).thenReturn(orderApellido);
        when(cb.asc(pathNombre)).thenReturn(orderNombre);

        List<Order> resultado = dao.ordenar(cb, root);

        assertEquals(3, resultado.size());
        assertEquals(orderActivo, resultado.get(0));
        assertEquals(orderApellido, resultado.get(1));
        assertEquals(orderNombre, resultado.get(2));
    }
}