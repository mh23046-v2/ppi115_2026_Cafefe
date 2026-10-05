package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractDataAccessTest {

    // Entidad ficticia para parametrizar el DAO genérico
    public static class EntidadPrueba {

        private UUID id;

        public EntidadPrueba(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }
    }

    // Subclase concreta para probar la clase abstracta AbstractDataAccess
    public static class TestAbstractDataAccess extends AbstractDataAccess<EntidadPrueba> {

        private final EntityManager em;

        public TestAbstractDataAccess(EntityManager em) {
            super(EntidadPrueba.class);
            this.em = em;
        }

        @Override
        protected EntityManager getEntityManager() {
            return em;
        }
    }

    @Mock
    private EntityManager mockEntityManager;

    @Mock
    private CriteriaBuilder mockCriteriaBuilder;

    @Mock
    private CriteriaQuery<EntidadPrueba> mockCriteriaQuery;

    @Mock
    private CriteriaQuery<Long> mockCriteriaQueryLong;

    @Mock
    private Root<EntidadPrueba> mockRoot;

    @Mock
    private TypedQuery<EntidadPrueba> mockTypedQuery;

    @Mock
    private TypedQuery<Long> mockTypedQueryLong;

    private TestAbstractDataAccess dao;
    private EntidadPrueba entidadPrueba;
    private UUID idPrueba;

    @BeforeEach
    public void setUp() {
        dao = new TestAbstractDataAccess(mockEntityManager);
        idPrueba = UUID.randomUUID();
        entidadPrueba = new EntidadPrueba(idPrueba);
    }

    @Nested
    @DisplayName("Pruebas de Operaciones CRUD Básicas")
    class CrudOperacionesTests {

        @Test
        @DisplayName("create() con entidad válida debe llamar a persist()")
        public void testCreateExito() {
            dao.create(entidadPrueba);
            verify(mockEntityManager, times(1)).persist(entidadPrueba);
        }

        @Test
        @DisplayName("create() con entidad nula lanza IllegalArgumentException")
        public void testCreateNulo() {
            assertThrows(IllegalArgumentException.class, () -> dao.create(null));
            verify(mockEntityManager, never()).persist(any());
        }

        @Test
        @DisplayName("modify() con entidad válida debe llamar a merge() y devolver entidad")
        public void testModifyExito() {
            when(mockEntityManager.merge(entidadPrueba)).thenReturn(entidadPrueba);

            EntidadPrueba resultado = dao.modify(entidadPrueba);

            assertNotNull(resultado);
            assertEquals(entidadPrueba, resultado);
            verify(mockEntityManager, times(1)).merge(entidadPrueba);
        }

        @Test
        @DisplayName("modify() con entidad nula lanza IllegalArgumentException")
        public void testModifyNulo() {
            assertThrows(IllegalArgumentException.class, () -> dao.modify(null));
            verify(mockEntityManager, never()).merge(any());
        }

        @Test
        @DisplayName("delete() con entidad adjunta (managed) debe llamar directamente a remove()")
        public void testDeleteEntidadManaged() {
            when(mockEntityManager.contains(entidadPrueba)).thenReturn(true);

            dao.delete(entidadPrueba);

            verify(mockEntityManager, times(1)).remove(entidadPrueba);
            verify(mockEntityManager, never()).merge(any());
        }

        @Test
        @DisplayName("delete() con entidad desconectada (detached) debe hacer merge() y luego remove()")
        public void testDeleteEntidadDetached() {
            EntidadPrueba entidadReconectada = new EntidadPrueba(idPrueba);
            when(mockEntityManager.contains(entidadPrueba)).thenReturn(false);
            when(mockEntityManager.merge(entidadPrueba)).thenReturn(entidadReconectada);

            dao.delete(entidadPrueba);

            verify(mockEntityManager, times(1)).merge(entidadPrueba);
            verify(mockEntityManager, times(1)).remove(entidadReconectada);
        }

        @Test
        @DisplayName("delete() con entidad nula lanza IllegalArgumentException")
        public void testDeleteNulo() {
            assertThrows(IllegalArgumentException.class, () -> dao.delete(null));
            verify(mockEntityManager, never()).remove(any());
        }

        @Test
        @DisplayName("findById() con ID válido devuelve la entidad")
        public void testFindByIdExito() {
            when(mockEntityManager.find(EntidadPrueba.class, idPrueba)).thenReturn(entidadPrueba);

            EntidadPrueba resultado = dao.findById(idPrueba);

            assertNotNull(resultado);
            assertEquals(idPrueba, resultado.getId());
            verify(mockEntityManager, times(1)).find(EntidadPrueba.class, idPrueba);
        }

        @Test
        @DisplayName("findById() con ID nulo lanza IllegalArgumentException")
        public void testFindByIdNulo() {
            assertThrows(IllegalArgumentException.class, () -> dao.findById(null));
            verify(mockEntityManager, never()).find(any(), any());
        }
    }

    @Nested
    @DisplayName("Pruebas de Consultas mediante Criteria API")
    class CriteriaQueriesTests {

        @Test
        @DisplayName("findAll() ejecuta la consulta CriteriaQuery y devuelve la lista")
        public void testFindAll() {
            when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
            when(mockCriteriaBuilder.createQuery(EntidadPrueba.class)).thenReturn(mockCriteriaQuery);
            when(mockCriteriaQuery.from(EntidadPrueba.class)).thenReturn(mockRoot);
            when(mockCriteriaQuery.select(mockRoot)).thenReturn(mockCriteriaQuery);
            when(mockEntityManager.createQuery(mockCriteriaQuery)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getResultList()).thenReturn(List.of(entidadPrueba));

            List<EntidadPrueba> resultado = dao.findAll();

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals(entidadPrueba, resultado.get(0));
        }

        @Test
        @DisplayName("findRange() aplica setFirstResult y setMaxResults para paginación")
        public void testFindRange() {
            when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
            when(mockCriteriaBuilder.createQuery(EntidadPrueba.class)).thenReturn(mockCriteriaQuery);
            when(mockCriteriaQuery.from(EntidadPrueba.class)).thenReturn(mockRoot);
            when(mockCriteriaQuery.select(mockRoot)).thenReturn(mockCriteriaQuery);
            when(mockEntityManager.createQuery(mockCriteriaQuery)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.setFirstResult(5)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.setMaxResults(10)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getResultList()).thenReturn(List.of(entidadPrueba));

            List<EntidadPrueba> resultado = dao.findRange(5, 10);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(mockTypedQuery, times(1)).setFirstResult(5);
            verify(mockTypedQuery, times(1)).setMaxResults(10);
        }

        @Test
        @DisplayName("count() ejecuta cb.count() y devuelve el número total de registros")
        public void testCount() {
            when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
            when(mockCriteriaBuilder.createQuery(Long.class)).thenReturn(mockCriteriaQueryLong);
            when(mockCriteriaQueryLong.from(EntidadPrueba.class)).thenReturn(mockRoot);
            when(mockEntityManager.createQuery(mockCriteriaQueryLong)).thenReturn(mockTypedQueryLong);
            when(mockTypedQueryLong.getSingleResult()).thenReturn(15L);

            long total = dao.count();

            assertEquals(15L, total);
            verify(mockCriteriaBuilder, times(1)).count(mockRoot);
        }
    }
}
