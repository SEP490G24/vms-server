package fpt.edu.capstone.vms.constants;

public class Constants {

    public static class Claims {
        public static String OrgId = "org_id";
        public static String Name = "name";
        public static String PreferredUsername = "preferred_username";
        public static String GivenName = "given_name";
        public static String FamilyName = "family_name";
        public static String Email = "email";
    }

    public static final String[] IGNORE_CLIENT_ID_KEYCLOAK = new String[] {"account", "account-console", "admin-cli", "broker",
            "realm-management", "realm-management", "security-admin-console" };

    public static final String[] IGNORE_ROLE_REALM_KEYCLOAK = new String[] {"default-roles-cep", "offline_access", "uma_authorization" };

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public final static int PAGE_SIZE = 10;

    public enum UserState {
        AVAILABLE, UNAVAILABLE, OFFLINE
    }

    public enum UserRole {
        BUSINESS_ACCOUNT,
        AGENT_ACCOUNT
    }

}
