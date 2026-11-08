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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Rol> queryList;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private RolDAO dao;

    @Test
    @DisplayName("findByNombre: Lanza IllegalArgumentException ante paginación inválida")
    void testFindByNombrePaginacionInvalida() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByNombre("admin", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByNombre("admin", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dao.findByNombre("admin", 0, -5));
    }

    @Test
    @DisplayName("findByNombre: Ejecuta la NamedQuery paginada correctamente")
    void testFindByNombreExitoso() {
        when(em.createNamedQuery("Rol.findByNombre", Rol.class)).thenReturn(queryList);
        when(queryList.setParameter("nombre", "%admin%")).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(10)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new Rol()));

        List<Rol> resultado = dao.findByNombre("admin", 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(10);
    }

    @Test
    @DisplayName("findByNombre (sobrenacargado): Delega a findByNombre con firstResult = 0")
    void testFindByNombreSobrecargado() {
        when(em.createNamedQuery("Rol.findByNombre", Rol.class)).thenReturn(queryList);
        when(queryList.setParameter("nombre", "%cajero%")).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(5)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new Rol()));

        List<Rol> resultado = dao.findByNombre("cajero", 5);

        assertFalse(resultado.isEmpty());
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(5);
    }

    @Test
    @DisplayName("countByNombre: Retorna el conteo filtrado correctamente")
    void testCountByNombre() {
        when(em.createNamedQuery("Rol.countByNombre", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("nombre", "%cajero%")).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(3L);

        assertEquals(3L, dao.countByNombre("cajero"));
    }

    @Test
    @DisplayName("ordenar: Configura orden por activo descendente y nombre ascendente")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenar() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<Rol> root = mock(Root.class);
        Path pathActivo = mock(Path.class);
        Path pathNombre = mock(Path.class);
        Order orderActivo = mock(Order.class);
        Order orderNombre = mock(Order.class);

        when(root.get("activo")).thenReturn(pathActivo);
        when(root.get("nombre")).thenReturn(pathNombre);
        when(cb.desc(pathActivo)).thenReturn(orderActivo);
        when(cb.asc(pathNombre)).thenReturn(orderNombre);

        List<Order> resultado = dao.ordenar(cb, root);

        assertEquals(2, resultado.size());
        assertEquals(orderActivo, resultado.get(0));
        assertEquals(orderNombre, resultado.get(1));
    }
}