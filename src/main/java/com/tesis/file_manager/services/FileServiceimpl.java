package com.tesis.file_manager.services;

import com.tesis.file_manager.entity.Fileentity;
import com.tesis.file_manager.repository.FileRepository;
import com.tesis.file_manager.response.ResponseFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceimpl implements FileService {

    private final FileRepository fileRepository;
    private final DbLogService dbLogService;

    @Override
    public Fileentity store(MultipartFile file, String Nombrepaciente, String Raza, Integer Edad, String Diagnostico) throws IOException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        Fileentity fileentity = Fileentity.builder()
                .name(filename)
                .type(file.getContentType())
                .data(file.getBytes())
                .Nombrepaciente(Nombrepaciente)
                .Raza(Raza)
                .Edad(Edad)
                .Diagnostico(Diagnostico)
                .build();

        Fileentity savedFile = fileRepository.save(fileentity);

        //  registro del evento
        dbLogService.saveLog("UsuarioDesconocido", "INFO", "Archivo subido: " + filename);

        return savedFile;
    }

    @Override
    public Optional<Fileentity> getFile(UUID id) throws FileNotFoundException {
        Optional<Fileentity> file = fileRepository.findById(id);
        if (file.isPresent()) {
            dbLogService.saveLog("UsuarioDesconocido", "INFO", "Archivo descargado con ID: " + id);
            return file;
        }
        dbLogService.saveLog("UsuarioDesconocido", "ERROR", "Intento de descarga fallido: archivo no encontrado con ID " + id);
        throw new FileNotFoundException("Archivo no encontrado");
    }

    @Override
    public List<ResponseFile> getAllFiles() {
        dbLogService.saveLog("UsuarioDesconocido", "INFO", "Consulta de lista de archivos realizada");
        return fileRepository.findAll().stream().map(dbFile -> {
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("api/fileManager/files/")
                    .path(dbFile.getId().toString())
                    .toUriString();

            return ResponseFile.builder()
                    .Nombrepaciente(dbFile.getNombrepaciente())
                    .Edad(dbFile.getEdad())
                    .Raza(dbFile.getRaza())
                    .Diagnostico(dbFile.getDiagnostico())
                    .name(dbFile.getName())
                    .url(fileDownloadUri)
                    .type(dbFile.getType())
                    .size(dbFile.getData().length)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteFile(UUID id) throws FileNotFoundException {
        Optional<Fileentity> file = fileRepository.findById(id);
        if (file.isPresent()) {
            fileRepository.delete(file.get());
            dbLogService.saveLog("UsuarioDesconocido", "WARNING", "Archivo eliminado con ID: " + id);
        } else {
            dbLogService.saveLog("UsuarioDesconocido", "ERROR", "Intento de eliminación fallido: archivo no encontrado con ID " + id);
            throw new FileNotFoundException("Archivo no encontrado con ID: " + id);
        }
    }
}
