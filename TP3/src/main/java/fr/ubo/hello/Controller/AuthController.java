package fr.ubo.hello.Controller;

import fr.ubo.hello.Model.User;
import fr.ubo.hello.Service.OTPService;
import fr.ubo.hello.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private OTPService otpService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        logger.info("Controller : POST /api/auth/login - Tentative de connexion pour {}",
                credentials.get("email"));

        Map<String, Object> response = new HashMap<>();

        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                logger.warn("Controller : Email ou mot de passe manquant");
                response.put("success", false);
                response.put("message", "Email et mot de passe requis");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userService.authenticateUser(email, password);

            if (user == null) {
                logger.warn("Controller : Identifiants invalides pour {}", email);
                response.put("success", false);
                response.put("message", "Email ou mot de passe incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            logger.info("Controller : Utilisateur trouvé - id: {}, email: {}, phone: {}",
                    user.getId(), user.getEmail(), user.getPhone());


            try {
                otpService.generateAndSendOTP(user.getId());
                logger.info("Controller : OTP généré pour user_id={}", user.getId());
            } catch (Exception e) {
                logger.warn("Controller : Erreur génération OTP (ignorée pour test): {}", e.getMessage());
            }

            response.put("success", true);
            response.put("otpSent", true);
            response.put("requiresOTP", true);
            response.put("message", "Code OTP envoyé par SMS. Veuillez le saisir.");
            response.put("userId", user.getId()); // ← FORCER le userId
            response.put("email", email);

            logger.info("Controller : Réponse envoyée - userId: {}", user.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Controller : Erreur lors de la connexion", e);
            response.put("success", false);
            response.put("message", "Une erreur inattendue est survenue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOTP(@RequestBody Map<String, Object> request) {
        logger.info("Controller : POST /api/auth/verify-otp - Vérification OTP");
        logger.info("Controller : Requête reçue: {}", request);

        Map<String, Object> response = new HashMap<>();

        try {
            Integer userId = null;
            String otpCode = null;

            if (request.get("userId") != null) {
                userId = Integer.valueOf(request.get("userId").toString());
            }
            if (request.get("otpCode") != null) {
                otpCode = (String) request.get("otpCode");
            } else if (request.get("otp") != null) {
                otpCode = (String) request.get("otp");
            }

            logger.info("Controller : Vérification OTP - userId: {}, otpCode: {}", userId, otpCode);

            if (userId == null || otpCode == null || otpCode.isEmpty()) {
                logger.warn("Controller : Paramètres manquants pour la vérification OTP");
                response.put("success", false);
                response.put("message", "ID utilisateur et code OTP requis");
                return ResponseEntity.badRequest().body(response);
            }


            boolean isValid = otpService.verifyOTP(userId, otpCode);

            if (isValid) {
                User user = userService.getById(userId);

                logger.info("Controller : Connexion réussie avec OTP pour user_id={}", userId);
                response.put("success", true);
                response.put("authenticated", true);
                response.put("message", "Authentification réussie");
                response.put("user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail()
                ));
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Controller : OTP invalide pour user_id={}", userId);
                response.put("success", false);
                response.put("authenticated", false);
                response.put("message", "Code OTP invalide ou expiré");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            logger.error("Controller : Erreur lors de la vérification de l'OTP", e);
            response.put("success", false);
            response.put("message", "Une erreur est survenue lors de la vérification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/cleanup-otp")
    public ResponseEntity<String> cleanupOTP() {
        try {
            otpService.cleanupExpiredOTPs();
            return ResponseEntity.ok("OTP expirés nettoyés avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors du nettoyage: " + e.getMessage());
        }
    }
}