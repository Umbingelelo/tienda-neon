# Tienda Neon: Spring Boot + PostgreSQL en la nube (Neon)

Proyecto base de **DSY1104 Desarrollo Full Stack II**. Es una API REST de productos que guarda los datos en una base PostgreSQL alojada en **Neon**. Cada producto tiene un campo `imagenUrl` pensado para guardar la URL de la imagen que se sube a **ImgBB**.

```
Frontend (React/Angular)  --HTTP/JSON-->  Spring Boot (esta API)  --JDBC/SSL-->  Neon (PostgreSQL)
        |                                                                         
        +-- sube la imagen a ImgBB y guarda solo la URL en el producto
```

## Requisitos

| Herramienta | Versión | Verificar con |
|---|---|---|
| JDK | 17 o superior | `java -version` |
| Maven | **no es necesario**, el proyecto trae `mvnw` | — |
| IDE | IntelliJ, VS Code (Extension Pack for Java) o Spring Tools | — |
| Cuenta en Neon | gratuita | https://neon.tech |

---

## Paso 1: Crear la base de datos en Neon

1. Entra a **https://console.neon.tech** y regístrate (puedes usar tu cuenta de GitHub o Google).
2. Haz clic en **New Project**:
   - **Project name:** `tienda`
   - **Postgres version:** la que viene por defecto
   - **Region:** la más cercana (São Paulo si aparece en la lista)
3. Neon crea automáticamente una base llamada **`neondb`** y un usuario **`neondb_owner`**.
4. En el panel del proyecto, presiona **Connect**. Se abre una ventana con el *connection string*:

   ```
   postgresql://neondb_owner:AbC123xyz@ep-cool-name-123456.sa-east-1.aws.neon.tech/neondb?sslmode=require
                └── usuario ──┘ └ clave ┘ └──────────────── host ────────────────┘ └ base ┘
   ```

   > Desactiva la opción **Connection pooling** para que el host **no** termine en `-pooler`. Con una sola app y un pool pequeño, la conexión directa es la más simple.

## Paso 2: Convertir el connection string al formato JDBC

Java **no** usa el formato `postgresql://usuario:clave@host`. Se separa en 3 datos:

| Variable | Valor (con el ejemplo anterior) |
|---|---|
| `DB_URL` | `jdbc:postgresql://ep-cool-name-123456.sa-east-1.aws.neon.tech/neondb?sslmode=require` |
| `DB_USERNAME` | `neondb_owner` |
| `DB_PASSWORD` | `AbC123xyz` |

Reglas:
- Se agrega `jdbc:` al comienzo.
- El usuario y la clave **se sacan** de la URL.
- Se mantiene `?sslmode=require`, porque Neon **solo** acepta conexiones cifradas.
- Si la URL trae `&channel_binding=require`, bórralo; no hace falta.

## Paso 3: Configurar las credenciales (sin subirlas a GitHub)

En la raíz del proyecto copia el archivo de ejemplo:

```bash
# macOS / Linux / Git Bash
cp .env.example .env
# Windows (PowerShell)
copy .env.example .env
```

Edita `.env` con **tus** datos:

```properties
DB_URL=jdbc:postgresql://ep-cool-name-123456.sa-east-1.aws.neon.tech/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=AbC123xyz
```

`.env` está en el `.gitignore`. **Nunca** subas tu clave al repositorio: cualquiera con ese dato puede borrar tu base de datos.

¿Cómo llegan esos valores a Spring? Por `src/main/resources/application.properties`:

```properties
spring.config.import=optional:file:.env[.properties]   # lee el archivo .env

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=5            # el plan gratis limita conexiones

spring.jpa.hibernate.ddl-auto=update                    # crea la tabla desde la entidad
spring.jpa.show-sql=true

server.port=${PORT:8080}
app.cors.allowed-origins=${CORS_ORIGINS:http://localhost:5173,http://localhost:3000}
```

`${VARIABLE:valor}` significa "usa la variable de entorno y, si no existe, usa el valor por defecto". Más adelante, al subir la API a la nube (Render, Railway, etc.), **no** se usa `.env`: las mismas variables se configuran en el panel del servicio.

