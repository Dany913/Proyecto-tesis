package com.tesis.file_manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name="files")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Fileentity {

    @Id
    @GeneratedValue
    private UUID id;
    // datos del paciente
    private String Nombrepaciente;
    private Integer Edad;
    private String Diagnostico;
    private String Raza;
    private String name;
    private String type;
    @Lob
    private byte[] data;

}
