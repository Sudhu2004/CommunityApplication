package app.Exception;

import org.springframework.http.HttpStatus;

public class APIException extends RuntimeException {

    private final String message;
    private final String trace;
    private final HttpStatus status;

    public APIException(String message, String trace, HttpStatus status) {
        super(message);
        this.message = message;
        this.trace = trace;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public String getTrace() {
        return trace;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
