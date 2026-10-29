package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(nullable = false, precision = 1)
    private Boolean activo;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles",
    joinColumns = @JoinColumn(name = "usuario_id"),
    inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

}
