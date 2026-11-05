# Actividad Clase 1: tu tienda en la nube

**DSY1104 Desarrollo Full Stack II · Parcial 3 (EA3) · Actividad 3.3 Integración RESTful en la nube**
Se trabaja en el equipo del caso semestral (máximo 3 integrantes). Tiempo: el bloque 3 y lo que quede del bloque 2. Evaluación formativa (no lleva nota).

## Objetivo

Que la tienda del equipo quede con:
1. su **base de datos en la nube** (Neon);
2. el **backend conectado** a esa base;
3. las **fotos de los productos en ImgBB**, guardando en la base **solo la URL**.

## Antes de empezar

- Visual Studio Code con **Extension Pack for Java** y **Spring Boot Extension Pack**, y un JDK 17 o superior (README, paso 3.2).
- **Postman de escritorio** (la versión web no llega a `localhost`).
- Proyecto base: https://github.com/Umbingelelo/tienda-neon. Todos los pasos, con capturas, están en su **README**.
- Repartan roles: **Neon** (Parte A y todas las capturas de Neon) · **Backend** (Partes B y C) · **Fotos** (Parte D).

---

## Parte A: Base de datos en Neon (la hace UN integrante)

1. Crear una cuenta en https://console.neon.tech (con GitHub o Google).
2. **New project** → nombre `tienda-<nombre-equipo>` → región **AWS South America East 1 (São Paulo)** → **Create project** → **Go to project**.
3. **Connect** → apagar **Connection pooling** → menú **Connection string ▾** → **Parameters only** → **Copy snippet**.
4. Armar `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` como explica el **README, paso 2**.
5. Pasar esos 3 datos al equipo **por WhatsApp o Teams**, nunca por GitHub.

⚠️ No presionen **Reset password**: cambia la clave del equipo.

**Evidencia A:** captura del panel del proyecto en Neon (**Branch overview**) con el nombre del proyecto visible. **Que no se vea la clave.**

## Parte B: Backend conectado

Elijan **una** opción:

- **B1 (recomendada):** usar el proyecto base: **Code → Download ZIP**, o `git clone`.
- **B2:** usar el backend Spring Boot de su equipo:
  1. En `pom.xml`, dentro de `<dependencies>`, agregar:
     ```xml
     <dependency>
         <groupId>org.postgresql</groupId>
         <artifactId>postgresql</artifactId>
         <scope>runtime</scope>
     </dependency>
     ```
  2. Quitar la dependencia de H2 o MySQL si la tenían.
  3. Copiar en su `application.properties` las líneas `spring.config.import`, `spring.datasource.*` y `spring.jpa.*` del proyecto base.
  4. Su base parte vacía: si `GET` responde `[]`, **está bien**.

En las dos opciones:

1. Abrir en VS Code la carpeta que **contiene `pom.xml`** (**File → Open Folder…**).
2. Crear el archivo `.env` **dentro de VS Code**: panel **Explorer** → **New File…** → `.env`, al mismo nivel que `pom.xml`. No usen el Bloc de notas, porque lo guarda como `.env.txt`.
3. Pegar los 3 datos de Neon.
4. Abrir `TiendaNeonApplication.java` y hacer clic en **Run**, el texto que aparece encima de `main`.
5. La consola debe mostrar `HikariPool-1 - Added connection` y `Started ...Application`.
6. Abrir `http://localhost:8080/api/productos` en el navegador.

**Evidencia B:**
- Captura de la consola con `Started ...Application`.
- Captura del navegador o de Postman con el JSON de productos.
- **Prueba de equipo:** un segundo integrante descarga el **mismo** proyecto, crea **su propio** `.env` con los mismos datos, lo ejecuta y ve **los mismos productos**. Captura de su pantalla.

## Parte C: Adaptar el producto a su tienda

El `Producto` del proyecto base es genérico. Adáptenlo al rubro de su tienda (ropa, tecnología, mascotas, etc.):

1. En `Producto.java`, agregar **1 atributo propio** con su validación:
   - Texto: `@NotBlank private String categoria;`
   - Número: `@NotNull @Min(0) private Integer talla;`

   `@NotBlank` es **solo para texto (`String`)**. **No** usen `@Column(nullable = false)`.
2. Generar su getter y setter: clic derecho dentro de la clase → **Source Action…** → **Generate Getters and Setters**.
3. En `ProductoController.java`, dentro de `actualizar(...)`, agregar una línea como `producto.setCategoria(datos.getCategoria());`.
4. Detener la aplicación (cuadrado rojo arriba, o **Ctrl+C** en la terminal) y volver a ejecutarla con **Run**. En Neon → **SQL Editor**, comprobar que apareció la columna:
   ```sql
   SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'productos';
   ```
   El nombre en la base va en minúsculas y con `_`: `tallaRopa` queda como `talla_ropa`. Los 3 productos de ejemplo tendrán `NULL` en la columna nueva, y eso es normal.
