# Cafefe (backend)

Backend de la cafetería Cafefe: Jakarta EE 11 en Open Liberty, con JPA y PostgreSQL. Tiene la parte JSF (administración) y el REST que usa la PWA **CafefeWebApp**:

```
http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/
```

## 1. Base de datos

1. PostgreSQL con la base del curso (`pos_ppi115_cafefe`), con el esquema del docente, en UTF-8.
2. Cargar los datos de demostración. Se puede volver a correr sin problema:

```bash
psql -U postgres -d pos_ppi115_cafefe -f sql/poblar-bd.sql
# para empezar de cero: sql/limpiar-bd.sql y luego poblar-bd.sql
```

## 2. Servidor Open Liberty

El `server.xml` del servidor (`ppi115_2026`) necesita:
- la feature `webProfile-11.0`, o la que usa el curso;
- un `dataSource` con `jndiName="jdbc/pgdb"` hacia la base de arriba, con el driver de PostgreSQL;
- PrimeFaces disponible para la parte JSF, porque en el `pom.xml` está como `provided`.

Ejemplo mínimo de `dataSource` (el driver va en `${shared.resource.dir}/pg/`):

```xml
<library id="pg"><fileset dir="${shared.resource.dir}/pg" includes="*.jar"/></library>
<dataSource jndiName="jdbc/pgdb">
    <jdbcDriver libraryRef="pg"/>
    <properties.postgresql serverName="localhost" portNumber="5432"
        databaseName="pos_ppi115_cafefe" user="postgres" password="..."/>
</dataSource>
```

**Importante:** el `pom.xml` apunta a una instalación local de Liberty (`installDirectory`). Cámbiala por la tuya antes de usar `mvn liberty:run`. Otra forma es copiar `target/Cafefe-1.0-SNAPSHOT.war` a la carpeta `dropins/` de tu servidor.

```bash
mvn package          # compila y corre las pruebas
mvn liberty:run      # o copiar el .war a dropins/
```

## 3. Probar el REST

```bash
curl http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/tipo_producto?soloActivos=true
curl http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/orden/pendiente
```

| Recurso | Para |
|---|---|
| `POST login` | HU1: `{nombre, uid}` → empleado y roles activos (401 si no coincide). |
| `GET tipo_producto`, `GET tipo_producto/{id}/producto`, `GET producto` | HU2: catálogo. |
| `POST orden` | HU2: orden de la tienda en línea. |
| `GET orden/pendiente`, `GET orden/pendiente/{id}` | HU3 y HU4: órdenes sin cobrar, cada una con `"despachada"`. |
| `PUT` / `DELETE orden/pendiente/{id}/despacho` | HU3: Despachar y "Volver a la lista". Se guarda en memoria (no cambia la BD) y vence al día. |
| `GET orden/pendiente/pagada` | HU3: órdenes cobradas por adelantado que la cocina aún no despacha, para que no desaparezcan de Cocina. |
| `GET caja` | HU4: cajas. |
| `POST orden/{id}/cobro` | HU4: pago fraccionado, en una sola transacción y con un candado contra el doble cobro. |
| `GET factura/{id}/ticket` | HU4: ticket en PDF (OpenPDF). |

Errores, como enseña el curso: 404 con `Entity-Not-Found`, 422 con `Wrong-Param`, 500 con `App-Error`, más `{ "error": "..." }` en el cuerpo.

## 4. Usuarios de prueba (HU1)

Se entra con **nombre + UID**. La lista completa está al inicio de `sql/poblar-bd.sql`.

| Nombre | UID | Roles | Entra a |
|---|---|---|---|
| Juan | `bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb` | Cajero | Caja |
| Luis | `b3333333-3333-3333-3333-333333333333` | Barista | Cocina |
| Ana | `b2222222-2222-2222-2222-222222222222` | Camarero | Cocina |
| Carla | `b5555555-5555-5555-5555-555555555555` | Cajero + Camarero | elige el rol |

**Estos dan 401 a propósito** (prueban las validaciones del login):
- Pedro (`b6666666-…`): el empleado está inactivo.
- Sofía (`b7777777-…`): su única asignación de rol está inactiva.
- Mateo (`b8888888-…`): su único rol (Supervisor) está inactivo.

Si alguno de ellos "no entra", el login funciona bien.

## 5. Pruebas

```bash
mvn test
```

Las pruebas usan JUnit 5 y Mockito; no necesitan la base de datos ni Liberty.
