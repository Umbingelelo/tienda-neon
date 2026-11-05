package cl.duoc.tiendaneon;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.tiendaneon.producto.Producto;
import cl.duoc.tiendaneon.producto.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración: usan la MISMA base de datos configurada en .env
 * (o variables de entorno). Si pasan, la conexión a Neon funciona.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TiendaNeonApplicationTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ProductoRepository repository;

    @Test
    void creaLeeYEliminaUnProducto() throws Exception {
        String json = """
                {"nombre":"Producto de prueba","precio":1990,"stock":3,
                 "imagenUrl":"https://i.ibb.co/ejemplo/foto.jpg"}
                """;

        String location = mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(location.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(get("/api/productos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Producto de prueba"))
                .andExpect(jsonPath("$.imagenUrl").value("https://i.ibb.co/ejemplo/foto.jpg"));

        mvc.perform(delete("/api/productos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rechazaProductoInvalido() throws Exception {
        long antes = repository.count();
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"precio\":-1,\"stock\":0}"))
                .andExpect(status().isBadRequest());
        org.junit.jupiter.api.Assertions.assertEquals(antes, repository.count());
    }

    @Test
    void permiteCorsDesdeElFrontend() throws Exception {
        mvc.perform(get("/api/productos").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void cargaDatosIniciales() {
        org.junit.jupiter.api.Assertions.assertTrue(
                repository.findAll().stream().map(Producto::getNombre).anyMatch("Polerón"::equals));
    }
}
