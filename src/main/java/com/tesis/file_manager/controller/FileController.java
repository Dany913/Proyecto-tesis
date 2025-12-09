package com.tesis.file_manager.controller;

import com.tesis.file_manager.entity.Fileentity;
import com.tesis.file_manager.response.ResponseFile;
import com.tesis.file_manager.response.ResponseMessage;
import com.tesis.file_manager.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fileManager")
public class FileController {

    @Autowired
    private FileService fileService;

    // ---- SUBIR ARCHIVO ----
    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "Nombrepaciente", required = false, defaultValue = "Desconocido") String Nombrepaciente,
            @RequestParam(value = "Raza", required = false, defaultValue = "Desconocido") String Raza,
            @RequestParam(value = "Edad", required = false, defaultValue = "0") Integer Edad,
            @RequestParam(value = "Diagnostico", required = false, defaultValue = "Sin diagnostico") String Diagnostico) {
        try {
            fileService.store(file, Nombrepaciente, Raza, Edad, Diagnostico);
            return ResponseEntity.ok(new ResponseMessage("Archivo subido correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseMessage("Error al subir archivo: " + e.getMessage()));
        }
    }

    // ---- DESCARGAR ARCHIVO ----
    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable UUID id) throws FileNotFoundException {
        Fileentity fileentity = fileService.getFile(id).get();
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileentity.getName() + "\"")
                .body(fileentity.getData());
    }

    // ---- LISTAR ARCHIVOS ----
    @GetMapping("/files")
    public ResponseEntity<List<ResponseFile>> getListFiles() {
        List<ResponseFile> files = fileService.getAllFiles();
        return ResponseEntity.status(HttpStatus.OK).body(files);
    }

    // ---- ELIMINAR ARCHIVO ----
    @DeleteMapping("/files/{id}")
    public ResponseEntity<ResponseMessage> deleteFile(@PathVariable UUID id) {
        try {
            fileService.deleteFile(id); // usar la instancia, no la clase
            return ResponseEntity.ok(new ResponseMessage("Archivo eliminado correctamente"));
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseMessage("Error al eliminar archivo: " + e.getMessage()));
        }
    }
}
