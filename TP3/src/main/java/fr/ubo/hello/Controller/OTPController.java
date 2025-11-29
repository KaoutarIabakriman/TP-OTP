// OTPController.java
package fr.ubo.hello.Controller;

import fr.ubo.hello.Service.OTPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class OTPController {

    private static final Logger logger = LoggerFactory.getLogger(OTPController.class);

    @Autowired
    private OTPService otpService;

    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, String>> requestOTP(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Demande OTP reçue pour: {}", email);

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Email requis"));
        }

        try {
            boolean success = otpService.requestOTP(Integer.parseInt(email));

            if (success) {
                logger.info("OTP envoyé avec succès à: {}", email);
                return ResponseEntity.ok(createSuccessResponse("OTP envoyé avec succès"));
            } else {
                logger.warn("Échec envoi OTP pour: {}", email);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(createErrorResponse("Impossible d'envoyer l'OTP. Veuillez réessayer."));
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la demande OTP pour: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur"));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOTP(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otpCode = request.get("otpCode");

        logger.info("Vérification OTP pour: {}", email);

        if (email == null || otpCode == null) {
            return ResponseEntity.badRequest().body(createErrorResponse("Email et code OTP requis"));
        }

        try {
            boolean isValid = otpService.verifyOTP(Integer.parseInt(email), otpCode);

            if (isValid) {
                logger.info("OTP vérifié avec succès pour: {}", email);
                return ResponseEntity.ok(createSuccessResponse("Authentification réussie"));
            } else {
                logger.warn("OTP invalide pour: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Code OTP invalide ou expiré"));
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la vérification OTP pour: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur"));
        }
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }
}