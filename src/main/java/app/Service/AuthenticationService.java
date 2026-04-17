package app.Service;

import app.Config.JwtService;
import app.DTO.Auth.AuthRequest;
import app.DTO.Auth.AuthResponse;
import app.DTO.Auth.SignUpRequest;
import app.DTO.Auth.SignUpResponse;
import app.Database.AccountActivation;
import app.Database.User;
import app.Exception.APIException;
import app.Repository.AccountActivationRepository;
import app.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountActivationRepository accountActivationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 6;
    private static final Random RANDOM = new SecureRandom();

    public SignUpResponse register(SignUpRequest request) {
        if(request.getEmail() != null) {
            boolean existsByEmail = userRepository.existsByEmail(request.getEmail());
            if(existsByEmail) {
                throw new APIException(
                        "Account already exists, Sign Up",
                        "AuthenticationService.register",
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(false);
        if(request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);

        // Send activation code
        Boolean codeSent = sendActivationCode(user);

        return new SignUpResponse(
                user.getEmail(),
                codeSent,
                codeSent ? "Activation code sent successfully" : "Failed to send activation code"
        );
    }

    public Boolean sendActivationCode(User user) {
        // Generate activation code
        String activationCode = generateActivationCode();

        try {
            // Send email with Thymeleaf template
            Context context = new Context();
            context.setVariable("userName", user.getName());
            context.setVariable("activationCode", activationCode);

            emailService.sendEmailWithTemplate(
                    user.getEmail(),
                    "Activate Your Account",
                    "activation-email",
                    context
            );
        } catch (Exception e) {
            System.err.println("Error Sending Email: " + e.getMessage());
            return false;
        }

        // Create and save AccountActivation entity
        AccountActivation activation = new AccountActivation();
        activation.setActivationCode(activationCode);
        activation.setUser(user);
        accountActivationRepository.save(activation);

        return true;
    }

    public String generateActivationCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    public void verifyActivationCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new APIException(
                        "User Not Found",
                        "ActivationService.verifyActivationCode",
                        HttpStatus.BAD_REQUEST
                ));

        AccountActivation activation = accountActivationRepository
                .findByUserAndActivationCodeAndActivatedAtIsNull(user, code)
                .orElseThrow(() -> new APIException(
                        "Invalid Activation Code",
                        "ActivationService.verifyActivationCode",
                        HttpStatus.BAD_REQUEST
                ));

        if (activation.isExpired()) {
            throw new APIException(
                    "Activation code expired",
                    "ActivationService.verifyActivationCode",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (activation.isActivated()) {
            throw new APIException(
                    "Activation code already used",
                    "ActivationService.verifyActivationCode",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Mark as activated
        activation.setActivatedAt(LocalDateTime.now());
        accountActivationRepository.save(activation);

        // Activate user account
        user.setActive(true);
        userRepository.save(user);
    }

    public SignUpResponse resendActivationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new APIException(
                        "User not Found",
                        "ActivationService.resentActivationCode",
                        HttpStatus.BAD_REQUEST
                ));

        if (user.getActive()) {
            throw new APIException(
                    "Account is already activated",
                    "ActivationService.resendActivationCode",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Send activation code
        Boolean codeSent = sendActivationCode(user);

        return new SignUpResponse(
                user.getEmail(),
                codeSent,
                codeSent ? "Activation code sent successfully" : "Failed to send activation code"
        );
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();

        try {
            // Check if user is activated
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new APIException(
                            "User not Found",
                            "ActivationService.authenticate",
                            HttpStatus.BAD_REQUEST
                    ));

            if (!user.getActive()) {
                throw new APIException(
                        "Account not activated. Please check your email for activation code.",
                        "ActivationService.authenticate",
                        HttpStatus.BAD_REQUEST
                );
            }

            System.out.println("Authentication manager");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );
            System.out.println("Authentication manager Completed");

            String jwtToken = jwtService.generateToken(email);
            System.out.println("Sending Token: " + jwtToken);
            return new AuthResponse(jwtToken, email);
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            throw new APIException(
                    "Authentication failed: " + e.getMessage(),
                    "AuthenticationService.authenticate",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
