package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Role;
import Host_Usach_Cloud.Backend.Entity.Users;
import Host_Usach_Cloud.Backend.Entity.User_role;
import Host_Usach_Cloud.Backend.Repository.RoleRepository;
import Host_Usach_Cloud.Backend.Repository.UserRepository;
import Host_Usach_Cloud.Backend.Repository.UserRoleRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final Keycloak keycloak;
    private final TransactionTemplate transactionTemplate;

    @Value("${keycloak.target-realm:host-usach}")
    private String targetRealm;

    /**
     * Crea un usuario normal (con rol 'user' por defecto)
     */
    public Mono<Users> createUser(String email, String name, String password) {
        return createUserWithRole(email, name, password, "user");
    }

    /**
     * Crea un administrador (con rol 'admin')
     */
    public Mono<Users> createAdmin(String email, String name, String password) {
        return createUserWithRole(email, name, password, "admin");
    }

    private Mono<Users> createUserWithRole(String email, String name, String password, String roleName) {
        return Mono.fromCallable(() -> {
            RealmResource realmResource = keycloak.realm(targetRealm);
            UsersResource usersResource = realmResource.users();
            String keycloakUserId = null;

            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setUsername(email); // Usamos el email como username
            userRepresentation.setEmail(email);
            userRepresentation.setFirstName(name);
            userRepresentation.setLastName("");
            userRepresentation.setEnabled(true);
            userRepresentation.setEmailVerified(true);
            userRepresentation.setRequiredActions(Collections.emptyList());

            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(password);
            userRepresentation.setCredentials(Collections.singletonList(passwordCred));

            try (Response response = usersResource.create(userRepresentation)) {
                if (response.getStatus() != 201) {
                    throw new RuntimeException("Error al crear usuario en Keycloak. Status: " + response.getStatus());
                }

                if (response.getLocation() == null || response.getLocation().getPath() == null) {
                    throw new RuntimeException("Keycloak no devolvio la ubicacion del usuario creado");
                }

                // Obtener el ID del usuario recién creado en Keycloak
                keycloakUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            }

            try {
                // Evita bloqueos de grant_type=password por acciones requeridas pendientes
                UserRepresentation createdUser = usersResource.get(keycloakUserId).toRepresentation();
                createdUser.setRequiredActions(Collections.emptyList());
                createdUser.setEmailVerified(true);
                createdUser.setEnabled(true);
                usersResource.get(keycloakUserId).update(createdUser);

                RoleRepresentation realmRole = realmResource.roles().get(roleName).toRepresentation();
                usersResource.get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(realmRole));

                return persistUserAndRoleInDatabase(email, name, roleName);
            } catch (Exception e) {
                rollbackKeycloakUser(usersResource, keycloakUserId, email, e);
                throw new RuntimeException("No se pudo completar el registro atomico: " + e.getMessage(), e);
            }

        }).subscribeOn(Schedulers.boundedElastic()); // Ejecutar en hilo separado porque son operaciones bloqueantes
    }

    private Users persistUserAndRoleInDatabase(String email, String name, String roleName) {
        return transactionTemplate.execute(status -> {
            Users newUser = Users.builder()
                    .Email(email)
                    .Name(name)
                    .Max_instances(3)
                    .Lock(false)
                    .build();

            Users savedUser = userRepository.save(newUser);

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Rol '" + roleName + "' no encontrado en BD"));

            User_role userRole = User_role.builder()
                    .User_id(savedUser.getUser_id())
                    .Role_id(role.getRole_id())
                    .build();

            userRoleRepository.save(userRole);
            log.info("Rol {} sincronizado en BD para usuario {}", roleName, email);
            return savedUser;
        });
    }

    private void rollbackKeycloakUser(UsersResource usersResource, String keycloakUserId, String email, Exception cause) {
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            log.error("Registro atomico fallido para {} y no se pudo compensar en Keycloak: ID nulo", email, cause);
            return;
        }

        try {
            usersResource.get(keycloakUserId).remove();
            log.warn("Registro atomico fallido para {}. Usuario compensado en Keycloak", email);
        } catch (Exception deleteError) {
            log.error("Registro atomico fallido para {} y no se pudo eliminar en Keycloak (id={}): {}", email, keycloakUserId, deleteError.getMessage(), deleteError);
        }
    }
}
