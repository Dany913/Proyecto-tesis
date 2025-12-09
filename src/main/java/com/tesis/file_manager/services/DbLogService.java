package com.tesis.file_manager.services;

import com.tesis.file_manager.entity.DbLog;
import com.tesis.file_manager.repository.DbLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DbLogService {

    private final DbLogRepository dbLogRepository;

    public void saveLog(String username, String severity, String message) {
        DbLog log = DbLog.builder()
                .logTime(LocalDateTime.now())
                .userName(username)
                .databaseName("API_DB")
                .connectionFrom("localhost")
                .errorSeverity(severity)
                .message(message)
                .applicationName("SpringBootAPI")
                .build();

        dbLogRepository.save(log);
    }
}