5. En Postman (**POST** `http://localhost:8080/api/productos` → **Body → raw → JSON**), crear **5 productos reales** de su tienda, todavía con `"imagenUrl": null`. Por ejemplo:
   ```json
   { "nombre": "Polerón Oversize", "descripcion": "Algodón", "precio": 24990, "stock": 8, "categoria": "Polerones", "imagenUrl": null }
   ```
6. Probar la validación: enviar el mismo JSON con `"precio": -1` debe responder **400 Bad Request**.

**Evidencia C:** captura de `SELECT * FROM productos;` en el SQL Editor de Neon (la saca el integrante A) y captura del **400** en Postman.

## Parte D: Fotos en ImgBB

1. Crear una cuenta en https://imgbb.com → entrar a https://api.imgbb.com → **Get API key**.
2. Subir **una foto por cada uno de los 5 productos** con Postman:
   - **POST** `https://api.imgbb.com/1/upload?key=SU_API_KEY`
   - **Body → form-data** → en *Key* escriban `image`, cambien **Text** por **File** → **Select Files** → **Send**.
3. En la respuesta, dentro de `"data"`, copiar el valor de **`"url"`**: el que empieza con **`https://i.ibb.co/`**. No sirve `url_viewer`.
4. Guardar la URL en el producto. ⚠️ **PUT reemplaza el producto completo**, así que:
   1. `GET http://localhost:8080/api/productos/{id}` → copiar la respuesta;
   2. cambiar solo `"imagenUrl": null` por `"imagenUrl": "https://i.ibb.co/..."`;
   3. `PUT http://localhost:8080/api/productos/{id}` → **Body → raw → JSON** → pegar → **Send**.

   Si envían solo `{"imagenUrl": "..."}`, responde **400**.
5. Verificar en Neon:
   ```sql
   SELECT id, nombre, imagen_url FROM productos;
   ```

**Evidencia D:** captura de esa consulta con las 5 URLs, y una de esas URLs abierta en el navegador mostrando **solo la foto**.

## Desafío (opcional, adelanta la Clase 2)

Desde **su frontend**, listar los productos de `GET http://localhost:8080/api/productos` mostrando nombre, precio y foto:

- React: `<img src={producto.imagenUrl} alt={producto.nombre} />`
- Angular: `<img [src]="producto.imagenUrl" [alt]="producto.nombre">`

Si el navegador muestra **`blocked by CORS policy`**: por defecto se permiten los puertos 5173 (Vite), 3000 y 4200 (Angular). Si su frontend usa otro puerto, agreguen al `.env` la lista **completa** `CORS_ORIGINS=http://localhost:5173,http://localhost:3000,http://localhost:4200,http://localhost:XXXX` y reinicien el backend.

**Evidencia:** captura de la página con las 5 tarjetas de producto.

---

## Entrega (al final de la clase)

**Un documento por equipo, subido en AVA**, con:

1. Nombre del equipo, integrantes y sección.
2. Link al repositorio de GitHub **del equipo** (no el del profesor), sin `.env` ni claves. Cómo crearlo: README, sección "Subir tu proyecto a GitHub".
3. Las evidencias A, B, C y D (y la del desafío, si la hicieron).
4. Una respuesta de 3 a 5 líneas: **¿por qué guardamos en la base de datos solo la URL de la foto y no el archivo?**

## Pauta de revisión (formativa)

| Criterio | Logrado | Parcial | No logrado |
|---|---|---|---|
| A. BD en Neon | Creada y usada por todo el equipo | Creada, pero solo le funciona a un integrante | No creada |
| B. Backend conectado | Conecta usando `.env`, y las claves no están en el repositorio | Conecta, pero con la clave escrita en `application.properties` | No conecta |
| C. Modelo propio | Atributo nuevo con validación, 5 productos y el 400 probado | Hay productos, pero sin atributo propio o sin validación | Sin datos propios |
| D. Fotos | 5 productos con `imagenUrl` de ImgBB que se ven en el navegador | Menos de 5, o URLs que no cargan | Sin fotos |
| Respuesta | Menciona el peso, la velocidad y el almacenamiento aparte (CDN) | Incompleta | Sin respuesta |

## Si algo falla

Revisen la tabla **"Problemas frecuentes"** del README antes de llamar al docente. Busquen en la consola la **primera** línea que diga `Caused by:`.
