package fr.ubo.hello.Service;

import fr.ubo.hello.Dao.OTPDao;
import fr.ubo.hello.Model.OTP;
import fr.ubo.hello.Model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Service responsible for OTP (One-Time Password) operations.
 * Handles OTP generation, validation, sending via SMS, and cleanup of expired OTPs.
 */
@Service
public class OTPService {

    private static final Logger logger = LoggerFactory.getLogger(OTPService.class);
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 2;
    private static final int MAX_OTP_PER_30_MIN = 3;

    @Autowired
    private OTPDao otpDao;

    @Autowired
    private UserService userService;

    @Autowired
    private SMSService smsService;

    private final SecureRandom random = new SecureRandom();

    /**
     * Cleans and formats a phone number for SMS sending.
     *
     * @param phoneNumber raw phone number.
     * @return cleaned phone number in standard French format, or null if invalid.
     */
    private String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        if (cleaned.startsWith("33") && cleaned.length() == 11) {
            cleaned = "0" + cleaned.substring(2);
        }

        if (cleaned.length() == 10 && cleaned.startsWith("0")) {
            logger.debug("Service : Numéro nettoyé: {} -> {}", phoneNumber, cleaned);
            return cleaned;
        }

        logger.error("Service : Format de numéro invalide: {} (nettoyé: {})", phoneNumber, cleaned);
        return null;
    }

    /**
     * Generates a random numeric OTP code.
     *
     * @return OTP as a string.
     */
    private String generateOTPCode() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        String generatedOTP = otp.toString();
        logger.debug("Service : Code OTP généré: {}", generatedOTP);
        return generatedOTP;
    }

    /**
     * Sends an OTP via SMS.
     *
     * @param phoneNumber destination phone number.
     * @param otpCode     OTP code to send.
     * @return true if SMS successfully sent, false otherwise.
     */
    private boolean sendSMS(String phoneNumber, String otpCode) {
        try {
            String cleanPhone = cleanPhoneNumber(phoneNumber);

            if (cleanPhone == null) {
                logger.error("Service : Numéro de téléphone invalide après nettoyage: {}", phoneNumber);
                return false;
            }

            boolean smsSent = smsService.sendOTP(cleanPhone, otpCode);
            return smsSent;

        } catch (Exception e) {
            logger.error("Service : Exception lors de l'envoi SMS via API: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Requests an OTP for a user (alias for generateAndSendOTP).
     *
     * @param userId user ID.
     * @return true if OTP generated successfully.
     */
    public boolean requestOTP(int userId) throws InterruptedException {
        return generateAndSendOTP(userId);
    }

    /**
     * Generates a new OTP, saves it in DB, and sends it via SMS.
     *
     * @param userId user ID.
     * @return true if OTP successfully generated and sent (or in dev mode, just generated).
     */
    public boolean generateAndSendOTP(int userId) throws InterruptedException {
        logger.info("Service : Début génération OTP pour user_id={}", userId);

        try {
            int recentOTPCount = otpDao.countRecentOTPsByUserId(userId, 30);
            if (recentOTPCount >= MAX_OTP_PER_30_MIN) {
                throw new RuntimeException("Trop de demandes d'OTP. Veuillez patienter 30 minutes.");
            }

            User user = userService.getById(userId);
            if (user.getPhone() == null || user.getPhone().isEmpty()) {
                throw new RuntimeException("Aucun numéro de téléphone associé à ce compte.");
            }

            String otpCode = generateOTPCode();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

            OTP otp = new OTP(userId, otpCode, expiresAt);

            if (!otpDao.save(otp)) {
                throw new RuntimeException("Erreur lors de la génération de l'OTP.");
            }

            boolean smsSent = false;
            int maxRetries = 2;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                smsSent = sendSMS(user.getPhone(), otpCode);
                if (smsSent) break;
                if (attempt < maxRetries) Thread.sleep(3000);
            }

            if (!smsSent) {
                logger.warn("MODE DÉVELOPPEMENT - Échec envoi SMS. OTP généré: {} pour user_id={}", otpCode, userId);
            }

            return true;

        } catch (Exception e) {
            logger.error("Service : Erreur lors de la génération/envoi de l'OTP pour user_id={}", userId, e);
            throw e;
        }
    }

    /**
     * Checks if SMS server is available.
     *
     * @return true if available.
     */
    public boolean isSMSServerAvailable() {
        return smsService.isSMSServerAvailable();
    }

    /**
     * Verifies the OTP code for a user.
     *
     * @param userId  user ID.
     * @param otpCode OTP code.
     * @return true if OTP valid.
     */
    public boolean verifyOTP(int userId, String otpCode) {
        try {
            OTP otp = otpDao.findValidOTP(userId, otpCode);
            if (otp != null) {
                otpDao.markAsUsed(otp.getId());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Service : Erreur lors de la vérification de l'OTP pour user_id={}", userId, e);
            return false;
        }
    }

    /**
     * Deletes expired OTPs from the database.
     */
    public void cleanupExpiredOTPs() {
        if (otpDao != null) {
            boolean deletedCount = otpDao.deleteExpiredOTPs();
            logger.info("Service : {} OTP expirés supprimés", deletedCount);
        }
    }

    /**
     * Checks if a user can request a new OTP based on rate limiting.
     *
     * @param userId user ID.
     * @return true if user can request OTP.
     */
    public boolean canRequestOTP(int userId) {
        try {
            int recentOTPCount = otpDao.countRecentOTPsByUserId(userId, 30);
            return recentOTPCount < MAX_OTP_PER_30_MIN;
        } catch (Exception e) {
            logger.error("Service : Erreur lors de la vérification de l'éligibilité OTP", e);
            return false;
        }
    }
}
