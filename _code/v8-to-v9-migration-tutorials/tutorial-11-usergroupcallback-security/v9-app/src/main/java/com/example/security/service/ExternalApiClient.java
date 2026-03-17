package com.example.security.service;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * v9 REST Client interface using MicroProfile REST Client
 * Replaces manual HTTP client configuration from v8
 *
 * Note: No @Path at class level - the base URL is configured in application.properties
 * Individual methods define their paths relative to the base URL
 */
@RegisterRestClient(configKey = "external-api")
public interface ExternalApiClient {
    
    /**
     * POST request to external service
     */
    @POST
    @Path("/{endpoint}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response postData(@PathParam("endpoint") String endpoint, String payload);
    
    /**
     * GET request to external service
     */
    @GET
    @Path("/{endpoint}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getData(@PathParam("endpoint") String endpoint);
}


