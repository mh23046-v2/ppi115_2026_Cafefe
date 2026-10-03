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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class TipoProductoDAOTest {
    
     @Mock
    private EntityManager em;
 
    @Mock
    private TypedQuery<TipoProducto> typedQuery;
 
    @InjectMocks
    private TipoProductoDAO tipoProductoDAO;
 
    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertEquals(em, tipoProductoDAO.getEntityManager());
    }
 
    @Test
    @DisplayName("findByActivo usa la NamedQuery con el parámetro activo")
    void testFindByActivo() {
        List<TipoProducto> esperados = Collections.singletonList(new TipoProducto(UUID.randomUUID()));
        when(em.createNamedQuery("TipoProducto.findByActivo", TipoProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("activo", true)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);
 
        List<TipoProducto> resultado = tipoProductoDAO.findByActivo(true);
 
        assertEquals(esperados, resultado);
        verify(typedQuery).setParameter("activo", true);
    }
 
    @Test
    @DisplayName("findByNombre usa la NamedQuery con el parámetro nombre")
    void testFindByNombre() {
        List<TipoProducto> esperados = Collections.singletonList(new TipoProducto(UUID.randomUUID()));
        when(em.createNamedQuery("TipoProducto.findByNombre", TipoProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("nombre", "Bebidas")).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);
 
        List<TipoProducto> resultado = tipoProductoDAO.findByNombre("Bebidas");
 
        assertEquals(esperados, resultado);
        verify(typedQuery).setParameter("nombre", "Bebidas");
    }
 
    @Test
    @DisplayName("create con null lanza error y no toca la base de datos")
    void testCreateNulo() {
        assertThrows(IllegalArgumentException.class, () -> tipoProductoDAO.create(null));
 
        verifyNoInteractions(em);
    }
 
    @Test
    @DisplayName("delete de una entidad desconectada: primero merge y luego remove")
    void testDeleteEntidadDesconectada() {
        TipoProducto tipo = new TipoProducto(UUID.randomUUID());
        TipoProducto adjunto = new TipoProducto(tipo.getIdTipoProducto());
        when(em.contains(tipo)).thenReturn(false); // viene de la pantalla, JPA no la está siguiendo
        when(em.merge(tipo)).thenReturn(adjunto);
 
        tipoProductoDAO.delete(tipo);
 
        verify(em).remove(adjunto);
    }
}
