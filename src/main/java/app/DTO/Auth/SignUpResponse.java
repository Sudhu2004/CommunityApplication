package app.DTO.Auth;

public class SignUpResponse {

    private String email;
    private boolean activationCodeDelivered;
    private String message;

    // Constructors
    public SignUpResponse() {}

    public SignUpResponse(String email, boolean activationCodeSent, String message) {
        this.email = email;
        this.activationCodeDelivered = activationCodeSent;
        this.message = message;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivationCodeDelivered() {
        return activationCodeDelivered;
    }

    public void setActivationCodeDelivered(boolean activationCodeDelivered) {
        this.activationCodeDelivered = activationCodeDelivered;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
