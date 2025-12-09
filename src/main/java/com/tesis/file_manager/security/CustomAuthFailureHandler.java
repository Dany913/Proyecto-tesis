package com.tesis.file_manager.security;

import com.tesis.file_manager.entity.DbLog;
import com.tesis.file_manager.repository.DbLogRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    private final DbLogRepository dbLogRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        DbLog log = DbLog.builder()
                .logTime(LocalDateTime.now())
                .userName(request.getParameter("username"))
                .databaseName("db_log")
                .connectionFrom(request.getRemoteAddr())
                .errorSeverity("FATAL")
                .message("Authentication failed: " + exception.getMessage())
                .applicationName("SpringBootAPI")
                .build();
        System.out.println("🚨 Fallo de autenticación detectado para: " + request.getParameter("username"));

        dbLogRepository.save(log);

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
    }
}


