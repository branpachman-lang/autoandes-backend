package pe.edu.upeu.autoandes.service.service;

import pe.edu.upeu.autoandes.dto.auth.LoginRequestDTO;
import pe.edu.upeu.autoandes.dto.auth.LoginResponseDTO;
import pe.edu.upeu.autoandes.dto.auth.UsuarioResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
    UsuarioResponseDTO usuarioActual(String username);
}
