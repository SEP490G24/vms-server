package fpt.edu.capstone.vms.constants;

public class Constants {

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
