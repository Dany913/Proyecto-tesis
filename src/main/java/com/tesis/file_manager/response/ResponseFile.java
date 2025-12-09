package com.tesis.file_manager.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class ResponseFile {
    private String name;
    private String url;
    private String type;
    private long size;
    private String Nombrepaciente;
    private String Raza;
    private Integer Edad;
    private String Diagnostico;

}
