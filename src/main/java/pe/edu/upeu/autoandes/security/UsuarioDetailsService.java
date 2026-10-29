package pe.edu.upeu.autoandes.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        var u = usuarios.findByUsernameIgnoreCase(username.trim())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
        return User.withUsername(u.getUsername()).password(u.getPassword())
            .authorities(PermisosPorRol.authorities(u.getRoles()))
            .disabled(!Boolean.TRUE.equals(u.getActivo())).build();
    }
}
