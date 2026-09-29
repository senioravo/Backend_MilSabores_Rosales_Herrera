package com.milsabores.usuario.repository;

import com.milsabores.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEntraOid(String entraOid);
    
    boolean existsByEmail(String email);
}
