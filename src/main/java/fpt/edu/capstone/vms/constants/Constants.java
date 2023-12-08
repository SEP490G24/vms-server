package fpt.edu.capstone.vms.constants;

public class Constants {

    public static final String createdOn = "createdOn";
    public static final String  lastUpdatedOn = "lastUpdatedOn";

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
        /* Setting for Mail */
        public static final String MAIL_HOST = "mail.host";
        public static final String MAIL_PORT = "mail.port";
        public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
        public static final String MAIL_DEBUG = "mail.debug";
        public static final String MAIL_PROTOCOL = "mail.protocol";
        public static final String MAIL_TYPE = "mail.type";
        public static final String MAIL_USERNAME = "mail.username";
        public static final String MAIL_PASSWORD = "mail.password";
        public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
        public static final String MAIL_SMTP_DISPLAY_NAME = "mail.smtp.display.name";
        /* Setting for Ticket */
        public static final String TICKET_TEMPLATE_CONFIRM_EMAIL = "ticket.template.confirm.email";
        public static final String TICKET_TEMPLATE_CANCEL_EMAIL = "ticket.template.cancel.email";
        public static final String TICKET_TEMPLATE_UPCOMING_EMAIL = "ticket.template.upcoming.email";

        /* Setting for Configuration */

        public static final String CONFIGURATION_CARD = "configuration.card";


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
        CANCEL,
        COMPLETE
    }

    public enum StatusCustomerTicket {
        PENDING,
        CHECK_IN,
        CHECK_OUT,
        REJECT
    }

    public enum StatusCheckInCard {
        APPROVED,
        DENIED
    }

    public enum DeviceType {
        SCAN_CARD,
        DOOR
    }

    public enum CustomerCheckType {
        EMAIL,
        PHONE_NUMBER,
        IDENTIFICATION_NUMBER
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
        CANCEL_MEETING_EMAIL,
        CONFIRM_MEETING_EMAIL,
        UPCOMING_MEETING_EMAIL
    }

    public enum SettingType {
        INPUT,
        SWITCH,
        SELECT,
        API
    }

    public enum Purpose {
        CONFERENCES,
        INTERVIEW,
        MEETING,
        OTHERS,
        WORKING
    }

    public enum Reason {
        CANCEL,
        REJECT;
    }

    public enum AuditType {
        CREATE,
        UPDATE,
        DELETE,
        NONE
    }

    public enum StatusAction {
        DONE,
        ERROR
    }
}
