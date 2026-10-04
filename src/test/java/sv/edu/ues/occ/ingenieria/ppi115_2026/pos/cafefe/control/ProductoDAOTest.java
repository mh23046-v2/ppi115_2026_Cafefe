package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class ProductoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Producto> typedQuery;

    @InjectMocks
    private ProductoDAO productoDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertNotNull(productoDAO.getEntityManager());
        assertEquals(em, productoDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByActivo debe retornar la lista de productos correspondiente al filtro")
    void testFindByActivo() {
        Boolean activo = true;
        List<Producto> esperados = Collections.singletonList(new Producto());

        when(em.createNamedQuery("Producto.findByActivo", Producto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("activo", activo)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Producto> resultado = productoDAO.findByActivo(activo);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("Producto.findByActivo", Producto.class);
        verify(typedQuery).setParameter("activo", activo);
    }

    @Test
    @DisplayName("findByNombre consulta con el parámetro nombre y devuelve el resultado")
    void testFindByNombre() {
        String nombre = "Café Americano";
        List<Producto> esperados = Collections.singletonList(new Producto());

        when(em.createNamedQuery("Producto.findByNombre", Producto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("nombre", nombre)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Producto> resultado = productoDAO.findByNombre(nombre);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("Producto.findByNombre", Producto.class);
        verify(typedQuery).setParameter("nombre", nombre);
    }
}