package com.example.be.repository;

import com.example.be.entity.Certification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, String> {
    Optional<Certification> findByEmail(String email);
    @Transactional
    void deleteByEmail(String email);
}