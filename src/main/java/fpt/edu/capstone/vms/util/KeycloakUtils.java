package fpt.edu.capstone.vms.util;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;

@Slf4j
public class KeycloakUtils {

    /**
      * Find id of client with client id
      *
      * @param realmResource realm keycloak.
      * @param clientId client id to find
      * @return id if exists, null if not exists
      * */
    public static String findIdClient(RealmResource realmResource, String clientId) {

        var find = realmResource.clients().findByClientId(clientId);
        if (null == find || find.isEmpty()){
            log.error("Client not exists with client id: {}", clientId);
            return null;
        }
        return find.get(0).getId();
    }

    /**
      * Find id of user with username
      *
      * @param realmResource realm keycloak.
      * @param username username to find
      * @return id if exists, null if not exists
      * */
    public static String findIdUser(RealmResource realmResource, String username) {

        var find = realmResource.users().searchByUsername(username, true);
        if (null == find || find.isEmpty()){
            log.error("User not exists with username: {}", username);
            return null;
        }
        return find.get(0).getId();
    }

}
