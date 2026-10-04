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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class CaracteristicaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Caracteristica> typedQuery;

    @InjectMocks
    private CaracteristicaDAO caracteristicaDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertNotNull(caracteristicaDAO.getEntityManager());
        assertEquals(em, caracteristicaDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByActivo debe retornar la lista correspondiente al filtro")
    void testFindByActivo() {
        Boolean activo = true;
        List<Caracteristica> esperados = Collections.singletonList(new Caracteristica());

        when(em.createNamedQuery("Caracteristica.findByActivo", Caracteristica.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("activo", activo)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Caracteristica> resultado = caracteristicaDAO.findByActivo(activo);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("Caracteristica.findByActivo", Caracteristica.class);
        verify(typedQuery).setParameter("activo", activo);
    }

    @Test
    @DisplayName("findByNombre consulta con el parámetro nombre y devuelve el resultado")
    void testFindByNombre() {
        String nombre = "Tamaño";
        List<Caracteristica> esperados = Collections.singletonList(new Caracteristica());

        when(em.createNamedQuery("Caracteristica.findByNombre", Caracteristica.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("nombre", nombre)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Caracteristica> resultado = caracteristicaDAO.findByNombre(nombre);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("Caracteristica.findByNombre", Caracteristica.class);
        verify(typedQuery).setParameter("nombre", nombre);
    }

    @Test
    @DisplayName("findByIdTipoCaracteristica retorna lista vacía si el parametro es nulo")
    void testFindByIdTipoCaracteristicaNull() {
        List<Caracteristica> resultado = caracteristicaDAO.findByIdTipoCaracteristica(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("findByIdTipoCaracteristica retorna lista vacía si el ID del TipoCaracteristica es nulo")
    void testFindByIdTipoCaracteristicaIdNull() {
        TipoCaracteristica tipo = new TipoCaracteristica();
        tipo.setIdTipoCaracteristica(null);

        List<Caracteristica> resultado = caracteristicaDAO.findByIdTipoCaracteristica(tipo);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("findByIdTipoCaracteristica ejecuta query y retorna la lista correspondiente")
    void testFindByIdTipoCaracteristicaExitoso() {
        TipoCaracteristica tipo = new TipoCaracteristica();
        tipo.setIdTipoCaracteristica(UUID.randomUUID());
        List<Caracteristica> esperados = List.of(new Caracteristica());
        String queryExpected = "SELECT c FROM Caracteristica c WHERE c.idTipoCaracteristica = :idTipoCaracteristica";

        when(em.createQuery(queryExpected, Caracteristica.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("idTipoCaracteristica", tipo)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Caracteristica> resultado = caracteristicaDAO.findByIdTipoCaracteristica(tipo);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createQuery(queryExpected, Caracteristica.class);
        verify(typedQuery).setParameter("idTipoCaracteristica", tipo);
    }
}