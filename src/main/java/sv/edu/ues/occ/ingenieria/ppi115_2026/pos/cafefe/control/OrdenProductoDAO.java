package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

@Stateless
public class OrdenProductoDAO extends AbstractDataAccess<OrdenProducto> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public OrdenProductoDAO() {
        super(OrdenProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<OrdenProducto> root) {
        return List.of(
                cb.asc(root.get("idProducto").get("nombre"))
        );
    }

    // Obtiene de forma paginada los productos asociados a una orden.
    public List<OrdenProducto> findByOrden(Orden orden, int first, int max) {
        if (orden == null || orden.getIdOrden() == null) {
            throw new IllegalArgumentException("La orden no puede ser nula ni carecer de ID");
        }
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        return ejecutar("No se pudieron cargar los productos de la orden", ()
                -> em.createNamedQuery("OrdenProducto.findByOrden", OrdenProducto.class)
                        .setParameter("orden", orden)
                        .setFirstResult(first)
                        .setMaxResults(max)
                        .getResultList()
        );
    }

    // Conteo total de ítems agregados a la orden para el LazyDataModel.
    public int countByOrden(Orden orden) {
        if (orden == null || orden.getIdOrden() == null) {
            return 0;
        }
        return ejecutar("No se pudo contar los productos de la orden", () -> {
            Long count = em.createNamedQuery("OrdenProducto.countByOrden", Long.class)
                    .setParameter("orden", orden)
                    .getSingleResult();
            return count != null ? count.intValue() : 0;
        });
    }

    // Calcula la suma acumulada de los precios de los productos en la orden.
    public BigDecimal totalByOrden(Orden orden) {
        if (orden == null || orden.getIdOrden() == null) {
            return BigDecimal.ZERO;
        }
        return ejecutar("No se pudo calcular el total de la orden", () -> {
            BigDecimal total = em.createNamedQuery("OrdenProducto.sumPrecioByOrden", BigDecimal.class)
                    .setParameter("orden", orden)
                    .getSingleResult();
            return total == null ? BigDecimal.ZERO : total;
        });
    }

    // Descuentos candidatos asignados a los productos presentes en la orden.
    public List<DescuentoProducto> findDescuentosDeProductosEnOrden(Orden orden) {
        if (orden == null || orden.getIdOrden() == null) {
            throw new IllegalArgumentException("La orden no puede ser nula ni carecer de ID");
        }
        return ejecutar("No se pudieron cargar los descuentos aplicables a la orden", ()
                -> em.createNamedQuery("DescuentoProducto.findByProductosEnOrden", DescuentoProducto.class)
                        .setParameter("orden", orden)
                        .getResultList()
        );
    }

    /** Productos de órdenes que todavía no están en una factura (o solo en anuladas). */
    public List<OrdenProducto> findPendientesDeFacturar(String texto, int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar los productos pendientes de facturar", ()
                -> em.createNamedQuery("OrdenProducto.findPendientesDeFacturar", OrdenProducto.class)
                        .setParameter("texto", patronLike(texto))
                        .setParameter("anulada", ReglasFactura.ANULADA)
                        .setMaxResults(max)
                        .getResultList()
        );
    }

    /**
     * Aplica un descuento a todas las líneas de la orden cuyo producto lo tenga,
     * en UNA sola transacción (método de un EJB @Stateless): o se actualizan
     * todas las líneas o ninguna. El precio se calcula sobre el precio sugerido
     * del producto con el porcentaje efectivo de cada DescuentoProducto.
     *
     * @return cantidad de líneas actualizadas
     */
    public int aplicarDescuento(Orden orden, List<DescuentoProducto> vigentes) {
        if (orden == null || orden.getIdOrden() == null) {
            throw new IllegalArgumentException("La orden no puede ser nula ni carecer de ID");
        }
        if (vigentes == null || vigentes.isEmpty()) {
            throw new IllegalArgumentException("No hay descuentos vigentes para aplicar");
        }
        Map<UUID, Integer> porcentajePorProducto = new HashMap<>();
        for (DescuentoProducto dp : vigentes) {
            if (dp != null && dp.getIdProducto() != null && dp.getIdProducto().getIdProducto() != null) {
                porcentajePorProducto.put(dp.getIdProducto().getIdProducto(), ReglasOrden.porcentajeEfectivo(dp));
            }
        }
        return ejecutar("No se pudo aplicar el descuento a la orden", () -> {
            List<OrdenProducto> lineas = em.createNamedQuery("OrdenProducto.findByOrden", OrdenProducto.class)
                    .setParameter("orden", orden)
                    .getResultList();
            int aplicados = 0;
            for (OrdenProducto op : lineas) {
                Producto producto = op.getIdProducto();
                Integer porcentaje = producto == null ? null : porcentajePorProducto.get(producto.getIdProducto());
                if (porcentaje != null && producto.getPrecioSugerido() != null) {
                    // La línea está administrada por el EntityManager: el cambio se guarda al confirmar.
                    op.setPrecio(ReglasOrden.precioConDescuento(producto.getPrecioSugerido(), porcentaje));
                    aplicados++;
                }
            }
            em.flush();
            return aplicados;
        });
    }
    static final String JPQL_PENDIENTES_DE_ORDENES = "SELECT op FROM OrdenProducto op JOIN FETCH op.idProducto p"
            + " WHERE op.idOrden IN :ordenes"
            + " AND NOT EXISTS (SELECT f FROM FacturaOrdenProducto f WHERE f.idOrdenProducto = op"
            + " AND (f.idFactura.estado IS NULL OR f.idFactura.estado <> :anulada))"
            + " ORDER BY p.nombre, op.idOrdenProducto";

    /**
     * Productos sin cobrar de varias órdenes en UNA sola consulta (en lugar de
     * una consulta por orden). Si la lista viene vacía no consulta la base.
     */
    public List<OrdenProducto> findPendientesDeOrdenes(List<Orden> ordenes) {
        if (ordenes == null || ordenes.isEmpty()) {
            return List.of();
        }
        return ejecutar("No se pudieron consultar los productos pendientes de las órdenes", ()
                -> em.createQuery(JPQL_PENDIENTES_DE_ORDENES, OrdenProducto.class)
                        .setParameter("ordenes", ordenes)
                        .setParameter("anulada", ReglasFactura.ANULADA)
                        .getResultList()
        );
    }

    /** Productos sin cobrar de una orden (ya existía la named query, faltaba el método). */
    public List<OrdenProducto> findPendientesByOrden(Orden orden) {
        if (orden == null || orden.getIdOrden() == null) {
            throw new IllegalArgumentException("La orden es obligatoria");
        }
        return ejecutar("No se pudieron consultar los productos pendientes de la orden", ()
                -> em.createNamedQuery("OrdenProducto.findPendientesByOrden", OrdenProducto.class)
                        .setParameter("orden", orden)
                        .setParameter("anulada", ReglasFactura.ANULADA)
                        .getResultList()
        );
    }
}
