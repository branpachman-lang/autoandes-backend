package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.auth.LoginRequestDTO;
import pe.edu.upeu.autoandes.dto.auth.LoginResponseDTO;
import pe.edu.upeu.autoandes.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.autoandes.entity.Rol;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;
import pe.edu.upeu.autoandes.security.JwtService;
import pe.edu.upeu.autoandes.security.PermisosPorRol;
import pe.edu.upeu.autoandes.service.service.AuthService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager autenticacion;
    private final JwtService jwt;
    private final UsuarioRepository usuarios;
    private final AutoAndesMapper mapper;
    @Override
    public LoginResponseDTO login(LoginRequestDTO r) {
        var auth = autenticacion.authenticate(new UsernamePasswordAuthenticationToken(r.getUsername().trim(), r.getPassword()));
        var principal = (UserDetails) auth.getPrincipal();
        var usuario = entidad(principal.getUsername());
        log.info("Login correcto usuario={}", usuario.getUsername());
        return new LoginResponseDTO(jwt.generarToken(principal), "Bearer", jwt.getExpiracionSegundos(), usuario.getUsername(),
        usuario.getNombreCompleto(), usuario.getRoles().stream().map(Rol::getNombre).sorted().toList(), PermisosPorRol.permisos(usuario.getRoles()));
    }
    @Override
    public UsuarioResponseDTO usuarioActual(String username) { return mapper.usuario(entidad(username)); }
    private Usuario entidad(String username) {
        return usuarios.findByUsernameIgnoreCase(username).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

}
