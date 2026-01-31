package app.Controller;

import app.DTO.Auth.ActivationRequest;
import app.DTO.Auth.ResendCodeRequest;
import app.Service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ActivationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/verify-activation")
    public ResponseEntity<Map<String, Object>> verifyActivation(
            @RequestBody ActivationRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean verified = authenticationService.verifyActivationCode(
                    request.getEmail(),
                    request.getCode()
            );

            if (verified) {
                response.put("success", true);
                response.put("message", "Account activated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid activation code");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<Map<String, Object>> resendActivation(
            @RequestBody ResendCodeRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            authenticationService.resendActivationCode(request.getEmail());
            response.put("success", true);
            response.put("message", "Activation code sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
