package org.example.resumeapp.Repository;

import org.example.resumeapp.Entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, String> {

    List<Resume> findByUserIdOrderByUpdatedAtDesc(String userId);

    // Always fetch by id AND owner, so users can never open each other's resumes
    Optional<Resume> findByIdAndUserId(String id, String userId);
}