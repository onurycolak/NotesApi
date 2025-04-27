package com.onur.bootcamp;

import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;

@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {
    @Inject
    NoteService noteService;

    @Override
    public HealthCheckResponse call() {
        try {
            noteService.getLatestNote();

            return HealthCheckResponse.up("notes-api: NoteService DB Readiness");
        } catch (Exception e) {
            return HealthCheckResponse.down("notes-api: NoteService DB Readiness failed");
        }
    }
}