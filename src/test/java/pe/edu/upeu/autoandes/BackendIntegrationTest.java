package pe.edu.upeu.autoandes;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.autoandes.dto.DetalleOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenResponseDTO;
import pe.edu.upeu.autoandes.entity.Cliente;
import pe.edu.upeu.autoandes.enums.EstadoOrden;
import pe.edu.upeu.autoandes.entity.Rol;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.repository.ClienteRepository;
import pe.edu.upeu.autoandes.repository.DetalleOrdenRepository;
import pe.edu.upeu.autoandes.repository.OrdenRepository;
import pe.edu.upeu.autoandes.repository.RolRepository;
import pe.edu.upeu.autoandes.repository.ServicioRepository;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;
import pe.edu.upeu.autoandes.security.JwtService;
import pe.edu.upeu.autoandes.security.PermisosPorRol;
import pe.edu.upeu.autoandes.service.service.OrdenService;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(BackendIntegrationTest.RelojPrueba.class)
class BackendIntegrationTest {
    @TestConfiguration
    static class RelojPrueba {
        @Bean @Primary Clock relojPrueba() {
            return Clock.fixed(Instant.parse("2026-10-29T15:00:00Z"), ZoneId.of("America/Lima"));
        }
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired ClienteRepository clientes;
    @Autowired VehiculoRepository vehiculos;
    @Autowired ServicioRepository servicios;
    @Autowired OrdenRepository ordenes;
    @Autowired DetalleOrdenRepository detalles;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtService jwt;
    @Autowired OrdenService ordenService;
    Long vehiculoId, clienteId;
    List<Long> servicioIds;
    String admin, asesor, jefe;

