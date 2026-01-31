package app.Service;

import app.Config.JwtService;
import app.DTO.Auth.AuthRequest;
import app.DTO.Auth.AuthResponse;
import app.DTO.Auth.SignUpRequest;
import app.Database.AccountActivation;
import app.Database.User;
import app.Repository.AccountActivationRepository;
import app.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public AuthResponse register(SignUpRequest request) {
        if(request.getEmail() != null) {
            boolean existsByEmail = userRepository.existsByEmail(request.getEmail());
            if(existsByEmail) {
                throw new RuntimeException("User Email: " + request.getEmail() + " exists");
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
        sendActivationCode(user);

        String jwtToken = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                jwtToken,
                user.getEmail()
        );
    }

    public void sendActivationCode(User user) {
        // Generate activation code
        String activationCode = generateActivationCode();

        // Create and save AccountActivation entity
        AccountActivation activation = new AccountActivation();
        activation.setActivationCode(activationCode);
        activation.setUser(user);
        accountActivationRepository.save(activation);

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
    }

    public String generateActivationCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    public boolean verifyActivationCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AccountActivation activation = accountActivationRepository
                .findByUserAndActivationCodeAndActivatedAtIsNull(user, code)
                .orElseThrow(() -> new RuntimeException("Invalid activation code"));

        if (activation.isExpired()) {
            throw new RuntimeException("Activation code has expired");
        }

        if (activation.isActivated()) {
            throw new RuntimeException("Activation code already used");
        }

        // Mark as activated
        activation.setActivatedAt(LocalDateTime.now());
        accountActivationRepository.save(activation);

        // Activate user account
        user.setActive(true);
        userRepository.save(user);

        return true;
    }

    public void resendActivationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getActive()) {
            throw new RuntimeException("Account is already activated");
        }

        sendActivationCode(user);
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();

        try {
            // Check if user is activated
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getActive()) {
                throw new RuntimeException("Account not activated. Please check your email for activation code.");
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
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
}
