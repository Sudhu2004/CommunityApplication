package app.DTO.Auth;

public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String userEmail;
    private String userCode;

    public AuthResponse(String accessToken, String userEmail, String userCode) {
        this.accessToken = accessToken;
        this.userEmail = userEmail;
        this.userCode = userCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
}