    @BeforeEach
    void preparar() throws Exception {
        // Cada prueba usa transacciones reales del servicio, sin transacción envolvente del test.
        for (String tabla : List.of("usuario_roles", "detalle_orden", "orden_servicio", "vehiculo", "servicio", "cliente", "usuarios", "roles")) {
            jdbc.update("DELETE FROM " + tabla);
        }
        jdbc.execute("CREATE SEQUENCE IF NOT EXISTS seq_orden_numero START WITH 1");
        Cliente c = new Cliente(); c.setDocumento("70000001"); c.setNombres("Cliente Prueba");
        clienteId = clientes.saveAndFlush(c).getId();
        Vehiculo v = new Vehiculo(); v.setPlaca("AAA-111"); v.setMarca("Toyota"); v.setModelo("Yaris"); v.setAnio(2024); v.setCliente(c);
        vehiculoId = vehiculos.saveAndFlush(v).getId();
        servicioIds = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Servicio s = new Servicio(); s.setCodigo("SRV-00" + i); s.setNombre("Servicio " + i);
            s.setPrecio(new BigDecimal("120.00")); s.setDuracionMin(45); s.setActivo(i != 3);
            servicioIds.add(servicios.saveAndFlush(s).getId());
        }
        for (String nombre : List.of("ADMIN", "ASESOR", "JEFE_TALLER")) {
            Rol rol = new Rol(); rol.setNombre(nombre); roles.saveAndFlush(rol);
            Usuario u = new Usuario(); u.setUsername(nombre.toLowerCase(Locale.ROOT));
            u.setNombreCompleto(nombre); u.setPassword(passwords.encode("Auto2026*")); u.setActivo(true);
            u.setRoles(Set.of(rol)); usuarios.saveAndFlush(u);
        }
        Usuario inactivo = new Usuario(); inactivo.setUsername("inactivo"); inactivo.setNombreCompleto("Inactivo");
        inactivo.setPassword(passwords.encode("Auto2026*")); inactivo.setActivo(false); usuarios.saveAndFlush(inactivo);
        admin = token("admin"); asesor = token("asesor"); jefe = token("jefe_taller");
    }
    String token(String user) throws Exception {
        var body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("username", user, "password", "Auto2026*"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.expiraEnSegundos").value(3600))
            .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asString();
    }
    String orden(int kilometraje, Long... ids) {
        return json.writeValueAsString(Map.of("vehiculoId", vehiculoId, "kilometraje", kilometraje,
        "detalles", Arrays.stream(ids).map(id -> Map.of("servicioId", id, "cantidad", 2)).toList(),
        "total", 0, "usuarioRegistro", "otro"));
    }
    OrdenResponseDTO registrar(int km) {
        return ordenService.registrar(new OrdenRequestDTO(vehiculoId, km,
        List.of(new DetalleOrdenRequestDTO(servicioIds.getFirst(), 2))), "asesor");
    }

    @Test void seguridadDistingue401y403() throws Exception {
        mvc.perform(get("/api/v1/servicios")).andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.mensaje").exists()).andExpect(jsonPath("$.ruta").value("/api/v1/servicios"))
            .andExpect(jsonPath("$.campos").doesNotExist());
        mvc.perform(get("/api/v1/servicios").header("Authorization", "Bearer " + admin + "alterado")).andExpect(status().isUnauthorized());
        // Un token firmado por otra clave tampoco permite entrar.
        var otraClave = new JwtService("b3RyYS1jbGF2ZS1kZS1wcnVlYmEtbXV5LWxhcmdhLWRlLTMyaXRlbXM=", 60);
        String firmaIncorrecta = otraClave.generarToken(User.withUsername("admin").password("x").authorities("SERVICIO_LEER").build());
        mvc.perform(get("/api/v1/servicios").header("Authorization", "Bearer " + firmaIncorrecta)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/reportes/ingresos-por-servicio").header("Authorization", "Bearer " + asesor)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/ordenes").header("Authorization", "Bearer " + jefe).contentType(MediaType.APPLICATION_JSON)
            .content(orden(45000, servicioIds.getFirst()))).andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/ordenes/1/anular").header("Authorization", "Bearer " + asesor)
            .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Registro duplicado\"}")).andExpect(status().isForbidden());
    }
    @Test void loginFallaUniformementeYNoExponePassword() throws Exception {
        for (String user : List.of("nadie", "inactivo", "asesor")) {
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", user, "password", user.equals("asesor") ? "incorrecta" : "Auto2026*"))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
        }
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        assertThat(passwords.matches("Auto2026*", usuarios.findByUsernameIgnoreCase("admin").orElseThrow().getPassword())).isTrue();
    }
    @Test void tokenVencidoYCuentaDesactivadaSeRechazan() throws Exception {
        var emisor = new JwtService("YXV0b2FuZGVzLWRlc2Fycm9sbG8tMjAyNi1jbGF2ZS1qd3QtNjQtYnl0ZXMtZXhjbHVzaXZh", -1);
        String expirado = emisor.generarToken(User.withUsername("asesor").password("x").authorities("SERVICIO_LEER").build());
        mvc.perform(get("/api/v1/servicios").header("Authorization", "Bearer " + expirado)).andExpect(status().isUnauthorized());
        Usuario u = usuarios.findByUsernameIgnoreCase("asesor").orElseThrow(); u.setActivo(false); usuarios.saveAndFlush(u);
        mvc.perform(get("/api/v1/servicios").header("Authorization", "Bearer " + asesor)).andExpect(status().isUnauthorized());
    }
    @Test void unionDePermisosNoComparaRolesEnControladores() {
        Rol a = new Rol(); a.setNombre("ASESOR"); Rol j = new Rol(); j.setNombre("JEFE_TALLER");
        assertThat(PermisosPorRol.permisos(List.of(a, j))).contains("ORDEN_REGISTRAR", "ORDEN_ANULAR", "REPORTE_VER").hasSize(8);
        assertThat(PermisosPorRol.de("DESCONOCIDO")).isEmpty();
    }
    @Test void validacionesYRecursosInexistentesTienenFormatoComun() throws Exception {
        mvc.perform(post("/api/v1/vehiculos").header("Authorization", "Bearer " + asesor).contentType(MediaType.APPLICATION_JSON)
            .content("{\"placa\":\"mala\",\"marca\":\"Toyota\",\"modelo\":\"Yaris\",\"anio\":1980,\"clienteId\":1}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.placa").exists()).andExpect(jsonPath("$.campos.anio").exists());
        mvc.perform(get("/api/v1/servicios/999999").header("Authorization", "Bearer " + admin)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/recurso-inexistente").header("Authorization", "Bearer " + admin))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.ruta").value("/api/v1/recurso-inexistente"));
        mvc.perform(get("/api/v1/ordenes?estado=INVALIDO").header("Authorization", "Bearer " + asesor)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/ordenes").header("Authorization", "Bearer " + asesor).contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje").exists());
    }
    @Test void totalYUsuarioSeCalculanEnServidor() throws Exception {
        mvc.perform(post("/api/v1/ordenes").header("Authorization", "Bearer " + asesor).contentType(MediaType.APPLICATION_JSON)
            .content(orden(45000, servicioIds.getFirst())))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(240))
            .andExpect(jsonPath("$.usuarioRegistro").value("asesor")).andExpect(jsonPath("$.detalles[0].precioUnitario").value(120));
        assertThat(ordenes.count()).isEqualTo(1); assertThat(detalles.count()).isEqualTo(1);
    }
    @Test void tercerServicioInactivoNoDejaDatosParciales() throws Exception {
        mvc.perform(post("/api/v1/ordenes").header("Authorization", "Bearer " + asesor).contentType(MediaType.APPLICATION_JSON)
            .content(orden(45000, servicioIds.toArray(Long[]::new)))).andExpect(status().isConflict());
        assertThat(ordenes.count()).isZero(); assertThat(detalles.count()).isZero();
    }
    @Test void falloAlPersistirRevierteLaTransaccion() {
        // Solo en H2: fuerza un fallo en la segunda línea después de insertar cabecera y primera línea.
        jdbc.execute("ALTER TABLE detalle_orden ADD CONSTRAINT ck_prueba_rollback CHECK (cantidad <> 2)");
        var r = new OrdenRequestDTO(vehiculoId, 45000, List.of(
            new DetalleOrdenRequestDTO(servicioIds.getFirst(), 1),
            new DetalleOrdenRequestDTO(servicioIds.get(1), 2)));
        try {
            assertThatThrownBy(() -> ordenService.registrar(r, "asesor")).isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            jdbc.execute("ALTER TABLE detalle_orden DROP CONSTRAINT ck_prueba_rollback");
        }
        assertThat(ordenes.count()).isZero(); assertThat(detalles.count()).isZero();
    }
    @Test void rechazaRepetidosLimitesYKilometrajeAnterior() throws Exception {
        mvc.perform(post("/api/v1/ordenes").header("Authorization", "Bearer " + asesor).contentType(MediaType.APPLICATION_JSON)
            .content(orden(45000, servicioIds.getFirst(), servicioIds.getFirst()))).andExpect(status().isBadRequest());
        var r = Map.of("vehiculoId",vehiculoId,"kilometraje",45000,"detalles",List.of(Map.of("servicioId",servicioIds.getFirst(),"cantidad",6)));
        mvc.perform(post("/api/v1/ordenes").header("Authorization", "Bearer " + asesor).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(r))).andExpect(status().isBadRequest());
        registrar(45000);
        mvc.perform(post("/api/v1/ordenes").header("Authorization", "Bearer " + asesor).contentType(MediaType.APPLICATION_JSON)
            .content(orden(44999, servicioIds.getFirst()))).andExpect(status().isConflict()).andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("45000")));
    }
    @Test void recursosUsadosNoSeEliminanPeroServicioSeDesactiva() throws Exception {
        registrar(45000);
        mvc.perform(delete("/api/v1/servicios/" + servicioIds.getFirst()).header("Authorization", "Bearer " + admin)).andExpect(status().isConflict());
        mvc.perform(delete("/api/v1/vehiculos/" + vehiculoId).header("Authorization", "Bearer " + asesor)).andExpect(status().isConflict());
        mvc.perform(patch("/api/v1/servicios/" + servicioIds.getFirst() + "/estado").header("Authorization", "Bearer " + jefe)
            .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(false));
    }
    @Test void anularExcluyeIngresoYNoPermiteReanular() throws Exception {
        var o = registrar(45000);
        mvc.perform(get("/api/v1/reportes/ingresos-por-servicio").header("Authorization", "Bearer " + jefe))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].importeTotal").value(240));
        mvc.perform(patch("/api/v1/ordenes/" + o.getId() + "/anular").header("Authorization", "Bearer " + jefe)
            .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Registro duplicado\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ANULADA"));
        mvc.perform(get("/api/v1/reportes/ingresos-por-servicio").header("Authorization", "Bearer " + jefe))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(patch("/api/v1/ordenes/" + o.getId() + "/anular").header("Authorization", "Bearer " + jefe)
            .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Registro duplicado\"}")).andExpect(status().isConflict());
    }
    @Test void anularSoloMismoDiaConMotivoValido() throws Exception {
        var o = registrar(45000);
        mvc.perform(patch("/api/v1/ordenes/" + o.getId() + "/anular").header("Authorization", "Bearer " + jefe)
            .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"corto\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.motivo").exists());
        var anterior = ordenes.findById(o.getId()).orElseThrow(); anterior.setFecha(LocalDateTime.of(2026,10,28,10,0)); ordenes.saveAndFlush(anterior);
        mvc.perform(patch("/api/v1/ordenes/" + o.getId() + "/anular").header("Authorization", "Bearer " + jefe)
            .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Registro duplicado\"}")).andExpect(status().isConflict());
    }
    @Test void filtrosCombinadosRangoYListaVacia() throws Exception {
        var registrada = registrar(45000);
        assertThat(registrada.getFecha()).isEqualTo(LocalDateTime.of(2026, 10, 29, 10, 0));
        assertThat(ordenService.listar(null, null, null, null)).hasSize(1);
        assertThat(ordenService.listar("aaa", null, null, null)).hasSize(1);
        assertThat(ordenService.listar(null, EstadoOrden.REGISTRADA, null, null)).hasSize(1);
        assertThat(ordenService.listar(null, null, LocalDate.of(2026,10,29), LocalDate.of(2026,10,29))).hasSize(1);
        mvc.perform(get("/api/v1/ordenes?placa=aaa&estado=REGISTRADA&desde=2026-10-29&hasta=2026-10-29")
            .header("Authorization", "Bearer " + asesor)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/v1/ordenes?placa=ZZZ").header("Authorization", "Bearer " + asesor))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/ordenes?desde=2026-10-30&hasta=2026-10-29").header("Authorization", "Bearer " + asesor))
            .andExpect(status().isBadRequest());
    }
    @Test void preflightPermiteAngularYAuthorization() throws Exception {
        mvc.perform(options("/api/v1/ordenes").header("Origin", "http://localhost:4200")
            .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "Authorization,Content-Type"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }
}
