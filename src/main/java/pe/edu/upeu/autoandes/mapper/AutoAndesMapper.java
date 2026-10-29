package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.dto.ClienteResponseDTO;
import pe.edu.upeu.autoandes.dto.DetalleOrdenResponseDTO;
import pe.edu.upeu.autoandes.dto.OrdenResponseDTO;
import pe.edu.upeu.autoandes.dto.ServicioResponseDTO;
import pe.edu.upeu.autoandes.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.entity.Cliente;
import pe.edu.upeu.autoandes.entity.OrdenServicio;
import pe.edu.upeu.autoandes.entity.Rol;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.security.PermisosPorRol;

@Component
public class AutoAndesMapper {
    public ClienteResponseDTO cliente(Cliente c) {
        return new ClienteResponseDTO(c.getId(), c.getDocumento(), c.getNombres(), c.getTelefono());
    }
    public ServicioResponseDTO servicio(Servicio s) {
        return new ServicioResponseDTO(s.getId(), s.getCodigo(), s.getNombre(), s.getPrecio(), s.getDuracionMin(), s.getActivo());
    }
    public VehiculoResponseDTO vehiculo(Vehiculo v) {
        return new VehiculoResponseDTO(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getAnio(),
        v.getCliente().getId(), v.getCliente().getNombres());
    }
    public OrdenResponseDTO orden(OrdenServicio o) {
        var detalles = o.getDetalles().stream().map(d -> new DetalleOrdenResponseDTO(
        d.getId(), d.getServicio().getId(), d.getServicio().getCodigo(), d.getServicio().getNombre(),
        d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal())).toList();
        return new OrdenResponseDTO(o.getId(), o.getNumero(), o.getFecha(), o.getVehiculo().getId(),
        o.getVehiculo().getPlaca(), o.getVehiculo().getCliente().getNombres(), o.getKilometraje(),
        o.getEstado().name(), o.getTotal(), o.getUsuarioRegistro(), o.getMotivoAnulacion(), detalles);
    }
    public UsuarioResponseDTO usuario(Usuario u) {
        return new UsuarioResponseDTO(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getActivo(),
        u.getRoles().stream().map(Rol::getNombre).sorted().toList(), PermisosPorRol.permisos(u.getRoles()));
    }
}
