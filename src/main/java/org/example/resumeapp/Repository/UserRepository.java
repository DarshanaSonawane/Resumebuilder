package org.example.resumeapp.Repository;

import org.example.resumeapp.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,String> {


    Optional<UserEntity> findByEmail(String email);
boolean existsByEmail(String email);

    Optional<UserEntity> findByVerificationToken(String token);
}
