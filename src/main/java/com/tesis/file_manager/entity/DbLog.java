package com.tesis.file_manager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "db_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime logTime;
    private String userName;
    private String databaseName;
    private String connectionFrom;
    private String errorSeverity;
    private String message;
    private String applicationName;
}


