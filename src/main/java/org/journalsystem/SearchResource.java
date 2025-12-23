package org.journalsystem;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.journalsystem.dto.EncounterSearchResult;
import org.journalsystem.dto.PatientSearchResult;
import org.journalsystem.service.SearchService;

import java.util.List;

/**
 * REST API for searching patients, conditions, and encounters.
 * Secured with Keycloak JWT authentication.
 */
@Path("/api/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    SearchService searchService;

    /**
     * Search patients by name - accessible by DOCTOR and STAFF
     */
    @GET
    @Path("/patients")
    @RolesAllowed({"doctor", "staff"})
    public Uni<List<PatientSearchResult>> searchPatients(
            @QueryParam("name") String name,
            @QueryParam("condition") String condition,
            @QueryParam("practitionerId") String practitionerId) {

        String username = securityIdentity.getPrincipal().getName();
        System.out.println("User " + username + " is searching for patients");
        System.out.println("Parameters - name: " + name + ", condition: " + condition + ", practitionerId: " + practitionerId);

        // Determine which search to perform based on parameters
        if (name != null && !name.trim().isEmpty()) {
            System.out.println("Searching by name: " + name);
            return searchService.searchPatientsByName(name);
        } else if (condition != null && !condition.trim().isEmpty()) {
            System.out.println("Searching by condition: " + condition);
            return searchService.searchPatientsByCondition(condition);
        } else if (practitionerId != null && !practitionerId.trim().isEmpty()) {
            System.out.println("Searching by practitionerId: " + practitionerId);
            return searchService.searchPatientsByPractitionerId(practitionerId);
        } else {
            System.out.println("No search parameters provided, returning empty list");
            return Uni.createFrom().item(List.of());
        }
    }

    /**
     * Search encounters - accessible by DOCTOR only
     */
    @GET
    @Path("/encounters")
    @RolesAllowed({"doctor"})
    public Uni<List<EncounterSearchResult>> searchEncounters(
            @QueryParam("practitionerId") String practitionerId,
            @QueryParam("date") String date) {

        String username = securityIdentity.getPrincipal().getName();
        System.out.println("User " + username + " is searching for encounters");
        System.out.println("Parameters - practitionerId: " + practitionerId + ", date: " + date);

        if (practitionerId == null || practitionerId.trim().isEmpty()) {
            System.out.println("No practitionerId provided, returning empty list");
            return Uni.createFrom().item(List.of());
        }

        return searchService.searchEncountersByPractitioner(practitionerId, date);
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

        System.out.println("User " + username + " requested their info. Roles: " + roles);

        return Response.ok()
                .entity(new UserInfo(username, roles))
                .build();
    }

    record UserInfo(String username, java.util.Set<String> roles) {}
}