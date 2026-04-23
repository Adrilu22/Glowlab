package com.example.api_skincare.repository;

import com.example.api_skincare.model.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RutinaRepository extends JpaRepository<Rutina, Long> {
    List<Rutina> findByUsuarioId(Long usuarioId);
}
