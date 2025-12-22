package org.journalsystem;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Exempel på hur du kan lägga till rollbaserad säkerhet i Quarkus.
 *
 * Lägg till @RolesAllowed på dina befintliga endpoints.
 */
@Path("/api/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Search patients by name - accessible by DOCTOR and STAFF
     */
    @GET
    @Path("/patients")
    @RolesAllowed({"doctor", "staff"})
    public Response searchPatients(@QueryParam("name") String name,
                                   @QueryParam("condition") String condition,
                                   @QueryParam("practitionerId") String practitionerId) {
        // Din befintliga logik här
        // ...

        // Exempel: Logga vem som söker
        String username = securityIdentity.getPrincipal().getName();
        System.out.println("User " + username + " is searching for patients");

        // Returnera resultat
        return Response.ok().build();
    }

    /**
     * Search encounters - accessible by DOCTOR only
     */
    @GET
    @Path("/encounters")
    @RolesAllowed({"doctor"})
    public Response searchEncounters(@QueryParam("practitionerId") String practitionerId,
                                     @QueryParam("date") String date) {
        // Din befintliga logik här
        return Response.ok().build();
    }

    /**
     * Get current user info from token
     */
    @GET
    @Path("/me")
    @RolesAllowed({"doctor", "staff", "patient"})
    public Response getCurrentUser() {
        String username = securityIdentity.getPrincipal().getName();
        var roles = securityIdentity.getRoles();

        return Response.ok()
                .entity(new UserInfo(username, roles))
                .build();
    }

    record UserInfo(String username, java.util.Set<String> roles) {}
}