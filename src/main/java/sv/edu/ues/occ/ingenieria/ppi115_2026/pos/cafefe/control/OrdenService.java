package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.ApplicationException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

@Stateless
public class OrdenService {

    public static final int MAX_CLIENTE = 100;
    public static final int MAX_ITEMS = 50;
    public static final int MAX_CANTIDAD_ITEM = 20;

    @Inject
    OrdenDAO ordenDAO;

    @Inject
    OrdenProductoDAO ordenProductoDAO;

    @Inject
    ProductoDAO productoDAO;

    @Inject
    EmpleadoRolDAO empleadoRolDAO;

    @ApplicationException(rollback = true)
    public static class SolicitudOrdenInvalidaException extends ReglaNegocioException {
        private static final long serialVersionUID = 1L;
        private final String wrongParam;

        public SolicitudOrdenInvalidaException(String wrongParam, String mensaje) {
            super(mensaje);
            this.wrongParam = wrongParam;
        }

        public String getWrongParam() {
            return wrongParam;
        }
    }

    /**
     * Crea una orden pública (tienda en línea) con sus líneas expandidas por unidad
     * dentro de una sola transacción.
     */
    public Map<String, Object> crearOrden(Map<String, Object> solicitud) {
        if (solicitud == null) {
            throw new SolicitudOrdenInvalidaException("registro", "El cuerpo de la orden es obligatorio");
        }

        String cliente = validarCliente(solicitud.get("cliente"));
        List<ItemSolicitado> items = validarItems(solicitud.get("items"));

        EmpleadoRol empleadoRol = empleadoRolDAO.findActivoTiendaEnLinea();
        if (empleadoRol == null || !ReglasOrden.puedeTomarOrdenes(empleadoRol)) {
            throw new AccesoDatosException("Configuración incompleta: no existe un empleado activo con el rol Tienda en línea", null);
        }

        // Validar existencia y estado activo de todos los productos antes de persistir
        Map<UUID, Producto> productosCache = new HashMap<>();
        for (ItemSolicitado item : items) {
            Producto producto = productosCache.get(item.idProducto);
            if (producto == null) {
                producto = productoDAO.findById(item.idProducto);
                if (producto == null || !Boolean.TRUE.equals(producto.getActivo())) {
                    throw new SolicitudOrdenInvalidaException(
                            "items[" + item.indice + "].idProducto",
                            "El producto seleccionado no existe o está inactivo"
                    );
                }
                if (producto.getPrecioSugerido() == null) {
                    throw new SolicitudOrdenInvalidaException(
                            "items[" + item.indice + "].idProducto",
                            "El producto no tiene precio configurado"
                    );
                }
                productosCache.put(item.idProducto, producto);
            }
        }

        Date ahora = new Date();
        Orden orden = new Orden();
        orden.setIdOrden(UUID.randomUUID());
        orden.setFechaCreacion(ahora);
        orden.setIdEmpleadoRol(empleadoRol);
        ordenDAO.create(orden);

        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        List<Map<String, Object>> lineasDto = new ArrayList<>();

        for (ItemSolicitado item : items) {
            Producto producto = productosCache.get(item.idProducto);
            BigDecimal precioUnitario = producto.getPrecioSugerido().setScale(2, RoundingMode.HALF_UP);
            String obsLinea = formatearObservaciones(cliente, item.observaciones);

            for (int u = 0; u < item.cantidad; u++) {
                OrdenProducto op = new OrdenProducto();
                op.setIdOrdenProducto(UUID.randomUUID());
                op.setIdOrden(orden);
                op.setIdProducto(producto);
                op.setPrecio(precioUnitario);
                op.setObservaciones(obsLinea);
                ordenProductoDAO.create(op);

                total = total.add(precioUnitario);

                Map<String, Object> linea = new LinkedHashMap<>();
                linea.put("idOrdenProducto", op.getIdOrdenProducto().toString());
                linea.put("idProducto", producto.getIdProducto().toString());
                linea.put("producto", producto.getNombre());
                linea.put("precio", precioUnitario);
                linea.put("observaciones", obsLinea);
                lineasDto.add(linea);
            }
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("idOrden", orden.getIdOrden().toString());
        respuesta.put("fechaCreacion", Instant.ofEpochMilli(ahora.getTime()).toString());
        respuesta.put("cliente", cliente);
        respuesta.put("total", total);
        respuesta.put("lineas", lineasDto);
        return respuesta;
    }

    private String validarCliente(Object rawCliente) {
        if (!(rawCliente instanceof String)) {
            throw new SolicitudOrdenInvalidaException("cliente", "El nombre del cliente es obligatorio");
        }
        String cliente = ((String) rawCliente).trim();
        if (cliente.isEmpty()) {
            throw new SolicitudOrdenInvalidaException("cliente", "El nombre del cliente no puede estar vacío");
        }
        if (cliente.length() > MAX_CLIENTE) {
            throw new SolicitudOrdenInvalidaException("cliente", "El nombre del cliente excede los 100 caracteres");
        }
        return cliente;
    }

    private List<ItemSolicitado> validarItems(Object rawItems) {
        if (!(rawItems instanceof List<?> listaRaw) || listaRaw.isEmpty()) {
            throw new SolicitudOrdenInvalidaException("items", "La orden debe incluir al menos un producto");
        }
        if (listaRaw.size() > MAX_ITEMS) {
            throw new SolicitudOrdenInvalidaException("items", "La orden no puede superar los 50 ítems");
        }

        List<ItemSolicitado> items = new ArrayList<>(listaRaw.size());
        for (int i = 0; i < listaRaw.size(); i++) {
            Object elem = listaRaw.get(i);
            if (!(elem instanceof Map<?, ?> map)) {
                throw new SolicitudOrdenInvalidaException("items[" + i + "]", "Formato de ítem inválido");
            }

            Object rawId = map.get("idProducto");
            if (!(rawId instanceof String idStr) || idStr.isBlank()) {
                throw new SolicitudOrdenInvalidaException("items[" + i + "].idProducto", "El producto es obligatorio");
            }
            UUID idProducto;
            try {
                idProducto = UUID.fromString(idStr.trim());
            } catch (IllegalArgumentException ex) {
                throw new SolicitudOrdenInvalidaException("items[" + i + "].idProducto", "Identificador de producto inválido");
            }

            int cantidad = 1;
            Object rawCant = map.get("cantidad");
            if (rawCant != null) {
                if (rawCant instanceof Number num) {
                    if (num.doubleValue() % 1 != 0) {
                        throw new SolicitudOrdenInvalidaException("items[" + i + "].cantidad", "La cantidad debe ser un número entero");
                    }
                    cantidad = num.intValue();
                } else {
                    throw new SolicitudOrdenInvalidaException("items[" + i + "].cantidad", "La cantidad debe ser numérica");
                }
                if (cantidad < 1 || cantidad > MAX_CANTIDAD_ITEM) {
                    throw new SolicitudOrdenInvalidaException("items[" + i + "].cantidad", "La cantidad debe estar entre 1 y 20");
                }
            }

            String obs = null;
            Object rawObs = map.get("observaciones");
            if (rawObs != null) {
                if (!(rawObs instanceof String obsStr)) {
                    throw new SolicitudOrdenInvalidaException("items[" + i + "].observaciones", "Las observaciones deben ser texto");
                }
                obs = obsStr.trim();
            }

            items.add(new ItemSolicitado(i, idProducto, cantidad, obs));
        }
        return items;
    }

    static String formatearObservaciones(String cliente, String obsItem) {
        if (obsItem == null || obsItem.isEmpty()) {
            return "Cliente: " + cliente;
        }
        return "Cliente: " + cliente + " | " + obsItem;
    }

    private record ItemSolicitado(int indice, UUID idProducto, int cantidad, String observaciones) {
    }
}