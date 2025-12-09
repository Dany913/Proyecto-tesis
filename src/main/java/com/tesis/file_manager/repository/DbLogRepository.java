package com.tesis.file_manager.repository;

import com.tesis.file_manager.entity.DbLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbLogRepository extends JpaRepository<DbLog, Long> {
}

