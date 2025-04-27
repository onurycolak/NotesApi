package com.onur.bootcamp;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;

import java.io.IOException;

@Provider
public class ApiKeyAuthFilter implements ContainerRequestFilter {
    private static final String API_KEY_HEADER = "X-API-Key";

    @Inject
    @ConfigProperty(name = "app.api-key")
    String apiKey;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod().toUpperCase();

        if (path.startsWith("/notes") &&  (method.equals("DELETE") || method.equals("POST") || method.equals("PUT"))) {
            String header = requestContext.getHeaderString(API_KEY_HEADER);
            if (header == null || !header.equals(apiKey)) {
                ErrorResponse error = new ErrorResponse("Missing or invalid API key", 401);
                requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                                .entity(error)
                                .type("application/json")
                                .build()
                );
            }
        }
    }
}
