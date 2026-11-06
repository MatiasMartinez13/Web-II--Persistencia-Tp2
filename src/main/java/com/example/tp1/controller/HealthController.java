package com.example.tp1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Salud", description = "Endpoint de estado del servicio (público, útil para verificar despliegues).")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Estado del servicio", description = "Responde 200 con status UP si la aplicación está activa.")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
