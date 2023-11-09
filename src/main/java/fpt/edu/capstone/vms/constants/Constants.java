package fpt.edu.capstone.vms.constants;

public class Constants {

    public static class Claims {
        public static String OrgId = "org_id";
        public static String SiteId = "site_id";
        public static String Name = "name";
        public static String PreferredUsername = "preferred_username";
        public static String GivenName = "given_name";
        public static String FamilyName = "family_name";
        public static String Email = "email";
    }

    public static class SettingCode {
        public static String MAIL_HOST = "mail.host";
        public static String MAIL_PORT = "mail.port";
        public static String MAIL_SMTP_AUTH = "mail.smtp.auth";
        public static String MAIL_DEBUG = "mail.debug";
        public static String MAIL_PROTOCOL = "mail.protocol";
        public static String MAIL_TYPE = "mail.type";
        public static String MAIL_USERNAME = "mail.username";
        public static String MAIL_PASSWORD = "mail.password";
        public static String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    }

    public static final String[] IGNORE_CLIENT_ID_KEYCLOAK = new String[]{"account", "account-console", "admin-cli", "broker",
        "realm-management", "realm-management", "security-admin-console"};

    public static final String[] IGNORE_ROLE_REALM_KEYCLOAK = new String[]{"default-roles-cep", "offline_access", "uma_authorization"};


    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public final static int PAGE_SIZE = 10;

    public enum UserState {
        AVAILABLE, UNAVAILABLE, OFFLINE
    }

    public enum UserRole {
        ORG_ADMIN,
        SYS_ADMIN,
        STAFF,
        GUARD,
        RECEPTIONIST
    }

    public enum StatusTicket {
        DRAFT,
        PENDING,
        CHECK_IN,
        CHECK_OUT,
        CANCEL,
        REJECT
    }

    public enum Gender {
        FEMALE,
        MALE,
        OTHER
    }

    public enum FileType {
        PDF,
        IMAGE_AVATAR,
        IMAGE,
        EXCEL,
        WORD
    }

    public enum PermissionType {
        CREATE,
        UPDATE,
        FIND,
        DELETE,
        READ

    }

    public enum TemplateType {
        EMAIL,
        SMS,
        CANCEL_MEETING,
        REFUSE_MEETING
    }

    public enum SettingType {
        INPUT,
        SWITCH,
        SELECT
    }

    public enum Purpose {
        CONFERENCES,
        INTERVIEW,
        MEETING,
        OTHERS,
        WORKING
    }

    public enum Reason {
        URGENT_ISSUE,
        SCHEDULE_CONFLICT,
        KEY_PARTICIPANTS_UNAVAILABLE,
        NOT_PREPARED,
        BAD_WEATHER,
        OVERCROWDED,
        TECHNICAL_ISSUES;
    }
}
