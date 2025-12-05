package com.example.authserver.config;// Source - https://stackoverflow.com/a
// Posted by pakkk, modified by community. See post 'Timeline' for change history
// Retrieved 2025-12-05, License - CC BY-SA 3.0


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthExceptionEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2) throws IOException, ServletException {
        final Map<String, Object> mapBodyException = new HashMap<>();

        mapBodyException.put("error", "Error from AuthenticationEntryPoint");
        mapBodyException.put("message", "Message from AuthenticationEntryPoint");
        mapBodyException.put("exception", "My stack trace exception");
        mapBodyException.put("path", request.getServletPath());
        mapBodyException.put("timestamp", (new Date()).getTime());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), mapBodyException);
    }
}
