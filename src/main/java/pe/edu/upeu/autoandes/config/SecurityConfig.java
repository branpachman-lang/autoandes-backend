package pe.edu.upeu.autoandes.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import pe.edu.upeu.autoandes.security.JwtAuthenticationFilter;
import pe.edu.upeu.autoandes.security.JwtService;
import pe.edu.upeu.autoandes.security.RestAccessDeniedHandler;
import pe.edu.upeu.autoandes.security.RestAuthenticationEntryPoint;
import static pe.edu.upeu.autoandes.enums.Permiso.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain seguridad(HttpSecurity http, JwtService jwt, UserDetailsService usuarios,
    @Qualifier("corsConfigurationSource") CorsConfigurationSource cors, RestAuthenticationEntryPoint entry, RestAccessDeniedHandler denied) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable).logout(AbstractHttpConfigurer::disable)
            .cors(c -> c.configurationSource(cors))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
            .authorizeHttpRequests(a -> a
            .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/v1/clientes").hasAuthority(VEHICULO_LEER.name())
            .requestMatchers(HttpMethod.GET, "/api/v1/servicios", "/api/v1/servicios/**").hasAuthority(SERVICIO_LEER.name())
            .requestMatchers("/api/v1/servicios", "/api/v1/servicios/**").hasAuthority(SERVICIO_ESCRIBIR.name())
            .requestMatchers(HttpMethod.GET, "/api/v1/vehiculos", "/api/v1/vehiculos/**").hasAuthority(VEHICULO_LEER.name())
            .requestMatchers("/api/v1/vehiculos", "/api/v1/vehiculos/**").hasAuthority(VEHICULO_ESCRIBIR.name())
            .requestMatchers(HttpMethod.POST, "/api/v1/ordenes").hasAuthority(ORDEN_REGISTRAR.name())
            .requestMatchers(HttpMethod.PATCH, "/api/v1/ordenes/*/anular").hasAuthority(ORDEN_ANULAR.name())
            .requestMatchers(HttpMethod.GET, "/api/v1/ordenes", "/api/v1/ordenes/**").hasAuthority(ORDEN_LEER.name())
            .requestMatchers("/api/v1/reportes/**").hasAuthority(REPORTE_VER.name())
            .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(jwt, usuarios), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
        @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    }
