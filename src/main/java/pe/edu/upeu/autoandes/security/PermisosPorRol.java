package pe.edu.upeu.autoandes.security;

import pe.edu.upeu.autoandes.enums.Permiso;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.edu.upeu.autoandes.entity.Rol;

public final class PermisosPorRol {
    private static final Map<String, Set<Permiso>> PERMISOS = Map.of(
    "ADMIN", Collections.unmodifiableSet(EnumSet.allOf(Permiso.class)),
    "ASESOR", Collections.unmodifiableSet(EnumSet.of(Permiso.SERVICIO_LEER,
    Permiso.VEHICULO_LEER, Permiso.VEHICULO_ESCRIBIR, Permiso.ORDEN_LEER, Permiso.ORDEN_REGISTRAR)),
    "JEFE_TALLER", Collections.unmodifiableSet(EnumSet.of(Permiso.SERVICIO_LEER,
    Permiso.SERVICIO_ESCRIBIR, Permiso.VEHICULO_LEER, Permiso.ORDEN_LEER, Permiso.ORDEN_ANULAR, Permiso.REPORTE_VER)));
    private PermisosPorRol() {}
    public static Set<Permiso> de(String rol) { return PERMISOS.getOrDefault(rol, Set.of()); }
    public static List<String> permisos(Collection<Rol> roles) {
        return roles.stream().flatMap(r -> de(r.getNombre()).stream()).map(Enum::name).distinct().sorted().toList();
    }
    public static List<GrantedAuthority> authorities(Collection<Rol> roles) {
        return permisos(roles).stream().<GrantedAuthority>map(SimpleGrantedAuthority::new).toList();
    }
}
