package fpt.edu.capstone.vms.exception;

import java.io.Serial;

public class NotFoundException extends Exception {

    @Serial
    private static final long serialVersionUID = 3976207482189111649L;
    private static final String message = "Not Found Entity In Database";

    public NotFoundException() {
        super(message);
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
