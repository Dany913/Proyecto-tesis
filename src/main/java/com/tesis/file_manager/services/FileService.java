package com.tesis.file_manager.services;
import com.tesis.file_manager.response.ResponseFile;
import org.springframework.web.multipart.MultipartFile;

import com.tesis.file_manager.entity.Fileentity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileService {
    // Permite almacenar o cargar archivos a la base de datos
    Fileentity store (MultipartFile file, String Nombrepaciente, String Raza, Integer Edad, String Diagnostico) throws IOException;

    //Permite descargar archivos en la base de datos
    Optional <Fileentity> getFile (UUID id) throws FileNotFoundException;

    //Permite consultar la lista de archivos cargados en la base de datos
    List <ResponseFile> getAllFiles();

    //Permite eliminar archivos de la base de datos
    void deleteFile (UUID id) throws FileNotFoundException;



}
