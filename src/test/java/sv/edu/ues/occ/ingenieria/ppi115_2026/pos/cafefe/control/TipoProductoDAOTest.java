package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoProductoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<TipoProducto> query;

    @InjectMocks
    private TipoProductoDAO dao;

    @Test
    @DisplayName("buscarPorNombre: Lanza IllegalArgumentException si max <= 0")
    void testBuscarPorNombreMaxInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombre("Bebida", 0));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombre("Bebida", -1));
    }

    @Test
    @DisplayName("buscarPorNombre: Ejecuta la consulta correctamente")
    void testBuscarPorNombreExitoso() {
        when(em.createNamedQuery("TipoProducto.buscarPorNombre", TipoProducto.class)).thenReturn(query);
        when(query.setParameter(eq("texto"), anyString())).thenReturn(query);
        when(query.setMaxResults(10)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TipoProducto()));

        List<TipoProducto> resultado = dao.buscarPorNombre("  Bebida ", 10);

        assertFalse(resultado.isEmpty());
        verify(query).setParameter(eq("texto"), anyString());
        verify(query).setMaxResults(10);
    }

    @Test
    @DisplayName("ordenar: Retorna criterios por nombre e idTipoProducto")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenar() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<TipoProducto> root = mock(Root.class);
        Path pathNombre = mock(Path.class);
        Path pathId = mock(Path.class);
        Order orderNombre = mock(Order.class);
        Order orderId = mock(Order.class);

        when(root.get("nombre")).thenReturn(pathNombre);
        when(root.get("idTipoProducto")).thenReturn(pathId);
        when(cb.asc(pathNombre)).thenReturn(orderNombre);
        when(cb.asc(pathId)).thenReturn(orderId);

        List<Order> resultado = dao.ordenar(cb, root);

        assertEquals(2, resultado.size());
        assertEquals(orderNombre, resultado.get(0));
        assertEquals(orderId, resultado.get(1));
    }
}