## Paso 4: Ejecutar

```bash
# macOS / Linux / Git Bash
./mvnw spring-boot:run
# Windows (PowerShell / CMD)
mvnw.cmd spring-boot:run
```

Desde IntelliJ también puedes ejecutar `TiendaNeonApplication` con el botón ▶. Se debe abrir **la carpeta del proyecto** (donde está `pom.xml`), porque `.env` se busca en esa carpeta.

Si todo está bien, en la consola verás:

```
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@...
Started TiendaNeonApplication in ... seconds
```

## Paso 5: Comprobar que los datos están en la nube

1. Abre http://localhost:8080/api/productos. Verás 3 productos de ejemplo, que la clase `DatosIniciales` inserta solo si la tabla está vacía.
2. En Neon ve a **SQL Editor** y ejecuta:

   ```sql
   SELECT * FROM productos;
   ```

   Aparecen los mismos productos. **La base no está en tu computador**: si un compañero usa tu `.env`, verá los mismos datos.

## Paso 6: Probar la API (CRUD)

| Método | URL | Body | Respuesta |
|---|---|---|---|
| GET | `/api/productos` | — | 200 + lista |
| GET | `/api/productos/{id}` | — | 200, o 404 si no existe |
| POST | `/api/productos` | JSON del producto | 201 + producto con `id` |
| PUT | `/api/productos/{id}` | JSON del producto | 200 + producto actualizado |
| DELETE | `/api/productos/{id}` | — | 204 |

Con Postman, Thunder Client o `curl`:

```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Zapatilla","descripcion":"Urbana","precio":39990,"stock":5,"imagenUrl":"https://i.ibb.co/xxxx/zapatilla.jpg"}'
```

Las validaciones (`@NotBlank`, `@Min(0)`, etc.) hacen que un producto sin nombre o con precio negativo responda **400 Bad Request** y no se guarde.

## Paso 7: Ejecutar las pruebas

```bash
./mvnw test        # Windows: mvnw.cmd test
```

`TiendaNeonApplicationTests` levanta la aplicación completa contra **la misma base configurada en `.env`** y comprueba que:
1. se puede crear, leer y eliminar un producto (incluida su `imagenUrl`);
2. un producto inválido se rechaza con 400;
3. CORS permite peticiones desde `http://localhost:5173`;
4. se cargaron los datos iniciales.

Si las 4 pruebas pasan, **la conexión con Neon funciona**. La prueba borra el producto que crea.

---

## Estructura del proyecto

```
tienda-neon/
├── .env.example                 # plantilla de credenciales (sí se sube)
├── .env                         # tus credenciales (NO se sube)
├── pom.xml                      # Spring Boot 4.1, Java 17, Web MVC, JPA, Validation, PostgreSQL
└── src/
    ├── main/java/cl/duoc/tiendaneon/
    │   ├── TiendaNeonApplication.java
    │   ├── config/
    │   │   ├── CorsConfig.java        # deja entrar al frontend
    │   │   └── DatosIniciales.java    # 3 productos de ejemplo
    │   └── producto/
    │       ├── Producto.java          # @Entity -> tabla "productos"
    │       ├── ProductoRepository.java# JpaRepository: CRUD sin escribir SQL
    │       └── ProductoController.java# endpoints /api/productos
    ├── main/resources/application.properties
    └── test/java/.../TiendaNeonApplicationTests.java
```

### `Producto.java` (entidad → tabla)

```java
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @NotNull @Min(0)
    private Integer precio;

    @NotNull @Min(0)
    private Integer stock;

    /** URL pública de la imagen (la que devuelve ImgBB). */
    @Size(max = 500)
    private String imagenUrl;
    // constructores, getters y setters...
}
```

Hibernate (`ddl-auto=update`) crea en Neon la tabla `productos` con las columnas `id`, `nombre`, `descripcion`, `precio`, `stock` e `imagen_url`.

