package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaracteristicaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Caracteristica> query;

    @InjectMocks
    private CaracteristicaDAO dao;

    @Test
    @DisplayName("buscarActivasPorTipoYNombre: Lanza IllegalArgumentException con argumentos inválidos")
    void testBuscarActivasPorTipoYNombreArgumentosInvalidos() {
        TipoCaracteristica tipoValido = new TipoCaracteristica();
        tipoValido.setIdTipoCaracteristica(UUID.randomUUID()); // Corrección del UUID
        TipoCaracteristica tipoSinId = new TipoCaracteristica();

        assertThrows(IllegalArgumentException.class, () -> dao.buscarActivasPorTipoYNombre(null, "txt", 10));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarActivasPorTipoYNombre(tipoSinId, "txt", 10));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarActivasPorTipoYNombre(tipoValido, "txt", 0));
    }

    @Test
    @DisplayName("buscarActivasPorTipoYNombre: Ejecuta la consulta con los parámetros limpios y válidos")
    void testBuscarActivasPorTipoYNombreParametrosValidos() {
        TipoCaracteristica tipo = new TipoCaracteristica();
        tipo.setIdTipoCaracteristica(UUID.randomUUID());

        when(em.createNamedQuery("Caracteristica.buscarActivasPorTipoYNombre", Caracteristica.class)).thenReturn(query);
        when(query.setParameter("tipo", tipo)).thenReturn(query);
        when(query.setParameter("texto", "%fresa%")).thenReturn(query);
        when(query.setMaxResults(5)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Caracteristica()));

        List<Caracteristica> resultado = dao.buscarActivasPorTipoYNombre(tipo, " Fresa  ", 5);

        assertFalse(resultado.isEmpty());
        verify(query).setParameter("tipo", tipo);
        verify(query).setParameter("texto", "%fresa%");
        verify(query).setMaxResults(5);
    }

    @Test
    @DisplayName("buscarActivasPorTipoYNombre: Construye el patrón vacío '%%' cuando el texto es null")
    void testBuscarActivasPorTipoYNombreTextoNull() {
        TipoCaracteristica tipo = new TipoCaracteristica();
        tipo.setIdTipoCaracteristica(UUID.randomUUID());

        when(em.createNamedQuery("Caracteristica.buscarActivasPorTipoYNombre", Caracteristica.class)).thenReturn(query);
        when(query.setParameter("tipo", tipo)).thenReturn(query);
        when(query.setParameter("texto", "%%")).thenReturn(query);
        when(query.setMaxResults(5)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        List<Caracteristica> resultado = dao.buscarActivasPorTipoYNombre(tipo, null, 5);

        assertTrue(resultado.isEmpty());
        verify(query).setParameter("texto", "%%");
    }

    @Test
    @DisplayName("buscarActivasPorTipoYNombre: Propaga AccesoDatosException conservando la causa original")
    void testBuscarActivasPorTipoYNombreLanzaExcepcionBD() {
        TipoCaracteristica tipo = new TipoCaracteristica();
        tipo.setIdTipoCaracteristica(UUID.randomUUID());

        when(em.createNamedQuery("Caracteristica.buscarActivasPorTipoYNombre", Caracteristica.class)).thenReturn(query);
        when(query.setParameter("tipo", tipo)).thenReturn(query);
        when(query.setParameter("texto", "%%")).thenReturn(query);
        when(query.setMaxResults(5)).thenReturn(query);
        
        PersistenceException causa = new PersistenceException("Error conexión BD");
        when(query.getResultList()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () -> 
            dao.buscarActivasPorTipoYNombre(tipo, null, 5)
        );
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("ordenar: Retorna la lista de criterios de ordenamiento por nombre e ID")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenarRetornaListaCriterios() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<Caracteristica> root = mock(Root.class);
        
        Path pathNombre = mock(Path.class);
        Path pathId = mock(Path.class);
        Order orderNombre = mock(Order.class);
        Order orderId = mock(Order.class);

        when(root.get("nombre")).thenReturn(pathNombre);
        when(root.get("idCaracteristica")).thenReturn(pathId);
        when(cb.asc(pathNombre)).thenReturn(orderNombre);
        when(cb.asc(pathId)).thenReturn(orderId);

        List<Order> resultado = dao.ordenar(cb, root);

        assertEquals(2, resultado.size());
        assertEquals(orderNombre, resultado.get(0));
        assertEquals(orderId, resultado.get(1));
    }
}