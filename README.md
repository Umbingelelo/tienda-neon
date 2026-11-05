# Tienda Neon: Spring Boot + PostgreSQL en la nube (Neon)

Proyecto base de **DSY1104 Desarrollo Full Stack II**. Es una API REST de productos que guarda los datos en una base PostgreSQL alojada en **Neon**, en internet y no en tu PC. Cada producto tiene un campo `imagenUrl` para guardar la dirección de su foto subida a **ImgBB**.

```
Frontend (React/Angular)  --pide datos-->  Spring Boot (esta API)  --SQL cifrado-->  Neon (PostgreSQL en la nube)
        |
        +-- sube la foto a ImgBB, recibe una URL y guarda SOLO esa URL en el producto
```

**Palabras que vas a ver**

| Palabra | Qué significa |
|---|---|
| PostgreSQL | Un motor de base de datos, como MySQL u Oracle. |
| Neon | Empresa que te presta un PostgreSQL en internet, gratis. |
| `.env` | Archivo de texto con tus claves. Se queda **solo en tu PC**, nunca se sube a GitHub. |
| JDBC | La forma en que Java se conecta a una base de datos. Su dirección empieza con `jdbc:`. |
| host | La dirección del servidor donde está la base de datos. |
| API REST | URLs como `/api/productos` que responden con datos (JSON) cuando les haces GET, POST, PUT o DELETE. |
| puerto | El número de "puerta" donde escucha un programa. Esta API usa el **8080**. |

## Requisitos

| Herramienta | Detalle |
|---|---|
| IntelliJ IDEA (Community sirve) | Es la forma recomendada de ejecutar el proyecto, sobre todo en Windows. |
| JDK 17 o superior | Si IntelliJ no tiene uno, lo descarga él mismo (ver paso 4). |
| Postman **de escritorio** | La versión web no puede llamar a `localhost`. |
| Cuenta en Neon | Gratis. Puedes entrar con GitHub o Google. |
| Cuenta en ImgBB | Gratis. |
| Maven | **No hace falta instalarlo**: el proyecto trae `mvnw`. |

---

## Paso 1: Crear la base de datos en Neon

> Las capturas son reales: se tomaron creando un proyecto en noviembre de 2026. Si Neon cambia algún botón de lugar, busca el mismo texto.

**Una base de datos por equipo.** La crea **un** integrante, que después comparte los datos con el resto (paso 3).

### 1.1 Entrar y crear un proyecto

1. Entra a **https://console.neon.tech** y regístrate con **GitHub** o **Google**.
2. Verás la lista de proyectos. Presiona **New project**, arriba a la derecha.
   *Si es tu primera vez en Neon, puede aparecer directamente el formulario de creación: sigue en el paso 1.2.*

   ![Lista de proyectos con el botón New project](docs/img/01-proyectos.png)

### 1.2 Configurar el proyecto

1. **Project name:** `tienda-<nombre-de-tu-equipo>`, por ejemplo `tienda-los-tres`.
2. **Region:** abre la lista y elige **AWS South America East 1 (São Paulo)**, la más cercana a Chile.
3. Deja **Postgres database** activado (el interruptor verde). **No** actives *Better Auth* ni nada más.
4. Presiona **Create project**.

   ![Formulario Create project con nombre y región São Paulo](docs/img/02-crear-proyecto.png)

5. Neon crea el proyecto en menos de un segundo. Ignora el texto en inglés sobre "coding agent" y presiona **Go to project**.

   ![Proyecto creado, botón Go to project](docs/img/03-proyecto-creado.png)

### 1.3 El panel del proyecto

La pantalla se titula **Branch overview**. *Branch* significa "rama", una copia de tu base; la principal se llama `production`. **Esta es la pantalla correcta.** Neon ya creó una base llamada **`neondb`** y un usuario llamado **`neondb_owner`**.

Botones que vas a usar (en el menú de la izquierda):
- **Connect** (verde): entrega los datos de conexión.
- **Tables**: muestra tus tablas como una planilla.
- **SQL Editor**: para escribir consultas SQL.

![Panel del proyecto con los botones Connect, Tables y SQL Editor](docs/img/04-panel-proyecto.png)