### `ProductoRepository.java`

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
```

Con esto ya hay `findAll`, `findById`, `save`, `delete` y `count`.

### `ProductoController.java` (resumen)

```java
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    @GetMapping            public List<Producto> listar()
    @GetMapping("/{id}")   public Producto obtener(@PathVariable Long id)
    @PostMapping           public Producto crear(@Valid @RequestBody Producto producto)    // 201
    @PutMapping("/{id}")   public Producto actualizar(@PathVariable Long id, @Valid @RequestBody Producto datos)
    @DeleteMapping("/{id}")public void eliminar(@PathVariable Long id)                    // 204
}
```

### `CorsConfig.java`

El frontend corre en otro puerto (Vite usa 5173 y CRA usa 3000), así que el navegador bloquea la llamada si la API no lo autoriza:

```java
registry.addMapping("/api/**")
        .allowedOrigins(allowedOrigins)          // viene de app.cors.allowed-origins
        .allowedMethods("GET", "POST", "PUT", "DELETE");
```

Cuando el frontend esté publicado (por ejemplo `https://mi-tienda.vercel.app`), agrega esa URL en la variable `CORS_ORIGINS`.

---

## Conectar con ImgBB (las imágenes NO van en la base de datos)

La base solo guarda **texto**. Las imágenes se suben a ImgBB, que devuelve una URL pública, y esa URL se guarda en `imagenUrl`.

1. Crea una cuenta en https://imgbb.com y obtén tu API key en https://api.imgbb.com.
2. Sube la imagen y luego crea el producto con la URL (ejemplo para el frontend):

```js
async function crearProductoConImagen(archivo, producto) {
  // 1) subir a ImgBB
  const form = new FormData();
  form.append("image", archivo); // archivo de un <input type="file">
  const r = await fetch(`https://api.imgbb.com/1/upload?key=${import.meta.env.VITE_IMGBB_KEY}`, {
    method: "POST",
    body: form,
  });
  const { data } = await r.json();

  // 2) guardar el producto con la URL en nuestra API
  const res = await fetch("http://localhost:8080/api/productos", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...producto, imagenUrl: data.url }),
  });
  return res.json();
}
```

En este ejemplo la API key de ImgBB queda visible en el navegador. Para la clase sirve. En un proyecto real, el frontend envía la imagen **al backend** y es el backend el que la sube a ImgBB, para que la clave nunca salga del servidor.

---

## Problemas frecuentes

| Error en consola | Causa | Solución |
|---|---|---|
| `Could not resolve placeholder 'DB_URL'` | No existe `.env` o no está en la carpeta desde donde se ejecuta | Crea `.env` junto a `pom.xml` y ejecuta desde esa carpeta |
| `FATAL: password authentication failed` | Usuario o clave incorrectos | Copia de nuevo la clave desde **Connect** en Neon |
| `The server does not support SSL` / `connection refused` | Falta `?sslmode=require` o el host está mal | Revisa `DB_URL` (paso 2) |
| `Driver ... claims to not accept jdbcUrl` | Pegaste `postgresql://usuario:clave@...` | Debe empezar con `jdbc:postgresql://` y sin usuario ni clave |
| El primer arranque tarda unos segundos | Neon "duerme" la base cuando no se usa | Es normal; las siguientes consultas son rápidas |
| En el navegador: `blocked by CORS policy` | La URL del frontend no está permitida | Agrégala en `CORS_ORIGINS` |
| `release version 17 not supported` | JDK antiguo | Instala JDK 17 o superior |

## Subir a GitHub

```bash
git init
git add .
git commit -m "Proyecto base Spring Boot + Neon"
git branch -M main
git remote add origin https://github.com/USUARIO/tienda-neon.git
git push -u origin main
```

Antes del `commit`, revisa con `git status` que **`.env` no aparezca** en la lista.

## Material de clase

- `clase1/slides.html`: presentación de la Clase 1. Ábrela en el navegador y usa ← → para avanzar, F para pantalla completa y N para ver las notas del docente.
- `clase1/ACTIVIDAD.md`: actividad de la Clase 1 con las evidencias y la pauta.
