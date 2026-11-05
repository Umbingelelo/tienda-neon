# Actividad Clase 1: tu tienda en la nube

**DSY1104 Desarrollo Full Stack II · EA3 · Act 3.3 Integración RESTful en la nube**
Modalidad: en el equipo del caso semestral (máx. 3 integrantes) · Duración: bloque 3 + lo que quede del bloque 2 · Formativa.

## Objetivo

Dejar la tienda del equipo con **su base de datos en la nube (Neon)**, **el backend conectado** y **las imágenes de los productos en ImgBB**, guardando en la BD solo la URL.

## Antes de empezar

- JDK 17 o superior (`java -version`).
- Git y una cuenta de GitHub.
- Postman, Thunder Client (VS Code) o `curl`.
- Proyecto base: https://github.com/Umbingelelo/tienda-neon. El paso a paso está en su `README.md`.

---

## Parte A: Base de datos en Neon (un integrante por equipo)

1. Crear una cuenta en https://console.neon.tech.
2. Crear el proyecto `tienda-<nombre-equipo>`.
3. En **Connect**, desactivar *Connection pooling* y copiar el connection string.
4. Convertirlo a las 3 variables `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` (README, paso 2).
5. Compartir esos datos con el equipo **por un canal privado**, nunca por GitHub.

**Evidencia A:** captura del panel del proyecto en Neon con el nombre del proyecto visible. **Tapen la clave.**

## Parte B: Backend conectado

Elijan **una** opción:

- **B1 (recomendada si su backend aún no está listo):** clonar el proyecto base.
- **B2:** usar el backend Spring Boot de su equipo. Agreguen la dependencia `org.postgresql:postgresql` y configuren `application.properties` igual que el proyecto base (con `${DB_URL}`, etc.).

En ambos casos:

1. Crear `.env` a partir de `.env.example` con los datos de Neon.
2. Verificar que `.env` aparece en `.gitignore` y que `git status` **no** lo muestra.
3. Ejecutar `./mvnw spring-boot:run` (en Windows, `mvnw.cmd spring-boot:run`) y abrir `http://localhost:8080/api/productos`.

**Evidencia B:**
- Captura de la consola con `Started ...Application`.
- Captura del navegador o de Postman con el JSON de productos.
- **Prueba de equipo:** un segundo integrante ejecuta el proyecto en su PC con el mismo `.env` y ve los mismos datos. Captura de su pantalla.

## Parte C: Adaptar la entidad a su tienda

El proyecto base trae un `Producto` genérico. Adáptenlo al rubro de su caso (ropa, tecnología, mascotas, etc.):

1. Agregar **al menos 1 atributo propio** de su tienda (por ejemplo `categoria`, `talla`, `marca` o `color`) con su validación (`@NotBlank`, `@Size`, `@Min`…).
2. Reiniciar la app y comprobar en Neon que la columna nueva se creó:
   ```sql
   SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'productos';
   ```
3. Crear con `POST /api/productos` **5 productos reales** de su tienda (todavía sin imagen).
4. Probar que la validación funciona: enviar un producto con precio negativo debe responder **400**.

**Evidencia C:** captura del `SELECT * FROM productos;` en el SQL Editor de Neon y captura del 400 en Postman.

## Parte D: Imágenes en ImgBB

1. Crear una cuenta en https://imgbb.com y obtener la API key en https://api.imgbb.com.
2. Subir **una imagen por cada uno de los 5 productos**:
   ```bash
   curl -X POST "https://api.imgbb.com/1/upload?key=TU_API_KEY" -F "image=@foto.jpg"
   ```
   En Postman: `POST` a la misma URL → *Body* → *form-data* → clave `image`, tipo **File**.
3. De cada respuesta, copiar `data.url`.
4. Actualizar cada producto con `PUT /api/productos/{id}`, incluyendo `"imagenUrl": "<data.url>"`.
5. Verificar en Neon:
   ```sql
   SELECT id, nombre, imagen_url FROM productos;
   ```

**Evidencia D:** captura de esa consulta con las 5 URLs, y una de esas URLs abierta en el navegador mostrando la imagen.

## Desafío (opcional, suma para la próxima clase)

Desde **su frontend** (React o Angular), listar los productos que entrega `GET http://localhost:8080/api/productos` mostrando nombre, precio e imagen (`<img src={producto.imagenUrl}>`).

- Si el navegador muestra un error de **CORS**, revisen `app.cors.allowed-origins` en `application.properties` (o la variable `CORS_ORIGINS`) y agreguen el puerto de su frontend.

**Evidencia:** captura de la página con las 5 tarjetas de producto.

---

## Entrega (al final de la clase)

Subir en AVA / enviar al docente **un documento por equipo** con:

1. Nombre del equipo, integrantes y sección.
2. Link al repositorio de GitHub. Debe **no** contener `.env` ni claves.
3. Las evidencias A, B, C y D (y la del desafío, si la hicieron).
4. Respondan en 3–5 líneas: **¿por qué guardamos solo la URL de la imagen en la base de datos y no el archivo?**

## Pauta de revisión (formativa)

| Criterio | Logrado | Parcial | No logrado |
|---|---|---|---|
| A. BD creada en Neon | Proyecto creado y en uso por el equipo | Creado, pero solo funciona a un integrante | No creada |
| B. Backend conectado | Conecta con `.env`; las claves no están en el repositorio | Conecta, pero con la clave escrita en `application.properties` | No conecta |
| C. Modelo propio | Atributo nuevo con validación + 5 productos + 400 probado | Productos creados, sin atributo propio o sin validación | Sin datos propios |
| D. Imágenes | 5 productos con `imagenUrl` de ImgBB que se ven en el navegador | Menos de 5 o URLs que no cargan | Sin imágenes |
| Respuesta conceptual | Menciona el peso, el rendimiento y el almacenamiento de objetos/CDN | Respuesta incompleta | Sin respuesta |

## Si algo falla

Revisen la tabla **"Problemas frecuentes"** del `README.md` del proyecto base antes de llamar al docente.