### 1.4 Obtener los datos de conexión

1. Presiona **Connect**.
2. **Apaga** el interruptor **Connection pooling**. Debe quedar **gris**, como en la imagen.

   ![Ventana Connect con Connection pooling apagado](docs/img/05-connect-pooling-off.png)

3. Haz clic donde dice **Connection string ▾** y elige **Parameters only**. Ahora cada dato aparece en su propia línea:

   ![Ventana Connect en modo Parameters only](docs/img/06-connect-parametros.png)

4. La clave aparece como `****************`. Presiona **Copy snippet**: eso copia todo **con la clave real**. Pégalo en un bloc de notas para tenerlo a mano.

> ⚠️ **No presiones "Reset password".** Cambia la clave, y el `.env` de todo tu equipo deja de funcionar.

## Paso 2: Pasar los datos de Neon al formato de Java

Lo que copiaste se ve así, pero con **tus** valores:

```
PGHOST='ep-dawn-moon-b62566zk.c-2.sa-east-1.aws.neon.tech'
PGDATABASE='neondb'
PGUSER='neondb_owner'
PGPASSWORD='npg_AbC123xyz'
PGSSLMODE='require'
PGCHANNELBINDING='require'      <- esta línea NO se usa
```

Spring Boot necesita **3 datos**. Se arman así, **sin las comillas `'`**:

| Dato para Spring | Cómo se arma | Ejemplo |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://` + **PGHOST** + `/` + **PGDATABASE** + `?sslmode=require` | `jdbc:postgresql://ep-dawn-moon-b62566zk.c-2.sa-east-1.aws.neon.tech/neondb?sslmode=require` |
| `DB_USERNAME` | **PGUSER** | `neondb_owner` |
| `DB_PASSWORD` | **PGPASSWORD**. La clave empieza con `npg_`, y eso es **parte de la clave**. | `npg_AbC123xyz` |

Revisa estas 4 cosas, que son los errores más comunes:
1. `DB_URL` empieza con `jdbc:postgresql://`, **no** solo con `postgresql://`.
2. Entre el host y `neondb` hay una barra `/`.
3. Termina **exactamente** en `?sslmode=require`. Si te quedó `&channel_binding=require` al final, **bórralo**.
4. No quedaron comillas `'` ni espacios.

<details>
<summary>¿Copiaste el "Connection string" en vez de "Parameters only"? Haz esto.</summary>

```
Antes:    postgresql://neondb_owner:npg_AbC123xyz@ep-dawn-moon-b62566zk.c-2.sa-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require
                       └─usuario──┘ └──clave────┘ └──────────────── host ────────────────────────────┘
Después:  jdbc:postgresql://ep-dawn-moon-b62566zk.c-2.sa-east-1.aws.neon.tech/neondb?sslmode=require
```

Para pasar de "Antes" a "Después":
- Agrega `jdbc:` al comienzo.
- Borra desde el usuario hasta el `@`, **incluido el `@`**.
- Borra `&channel_binding=require`.

El usuario y la clave que borraste van en `DB_USERNAME` y `DB_PASSWORD`.
</details>

## Paso 3: Abrir el proyecto y crear el `.env`

### 3.1 Descargar el proyecto

Elige **una** opción:

