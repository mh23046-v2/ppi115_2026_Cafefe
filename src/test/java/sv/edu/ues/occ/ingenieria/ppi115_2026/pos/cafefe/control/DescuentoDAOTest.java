package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DescuentoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Descuento> query;

    @InjectMocks
    private DescuentoDAO dao;

    @Test
    @DisplayName("buscarPorTipoYNombre: Lanza IllegalArgumentException si los argumentos son inválidos")
    void testBuscarPorTipoYNombreArgumentosInvalidos() {
        TipoDescuento tipoValido = mock(TipoDescuento.class);
        when(tipoValido.getIdTipoDescuento()).thenReturn(UUID.randomUUID());

        TipoDescuento tipoSinId = mock(TipoDescuento.class);
        when(tipoSinId.getIdTipoDescuento()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.buscarActivosPorTipoYNombre(null, "txt", 10));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarActivosPorTipoYNombre(tipoSinId, "txt", 10));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarActivosPorTipoYNombre(tipoValido, "txt", 0));
    }

    @Test
    @DisplayName("buscarPorTipoYNombre: Limpia el texto y ejecuta la consulta correctamente")
    void testBuscarPorTipoYNombreParametrosValidos() {
        TipoDescuento tipo = mock(TipoDescuento.class);
        when(tipo.getIdTipoDescuento()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("Descuento.buscarActivosPorTipoYNombre", Descuento.class)).thenReturn(query);
        when(query.setParameter("tipo", tipo)).thenReturn(query);
        when(query.setParameter("texto", "%navideño%")).thenReturn(query);
        when(query.setParameter(eq("hoy"), any(Date.class))).thenReturn(query);
        when(query.setMaxResults(5)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Descuento()));

        List<Descuento> resultado = dao.buscarActivosPorTipoYNombre(tipo, "   Navideño ", 5);

        assertFalse(resultado.isEmpty());
        verify(query).setParameter("tipo", tipo);
        verify(query).setParameter("texto", "%navideño%");
        verify(query).setMaxResults(5);
        verify(query).setParameter("hoy", DescuentoDAO.inicioDeHoy());
    }

    @Test
    @DisplayName("buscarPorTipoYNombre: Construye el patrón '%%' cuando el texto es null")
    void testBuscarPorTipoYNombreTextoNull() {
        TipoDescuento tipo = mock(TipoDescuento.class);
        when(tipo.getIdTipoDescuento()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("Descuento.buscarActivosPorTipoYNombre", Descuento.class)).thenReturn(query);
        when(query.setParameter("tipo", tipo)).thenReturn(query);
        when(query.setParameter("texto", "%%")).thenReturn(query);
        when(query.setParameter(eq("hoy"), any(Date.class))).thenReturn(query);
        when(query.setMaxResults(5)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        List<Descuento> resultado = dao.buscarActivosPorTipoYNombre(tipo, null, 5);

        assertTrue(resultado.isEmpty());
        verify(query).setParameter("texto", "%%");
    }

    @Test
    @DisplayName("buscarPorTipoYNombre: Propaga AccesoDatosException envolviendo PersistenceException")
    void testBuscarPorTipoYNombreLanzaExcepcionBD() {
        TipoDescuento tipo = mock(TipoDescuento.class);
        when(tipo.getIdTipoDescuento()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("Descuento.buscarActivosPorTipoYNombre", Descuento.class)).thenReturn(query);
        when(query.setParameter("tipo", tipo)).thenReturn(query);
        when(query.setParameter("texto", "%%")).thenReturn(query);
        when(query.setParameter(eq("hoy"), any(Date.class))).thenReturn(query);
        when(query.setMaxResults(5)).thenReturn(query);
        
        PersistenceException causa = new PersistenceException("Error conexión BD");
        when(query.getResultList()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () -> 
            dao.buscarActivosPorTipoYNombre(tipo, null, 5)
        );
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("ordenar: Retorna la lista de criterios de ordenamiento por nombre e ID")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenarRetornaListaCriterios() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<Descuento> root = mock(Root.class);
        
        Path pathNombre = mock(Path.class);
        Path pathId = mock(Path.class);
        Order orderNombre = mock(Order.class);
        Order orderId = mock(Order.class);

        when(root.get("nombre")).thenReturn(pathNombre);
        when(root.get("idDescuento")).thenReturn(pathId);
        when(cb.asc(pathNombre)).thenReturn(orderNombre);
        when(cb.asc(pathId)).thenReturn(orderId);

        List<Order> resultado = dao.ordenar(cb, root);

        assertEquals(2, resultado.size());
        assertEquals(orderNombre, resultado.get(0));
        assertEquals(orderId, resultado.get(1));
    }
}