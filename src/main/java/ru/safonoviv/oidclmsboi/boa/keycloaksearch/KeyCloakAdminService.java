package ru.safonoviv.oidclmsboi.boa.keycloaksearch;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeyCloakAdminService {
    private static final String REALM_NAME = "myoidc";
    @Autowired
    private Keycloak keycloak;

    public boolean userIsPresent(String username) {
        return !keycloak.realm(REALM_NAME).users().searchByUsername(username, true).isEmpty();
    }

    public boolean userHasRole(String username, String roleName) {
        return keycloak.realm(REALM_NAME)
                .roles()
                .get(roleName)
                .getUserMembers().stream().anyMatch(t->t.getUsername().equals(username));
    }

    public List<UserRepresentation> searchByRole(String roleName) {
        return keycloak.realm(REALM_NAME)
                .roles()
                .get(roleName)
                .getUserMembers();
    }
    public boolean containsUsernames(List<String> usernames) {
        return usernames.stream().filter(t->!keycloak.realm(REALM_NAME).users()
                .searchByUsername(t, true).isEmpty()).findFirst().isEmpty();
    }

    public List<UserRepresentation> searchByUsername(String username, boolean exact) {
        return keycloak.realm(REALM_NAME).users()
                .searchByUsername(username, true);
    }
}
