package com.tesis.file_manager.repository;

import com.tesis.file_manager.entity.Fileentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository <Fileentity, UUID> {
    Optional<Fileentity> findByName(String name);
}
