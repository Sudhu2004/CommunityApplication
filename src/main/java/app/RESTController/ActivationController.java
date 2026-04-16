package app.RESTController;

import app.Config.JwtService;
import app.DTO.Auth.ActivationRequest;
import app.DTO.Auth.AuthResponse;
import app.DTO.Auth.ResendCodeRequest;
import app.DTO.Auth.SignUpResponse;
import app.Service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class ActivationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService; // ✅ inject, don’t create manually

    @PostMapping("/verify-activation")
    public ResponseEntity<AuthResponse> verifyActivation(
            @RequestBody ActivationRequest request) {

        // This will throw exception if invalid → no need for boolean check
        authenticationService.verifyActivationCode(
                request.getEmail(),
                request.getCode()
        );

        // If no exception → success
        String token = jwtService.generateToken(request.getEmail());

        AuthResponse response = new AuthResponse(
                token,
                request.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<SignUpResponse> resendActivation(
            @RequestBody ResendCodeRequest request) {

        SignUpResponse signUpResponse =
                authenticationService.resendActivationCode(request.getEmail());

        return ResponseEntity.ok(signUpResponse);
    }
}
