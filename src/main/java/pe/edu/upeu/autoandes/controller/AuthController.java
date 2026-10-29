package pe.edu.upeu.autoandes.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.autoandes.dto.auth.LoginRequestDTO;
import pe.edu.upeu.autoandes.dto.auth.LoginResponseDTO;
import pe.edu.upeu.autoandes.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.autoandes.service.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;
    @PostMapping("/login")
    @SecurityRequirements
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) { return auth.login(request); }
    @GetMapping("/me")
    public UsuarioResponseDTO me(Authentication usuario) { return auth.usuarioActual(usuario.getName()); }

}