- **Sin Git (más fácil):** en GitHub, botón verde **Code** → **Download ZIP** → clic derecho → **Extraer todo**.
  ⚠️ Windows crea **dos carpetas con el mismo nombre, una dentro de la otra** (`tienda-neon-main\tienda-neon-main\`). La correcta es la de **adentro**, la que contiene `pom.xml`.
- **Con Git:** `git clone https://github.com/Umbingelelo/tienda-neon.git`

Usa una carpeta **sin espacios ni tildes y fuera de OneDrive**, por ejemplo `C:\dev\`. Mueve ahí la carpeta de **adentro** (la que tiene `pom.xml`); puedes renombrarla `tienda-neon`.

### 3.2 Abrirlo en IntelliJ

1. **File → Open** → elige la carpeta que **contiene `pom.xml`**.
2. Si pregunta *Trust project?*, elige **Trust Project**.
3. Espera a que termine la barra de abajo: IntelliJ está descargando las librerías.
4. Si aparece arriba *Project JDK is not defined* → **Setup SDK** → elige uno 17 o superior, o usa **Download JDK** → versión 17 → **Download**.

### 3.3 Crear el archivo `.env`

1. En IntelliJ, clic derecho sobre la carpeta principal del proyecto (la que tiene `pom.xml`) → **New → File** → escribe `.env` → Enter.
   > **No uses el Bloc de notas**: lo guarda como `.env.txt` y Spring no lo encuentra.
2. Pega esto y reemplaza con **tus** datos del paso 2:

```properties
DB_URL=jdbc:postgresql://ep-dawn-moon-b62566zk.c-2.sa-east-1.aws.neon.tech/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_AbC123xyz
```

Sin espacios alrededor del `=` y sin comillas. El archivo `.env.example` es solo una plantilla: el que vale es `.env`.

**¿Por qué un archivo aparte?** `.env` aparece en `.gitignore`, la lista de archivos que Git **no** sube. Si subes tu clave a GitHub, unos programas automáticos (bots) la encuentran en minutos y pueden borrar tu base.

**¿Cómo llegan esos datos a Spring?** Por `src/main/resources/application.properties`. `${DB_URL}` significa "aquí va el valor de `DB_URL` que está en `.env`":

```properties
spring.config.import=optional:file:.env[.properties]   # lee el archivo .env

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=5            # el plan gratis limita conexiones

spring.jpa.hibernate.ddl-auto=update                    # crea la tabla a partir de la clase Producto
spring.jpa.show-sql=true

server.port=${PORT:8080}                                # ${X:valor} = usa X; si no existe, usa valor
app.cors.allowed-origins=${CORS_ORIGINS:http://localhost:5173,http://localhost:3000,http://localhost:4200}
```

**Compartir con el equipo:** pasa el contenido del `.env` por **WhatsApp o Teams**, nunca por GitHub. Cada compañero descarga el mismo proyecto, crea **su propio** `.env` con esos datos y lo ejecuta.

## Paso 4: Ejecutar

**Forma recomendada (Windows, Mac y Linux):** en IntelliJ abre `src/main/java/cl/duoc/tiendaneon/TiendaNeonApplication.java` y presiona el **▶ verde** al lado de `main` → **Run**.

**Por terminal** (pestaña *Terminal*, abajo en IntelliJ):

| Sistema | Comando |
|---|---|
| Windows PowerShell (la terminal de IntelliJ en Windows) | `.\mvnw.cmd spring-boot:run` |
| Windows CMD | `mvnw.cmd spring-boot:run` |
| Mac / Linux / Git Bash | `./mvnw spring-boot:run` |

Si la terminal dice `JAVA_HOME not found` o `'java' no se reconoce`, usa el ▶ de IntelliJ.

**Funcionó si** en la consola aparecen estas dos líneas:

```
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@...    <- se conectó a Neon
Started TiendaNeonApplication in 3.2 seconds                             <- la API está lista
```

Si aparece un error, busca la **primera** línea que diga `Caused by:` y revisa la tabla de **Problemas frecuentes** al final.

## Paso 5: Comprobar que los datos están en la nube

1. Abre **http://localhost:8080/api/productos** en el navegador. Verás 3 productos de ejemplo en formato JSON. Los inserta la clase `DatosIniciales`, solo si la tabla está vacía.
2. En Neon, entra a **Tables** → `productos`. La tabla la creó Spring Boot solo, a partir de la clase `Producto`. `NULL` significa "vacío": todavía no tienen imagen. Desliza hacia la derecha para ver `stock`.

   ![Vista Tables de Neon con la tabla productos](docs/img/07-tables.png)

3. O entra a **SQL Editor**, escribe la consulta y presiona **Run**:

   ```sql
   SELECT id, nombre, precio, stock, imagen_url FROM productos;
   ```

   ![SQL Editor de Neon con el resultado de la consulta](docs/img/08-sql-editor.png)

> **Nombres en Java y en la base:** Hibernate cambia `imagenUrl` (Java) por `imagen_url` (base de datos): todo en minúsculas y con `_` entre palabras. Por eso en SQL se escribe `imagen_url`.

**La prueba de fuego:** que otro integrante, con el mismo `.env`, ejecute el proyecto en **su** PC y vea **los mismos datos**. Eso es tener la base en la nube.

## Paso 6: Probar la API con Postman

| Método | URL | Body | Respuesta esperada |
|---|---|---|---|
| GET | `http://localhost:8080/api/productos` | — | 200 + lista |
| GET | `http://localhost:8080/api/productos/1` | — | 200, o 404 si no existe |
| POST | `http://localhost:8080/api/productos` | JSON del producto | 201 + producto con su `id` |
| PUT | `http://localhost:8080/api/productos/1` | JSON del producto **completo** | 200 + producto actualizado |
| DELETE | `http://localhost:8080/api/productos/1` | — | 204 |

**Crear un producto (POST) en Postman:**
1. Método **POST**, URL `http://localhost:8080/api/productos`.
2. Pestaña **Body** → **raw** → a la derecha elige **JSON**.
3. Pega esto y presiona **Send**:

```json
{
  "nombre": "Zapatilla",
  "descripcion": "Urbana",
  "precio": 39990,
  "stock": 5,
  "imagenUrl": null
}
```

> ⚠️ **PUT reemplaza el producto COMPLETO.** Si envías solo `{"imagenUrl": "..."}`, responde **400** porque falta `nombre`. Si no incluyes `descripcion`, se borra.
> **Truco:** haz `GET /api/productos/1`, copia la respuesta, cambia solo lo que quieras y envíala con `PUT /api/productos/1`.

Si envías un producto sin nombre o con precio negativo, responde **400 Bad Request** y no se guarda. Eso lo hacen las validaciones (`@NotBlank`, `@Min(0)`).

<details>
<summary>Lo mismo con curl (opcional)</summary>

En **PowerShell** escribe `curl.exe` (no `curl`) y todo en **una sola línea**. En Mac, Linux o Git Bash:

```bash
curl -X POST http://localhost:8080/api/productos -H "Content-Type: application/json" -d '{"nombre":"Zapatilla","descripcion":"Urbana","precio":39990,"stock":5}'
```
</details>

## Paso 7: Ejecutar las pruebas automáticas (opcional)

Para ejecutarlas en IntelliJ: clic derecho en `src/test/java` → **Run 'All Tests'**. Por terminal: `.\mvnw.cmd test` (PowerShell) o `./mvnw test` (Mac/Linux).

Las pruebas usan **la misma base del `.env`** y comprueban que:
1. se puede crear, leer y borrar un producto;
2. un producto inválido responde 400;
3. CORS deja entrar al frontend;
4. se cargaron los datos iniciales.

Si las 4 pasan, **la conexión con Neon funciona**. La prueba **solo borra el producto que ella misma crea**; tus productos quedan intactos.

Ejecútalas **antes** de agregar tu propio atributo. Si después agregas uno con `@NotBlank`, la prueba 1 falla porque no lo envía. Eso es esperado y **no** es un problema de Neon.

---

## Subir las imágenes a ImgBB (las imágenes NO van en la base de datos)

La base guarda **texto**. Cada foto se sube a ImgBB, que devuelve una URL pública, y **esa URL** se guarda en `imagenUrl`.

### 1. Obtener la API key

Crea una cuenta en https://imgbb.com (**Crear cuenta**, arriba a la derecha). Luego entra a https://api.imgbb.com y presiona **Get API key**. La *API key* es la contraseña para usar la API: no la publiques.

![Página de la API de ImgBB con el botón Get API key](docs/img/09-imgbb-api.png)

### 2. Subir una imagen con Postman

1. Método **POST**, URL `https://api.imgbb.com/1/upload?key=TU_API_KEY`.
2. **Body** → **form-data**.
3. En *Key* escribe `image`. Pasa el mouse sobre esa celda, cambia **Text** por **File** y en *Value* presiona **Select Files** para elegir la foto (máximo 32 MB).
4. **Send**.

La respuesta es larga. Busca dentro de `"data"` la línea **`"url"`**:

```json
{
  "data": {
    "url_viewer": "https://ibb.co/abc123",              <- NO: abre una página web
    "url": "https://i.ibb.co/abc123/polera.jpg",         <- ESTA: la foto directa
    "display_url": "https://i.ibb.co/abc123/polera.jpg",
    ...
  },
  "success": true
}
```

La correcta empieza con **`https://i.ibb.co/`**, con una `i.` al comienzo. Si la abres en el navegador y ves **solo la foto**, está bien.

### 3. Guardar la URL en el producto

Con el truco del paso 6: `GET /api/productos/1` → copia el JSON → cambia `"imagenUrl": null` por `"imagenUrl": "https://i.ibb.co/abc123/polera.jpg"` → `PUT /api/productos/1`.

### 4. Desde el frontend (lo veremos en la Clase 2)

```js
const IMGBB_KEY = "tu_api_key"; // solo para la clase: queda visible en el navegador

async function crearProductoConImagen(archivo, producto) {
  // 1) subir la foto a ImgBB
  const form = new FormData();
  form.append("image", archivo); // archivo de un <input type="file">
  const r = await fetch(`https://api.imgbb.com/1/upload?key=${IMGBB_KEY}`, { method: "POST", body: form });
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

En este ejemplo la API key queda visible en el navegador (F12 → Network). Para la clase sirve. En un proyecto real, el frontend envía la foto **al backend** y es el backend el que la sube a ImgBB, así la clave nunca sale del servidor.

---

## Problemas frecuentes

Busca en la consola la **primera** línea que diga `Caused by:` o `ERROR`.

| Mensaje | Causa | Solución |
|---|---|---|
| `'mvnw.cmd' no se reconoce como nombre de un cmdlet` | En PowerShell hay que poner `.\` delante | `.\mvnw.cmd spring-boot:run`, o usa el ▶ de IntelliJ |
| `JAVA_HOME not found` / `'java' no se reconoce` | El JDK está solo dentro de IntelliJ | Ejecuta con el ▶ de IntelliJ |
| `Could not resolve placeholder 'DB_URL'` | No hay `.env`, se llama `.env.txt` o está en otra carpeta | Créalo con IntelliJ (**New → File**) junto a `pom.xml` |
| `FATAL: password authentication failed` | Usuario o clave mal copiados | Vuelve a **Connect → Copy snippet** en Neon (sin *Reset password*) |
| `Driver ... claims to not accept jdbcUrl` | `DB_URL` no empieza con `jdbc:` | Revisa el paso 2 |
| `The server does not support SSL` / `timeout` / `UnknownHost` | Falta `?sslmode=require` o el host está mal | Revisa el paso 2 |
| `Port 8080 was already in use` | Otro programa usa el puerto 8080 (otro backend, o lo ejecutaste dos veces) | Detén el otro con el **■ rojo** de IntelliJ, o agrega `PORT=8081` al `.env` y usa `http://localhost:8081/...` |
| `release version 17 not supported` | JDK antiguo | En IntelliJ: **File → Project Structure → SDK** → 17 o superior |
| La primera vez tarda unos segundos | Neon "duerme" la base cuando nadie la usa | Es normal |
| Postman: `Could not send request` a `localhost` | Usas Postman web, o la API no está corriendo | Usa Postman de escritorio y revisa que la consola diga `Started` |
| `PUT` responde 400 | Enviaste el producto incompleto | Envía todos los campos (truco del paso 6) |
| Navegador: `blocked by CORS policy` | El puerto de tu frontend no está permitido | Por defecto se permiten 5173 (Vite), 3000 (CRA) y 4200 (Angular). Para otro puerto, agrega al `.env` la lista **completa**: `CORS_ORIGINS=http://localhost:5173,http://localhost:3000,http://localhost:4200,http://localhost:XXXX`. Esta línea reemplaza la lista por defecto. |

---

## Estructura del proyecto

```
tienda-neon/
├── .env.example                 # plantilla (sí se sube)
├── .env                         # tus claves (NO se sube; lo creas tú)
├── pom.xml                      # Spring Boot 4.1, Java 17, Web MVC, JPA, Validation, PostgreSQL
└── src/
    ├── main/java/cl/duoc/tiendaneon/
    │   ├── TiendaNeonApplication.java     # ▶ se ejecuta desde aquí
    │   ├── config/
    │   │   ├── CorsConfig.java            # deja entrar al frontend
    │   │   └── DatosIniciales.java        # 3 productos de ejemplo
    │   └── producto/
    │       ├── Producto.java              # clase que se vuelve la tabla "productos"
    │       ├── ProductoRepository.java    # guardar / buscar / borrar sin escribir SQL
    │       └── ProductoController.java    # las URL /api/productos
    ├── main/resources/application.properties
    └── test/java/.../TiendaNeonApplicationTests.java
```

### `Producto.java`: la clase se vuelve tabla

```java
@Entity                       // "esta clase es una tabla"
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // la BD genera el id: 1, 2, 3...
    private Long id;

    @NotBlank @Size(max = 100)   // obligatorio, máx. 100 letras
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @NotNull @Min(0)             // obligatorio, no negativo
    private Integer precio;

    @NotNull @Min(0)
    private Integer stock;

    @Size(max = 500)
    private String imagenUrl;    // URL de ImgBB  ->  columna imagen_url
    // constructores, getters y setters...
}
```

### Agregar un atributo propio de tu tienda

1. Agrega el campo con su validación:
   - Texto: `@NotBlank private String categoria;`
   - Número: `@NotNull @Min(0) private Integer talla;`

   `@NotBlank` es **solo para `String`**. **No** uses `@Column(nullable = false)`: los productos que ya existen no tienen ese dato y la tabla no se podría actualizar.
2. Genera el getter y el setter: **Alt+Insert → Getter and Setter** (en Mac, **⌘N**). Sin ellos, el campo llega siempre vacío.
3. En `ProductoController.actualizar(...)` agrega `producto.setCategoria(datos.getCategoria());` para que el PUT también lo cambie.
4. Reinicia la app. La columna nueva (`categoria`, o `talla_ropa` si la llamaste `tallaRopa`) aparece en Neon con `NULL` en los productos antiguos. Es normal.

### `ProductoRepository.java`

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> { }
```

Con esta sola línea ya existen `findAll`, `findById`, `save`, `delete` y `count`.

### `CorsConfig.java`

*CORS* es una regla del navegador: bloquea que una página llame a una API de **otro puerto o dominio** si la API no la autoriza. Este archivo autoriza los orígenes de `app.cors.allowed-origins`. Cuando el frontend esté publicado (por ejemplo `https://mi-tienda.vercel.app`), se agrega esa URL en `CORS_ORIGINS`.

---

## Subir tu proyecto a GitHub

Primero crea en github.com un repositorio **vacío**: **New repository** → nombre `tienda-<equipo>` → **sin** README → **Create**.

**Si clonaste el proyecto base** (ya tiene Git y apunta al repositorio del profesor):

```bash
git remote set-url origin https://github.com/USUARIO/tienda-<equipo>.git
git add .
git commit -m "Conexión a Neon"
git push -u origin main
```

**Si descargaste el ZIP** (necesitas Git instalado: https://git-scm.com):

```bash
git init
git add .
git commit -m "Proyecto base Spring Boot + Neon"
git branch -M main
git remote add origin https://github.com/USUARIO/tienda-<equipo>.git
git push -u origin main
```

Antes del `commit`, ejecuta `git status` y verifica que **`.env` NO aparezca** en la lista.

> ⚠️ **No** subas el proyecto con *Add file → Upload files* en la web de GitHub: así también se sube tu `.env` con la clave. Si no quieres usar comandos, en IntelliJ usa **Git → GitHub → Share Project on GitHub**, que respeta `.gitignore`.

## Material de clase

- [`clase1/slides.html`](clase1/slides.html): presentación de la Clase 1. Es un solo archivo, con las capturas incluidas, y funciona sin internet. Descárgalo y ábrelo con doble clic. Teclas: → y Espacio para avanzar, ← para retroceder, **R** para revelar respuestas, **M** para cambiar entre modo clase y modo estudio.
- [`clase1/ACTIVIDAD.md`](clase1/ACTIVIDAD.md): actividad de la Clase 1, con las evidencias y la pauta.
