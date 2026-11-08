package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractDataAccessTest {

    public static class EntidadPrueba {

        private UUID id;

        public EntidadPrueba(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }
    }

    public static class TestAbstractDataAccess extends AbstractDataAccess<EntidadPrueba> {

        private final EntityManager em;
        private List<Order> ordenPersonalizado = List.of();

        public TestAbstractDataAccess(EntityManager em) {
            super(EntidadPrueba.class);
            this.em = em;
        }

        public void setOrdenPersonalizado(List<Order> ordenPersonalizado) {
            this.ordenPersonalizado = ordenPersonalizado;
        }

        @Override
        protected EntityManager getEntityManager() {
            return em;
        }

        @Override
        protected List<Order> ordenar(CriteriaBuilder cb, Root<EntidadPrueba> root) {
            return ordenPersonalizado;
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
        @DisplayName("create() con entidad válida debe llamar a persist() y flush()")
        public void testCreateExito() {
            dao.create(entidadPrueba);
            verify(mockEntityManager, times(1)).persist(entidadPrueba);
            verify(mockEntityManager, times(1)).flush();
        }

        @Test
        @DisplayName("create() con entidad nula lanza IllegalArgumentException")
        public void testCreateNulo() {
            assertThrows(IllegalArgumentException.class, () -> dao.create(null));
            verify(mockEntityManager, never()).persist(any());
            verify(mockEntityManager, never()).flush();
        }

        @Test
        @DisplayName("modify() con entidad válida llama a merge(), flush() y devuelve entidad")
        public void testModifyExito() {
            when(mockEntityManager.merge(entidadPrueba)).thenReturn(entidadPrueba);

            EntidadPrueba resultado = dao.modify(entidadPrueba);

            assertNotNull(resultado);
            assertEquals(entidadPrueba, resultado);
            verify(mockEntityManager, times(1)).merge(entidadPrueba);
            verify(mockEntityManager, times(1)).flush();
        }

        @Test
        @DisplayName("modify() con entidad nula lanza IllegalArgumentException")
        public void testModifyNulo() {
            assertThrows(IllegalArgumentException.class, () -> dao.modify(null));
            verify(mockEntityManager, never()).merge(any());
            verify(mockEntityManager, never()).flush();
        }

        @Test
        @DisplayName("delete() con entidad adjunta llama a remove() y flush()")
        public void testDeleteEntidadManaged() {
            when(mockEntityManager.contains(entidadPrueba)).thenReturn(true);

            dao.delete(entidadPrueba);

            verify(mockEntityManager, times(1)).remove(entidadPrueba);
            verify(mockEntityManager, never()).merge(any());
            verify(mockEntityManager, times(1)).flush();
        }

        @Test
        @DisplayName("delete() con entidad desconectada hace merge(), remove() y flush()")
        public void testDeleteEntidadDetached() {
            EntidadPrueba entidadReconectada = new EntidadPrueba(idPrueba);
            when(mockEntityManager.contains(entidadPrueba)).thenReturn(false);
            when(mockEntityManager.merge(entidadPrueba)).thenReturn(entidadReconectada);

            dao.delete(entidadPrueba);

            verify(mockEntityManager, times(1)).merge(entidadPrueba);
            verify(mockEntityManager, times(1)).remove(entidadReconectada);
            verify(mockEntityManager, times(1)).flush();
        }

        @Test
        @DisplayName("delete() con entidad nula lanza IllegalArgumentException")
        public void testDeleteNulo() {
            assertThrows(IllegalArgumentException.class, () -> dao.delete(null));
            verify(mockEntityManager, never()).remove(any());
            verify(mockEntityManager, never()).flush();
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

        @Test
        @DisplayName("findById() devuelve null cuando el registro no existe")
        void testFindByIdNoExiste() {
            when(mockEntityManager.find(EntidadPrueba.class, idPrueba)).thenReturn(null);
            assertNull(dao.findById(idPrueba));
        }

        @Test
        @DisplayName("findById() no envuelve errores que no son de persistencia")
        void testFindByIdErrorDeProgramacion() {
            when(mockEntityManager.find(EntidadPrueba.class, idPrueba))
                    .thenThrow(new IllegalArgumentException("tipo de id incorrecto"));
            assertThrows(IllegalArgumentException.class, () -> dao.findById(idPrueba));
        }
    }

    @Nested
    @DisplayName("Pruebas de Consultas mediante Criteria API y Validación")
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
        @DisplayName("findRange() aplica setFirstResult y setMaxResults correctamente")
        public void testFindRangeExito() {
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
        @DisplayName("findRange() aplica la cláusula orderBy cuando ordenar() devuelve elementos")
        void testFindRangeAplicaOrderBy() {
            Order mockOrder = mock(Order.class);
            dao.setOrdenPersonalizado(List.of(mockOrder));

            when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
            when(mockCriteriaBuilder.createQuery(EntidadPrueba.class)).thenReturn(mockCriteriaQuery);
            when(mockCriteriaQuery.from(EntidadPrueba.class)).thenReturn(mockRoot);
            when(mockCriteriaQuery.select(mockRoot)).thenReturn(mockCriteriaQuery);
            when(mockEntityManager.createQuery(mockCriteriaQuery)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.setFirstResult(0)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.setMaxResults(10)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getResultList()).thenReturn(List.of(entidadPrueba));

            dao.findRange(0, 10);

            verify(mockCriteriaQuery, times(1)).orderBy(List.of(mockOrder));
        }

        @Test
        @DisplayName("findRange() omite la cláusula orderBy cuando ordenar() devuelve null o lista vacía")
        void testFindRangeOmiteOrderByCuandoEsVacioONull() {
            dao.setOrdenPersonalizado(null);

            when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
            when(mockCriteriaBuilder.createQuery(EntidadPrueba.class)).thenReturn(mockCriteriaQuery);
            when(mockCriteriaQuery.from(EntidadPrueba.class)).thenReturn(mockRoot);
            when(mockCriteriaQuery.select(mockRoot)).thenReturn(mockCriteriaQuery);
            when(mockEntityManager.createQuery(mockCriteriaQuery)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.setFirstResult(0)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.setMaxResults(10)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getResultList()).thenReturn(List.of(entidadPrueba));

            dao.findRange(0, 10);

            verify(mockCriteriaQuery, never()).orderBy(anyList());
        }

        @Test
        @DisplayName("findRange() con first negativo lanza IllegalArgumentException")
        public void testFindRangeFirstInvalido() {
            assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 10));
            verify(mockEntityManager, never()).getCriteriaBuilder();
        }

        @Test
        @DisplayName("findRange() con max menor o igual a cero lanza IllegalArgumentException")
        public void testFindRangeMaxInvalido() {
            assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, 0));
            assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, -5));
            verify(mockEntityManager, never()).getCriteriaBuilder();
        }

        @Test
        @DisplayName("count() ejecuta cb.count() y devuelve el número total de registros")
        public void testCountExito() {
            when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
            when(mockCriteriaBuilder.createQuery(Long.class)).thenReturn(mockCriteriaQueryLong);
            when(mockCriteriaQueryLong.from(EntidadPrueba.class)).thenReturn(mockRoot);
            when(mockEntityManager.createQuery(mockCriteriaQueryLong)).thenReturn(mockTypedQueryLong);
            when(mockTypedQueryLong.getSingleResult()).thenReturn(15L);

            long total = dao.count();

            assertEquals(15L, total);
            verify(mockCriteriaBuilder, times(1)).count(mockRoot);
        }

        @Test
        @DisplayName("patronLike() limpia espacios, pasa a minúsculas y maneja nulos como cadena vacía")
        void testPatronLikeGeneracionCorrecta() {
            assertEquals("%caf%", AbstractDataAccess.patronLike("  CaF "));
            assertEquals("%%", AbstractDataAccess.patronLike(null));
            assertEquals("%%", AbstractDataAccess.patronLike("   "));
        }
    }

    @Nested
    @DisplayName("Pruebas de Captura y Conversión de Excepciones de Persistencia")
    class ExcepcionesPersistenciaTests {

        @Test
        @DisplayName("create() envuelve PersistenceException en AccesoDatosException")
        public void testCreatePersistenceException() {
            doThrow(new PersistenceException("Error en BD"))
                    .when(mockEntityManager).persist(entidadPrueba);

            AccesoDatosException ex = assertThrows(
                    AccesoDatosException.class,
                    () -> dao.create(entidadPrueba)
            );

            assertEquals("No se pudo crear el registro", ex.getMessage());
            assertInstanceOf(PersistenceException.class, ex.getCause());
        }

        @Test
        @DisplayName("modify() envuelve PersistenceException en AccesoDatosException")
        public void testModifyPersistenceException() {
            when(mockEntityManager.merge(entidadPrueba))
                    .thenThrow(new PersistenceException("Error al actualizar"));

            AccesoDatosException ex = assertThrows(
                    AccesoDatosException.class,
                    () -> dao.modify(entidadPrueba)
            );

            assertEquals("No se pudo modificar el registro", ex.getMessage());
            assertInstanceOf(PersistenceException.class, ex.getCause());
        }

        @Test
        @DisplayName("delete() envuelve PersistenceException en AccesoDatosException")
        public void testDeletePersistenceException() {
            when(mockEntityManager.contains(entidadPrueba)).thenReturn(true);
            doThrow(new PersistenceException("Violación de llave foránea"))
                    .when(mockEntityManager).remove(entidadPrueba);

            AccesoDatosException ex = assertThrows(
                    AccesoDatosException.class,
                    () -> dao.delete(entidadPrueba)
            );

            assertEquals("No se pudo eliminar el registro", ex.getMessage());
            assertInstanceOf(PersistenceException.class, ex.getCause());
        }

        @Test
        @DisplayName("findById() envuelve PersistenceException en AccesoDatosException")
        public void testFindByIdPersistenceException() {
            when(mockEntityManager.find(EntidadPrueba.class, idPrueba))
                    .thenThrow(new PersistenceException("Fallo de conexión"));

            AccesoDatosException ex = assertThrows(
                    AccesoDatosException.class,
                    () -> dao.findById(idPrueba)
            );

            assertEquals("No se pudo consultar el registro por id", ex.getMessage());
            assertInstanceOf(PersistenceException.class, ex.getCause());
        }

        @Test
        @DisplayName("findRange() envuelve PersistenceException en AccesoDatosException")
        public void testFindRangePersistenceException() {
            when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
            when(mockCriteriaBuilder.createQuery(EntidadPrueba.class)).thenReturn(mockCriteriaQuery);
            when(mockCriteriaQuery.from(EntidadPrueba.class)).thenReturn(mockRoot);
            when(mockCriteriaQuery.select(mockRoot)).thenReturn(mockCriteriaQuery);
            when(mockEntityManager.createQuery(mockCriteriaQuery)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.setFirstResult(0)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.setMaxResults(10)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getResultList())
                    .thenThrow(new PersistenceException("Error en consulta paginada"));

            AccesoDatosException ex = assertThrows(
                    AccesoDatosException.class,
                    () -> dao.findRange(0, 10)
            );

            assertEquals("No se pudieron consultar los registros paginados", ex.getMessage());
            assertInstanceOf(PersistenceException.class, ex.getCause());
        }

        @Test
        @DisplayName("count() envuelve PersistenceException en AccesoDatosException")
        public void testCountPersistenceException() {
            when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
            when(mockCriteriaBuilder.createQuery(Long.class)).thenReturn(mockCriteriaQueryLong);
            when(mockCriteriaQueryLong.from(EntidadPrueba.class)).thenReturn(mockRoot);
            when(mockEntityManager.createQuery(mockCriteriaQueryLong)).thenReturn(mockTypedQueryLong);
            when(mockTypedQueryLong.getSingleResult())
                    .thenThrow(new PersistenceException("Error al contar registros"));

            AccesoDatosException ex = assertThrows(
                    AccesoDatosException.class,
                    () -> dao.count()
            );

            assertEquals("No se pudo contar el total de registros", ex.getMessage());
            assertInstanceOf(PersistenceException.class, ex.getCause());
        }

        @Test
        @DisplayName("create() envuelve el error que ocurre en flush()")
        void testCreateFalloEnFlush() {
            PersistenceException causa = new PersistenceException("llave duplicada");
            doThrow(causa).when(mockEntityManager).flush();

            AccesoDatosException ex = assertThrows(AccesoDatosException.class,
                    () -> dao.create(entidadPrueba));

            assertSame(causa, ex.getCause());
            InOrder orden = inOrder(mockEntityManager);
            orden.verify(mockEntityManager).persist(entidadPrueba);
            orden.verify(mockEntityManager).flush();
        }

        @Test
        @DisplayName("modify() envuelve el error que ocurre en flush()")
        void testModifyFalloEnFlush() {
            PersistenceException causa = new PersistenceException("restricción violada");
            when(mockEntityManager.merge(entidadPrueba)).thenReturn(entidadPrueba);
            doThrow(causa).when(mockEntityManager).flush();

            AccesoDatosException ex = assertThrows(AccesoDatosException.class,
                    () -> dao.modify(entidadPrueba));

            assertSame(causa, ex.getCause());
        }

        @Test
        @DisplayName("delete() envuelve el error que ocurre en flush()")
        void testDeleteFalloEnFlush() {
            PersistenceException causa = new PersistenceException("registro referenciado");
            when(mockEntityManager.contains(entidadPrueba)).thenReturn(true);
            doThrow(causa).when(mockEntityManager).flush();

            AccesoDatosException ex = assertThrows(AccesoDatosException.class,
                    () -> dao.delete(entidadPrueba));

            assertSame(causa, ex.getCause());
        }

        @Test
        @DisplayName("findAll() envuelve PersistenceException en AccesoDatosException")
        void testFindAllPersistenceException() {
            when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
            when(mockCriteriaBuilder.createQuery(EntidadPrueba.class)).thenReturn(mockCriteriaQuery);
            when(mockCriteriaQuery.from(EntidadPrueba.class)).thenReturn(mockRoot);
            when(mockCriteriaQuery.select(mockRoot)).thenReturn(mockCriteriaQuery);
            when(mockEntityManager.createQuery(mockCriteriaQuery)).thenReturn(mockTypedQuery);
            PersistenceException causa = new PersistenceException("sin conexión");
            when(mockTypedQuery.getResultList()).thenThrow(causa);

            AccesoDatosException ex = assertThrows(AccesoDatosException.class, () -> dao.findAll());

            assertSame(causa, ex.getCause());
        }
    }
}