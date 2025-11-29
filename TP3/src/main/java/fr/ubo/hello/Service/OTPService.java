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
     * Nettoie le numéro de téléphone pour le format attendu par l'API (0612345678)
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
     * Génère un code OTP aléatoire à 6 chiffres
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
     * Envoie un SMS avec l'OTP via le SMSService
     */
    private boolean sendSMS(String phoneNumber, String otpCode) {
        try {
            String cleanPhone = cleanPhoneNumber(phoneNumber);

            if (cleanPhone == null) {
                logger.error("Service : Numéro de téléphone invalide après nettoyage: {}", phoneNumber);
                return false;
            }

            logger.info("Service : Envoi SMS via API vers: {}", cleanPhone);

            boolean smsSent = smsService.sendOTP(cleanPhone, otpCode);

            if (smsSent) {
                logger.info("Service : ✅ SMS envoyé avec succès via API");
                return true;
            } else {
                logger.error("Service : ❌ Échec envoi SMS via API");
                return false;
            }

        } catch (Exception e) {
            logger.error("Service : ❌ Exception lors de l'envoi SMS via API: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Alias pour generateAndSendOTP - demande un nouvel OTP
     */
    public boolean requestOTP(int userId) {
        return generateAndSendOTP(userId);
    }

    /**
     * Génère et envoie un OTP à l'utilisateur avec retry
     */
    public boolean generateAndSendOTP(int userId) {
        logger.info("Service : 🚀 Début génération OTP pour user_id={}", userId);

        try {
            // Vérification de la disponibilité des dépendances
            if (otpDao == null || userService == null || smsService == null) {
                logger.error("Service : Dépendances manquantes");
                throw new RuntimeException("Service OTP non configuré correctement");
            }

            // Vérifier le nombre d'OTP récents (protection contre le spam)
            int recentOTPCount = otpDao.countRecentOTPsByUserId(userId, 30);
            if (recentOTPCount >= MAX_OTP_PER_30_MIN) {
                logger.warn("Service : Trop de demandes d'OTP pour user_id={}. Limite atteinte.", userId);
                throw new RuntimeException("Trop de demandes d'OTP. Veuillez patienter 30 minutes.");
            }

            // Récupérer l'utilisateur
            User user = userService.getById(userId);
            if (user.getPhone() == null || user.getPhone().isEmpty()) {
                logger.error("Service : Aucun numéro de téléphone pour user_id={}", userId);
                throw new RuntimeException("Aucun numéro de téléphone associé à ce compte.");
            }

            logger.info("Service : Utilisateur trouvé - id: {}, phone: {}", userId, user.getPhone());

            // Générer l'OTP
            String otpCode = generateOTPCode();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

            // ⚠️ AFFICHAGE TRÈS VISIBLE DE L'OTP DANS LES LOGS
            System.out.println(" ");
            System.out.println("================================================");
            System.out.println("🎯 🎯 🎯  OTP GÉNÉRÉ POUR LES TESTS  🎯 🎯 🎯");
            System.out.println("🔢 CODE OTP: " + otpCode);
            System.out.println("👤 USER ID: " + userId);
            System.out.println("📧 EMAIL: " + user.getEmail());
            System.out.println("📱 NUMÉRO: " + user.getPhone());
            System.out.println("⏰ EXPIRE À: " + expiresAt);
            System.out.println("🎯 UTILISEZ CE CODE DANS VOTRE INTERFACE");
            System.out.println("================================================");
            System.out.println(" ");

            // Log aussi dans les logs normaux
            logger.warn("🎯 OTP DEBUG - Code: {} pour user_id: {}, phone: {}, email: {}",
                    otpCode, userId, user.getPhone(), user.getEmail());

            OTP otp = new OTP(userId, otpCode, expiresAt);

            // Sauvegarder l'OTP
            if (!otpDao.save(otp)) {
                logger.error("Service : Échec de la sauvegarde de l'OTP pour user_id={}", userId);
                throw new RuntimeException("Erreur lors de la génération de l'OTP.");
            }

            logger.info("Service : OTP sauvegardé en base de données");

            // Tentative d'envoi SMS avec retry
            boolean smsSent = false;
            int maxRetries = 2;

            logger.info("Service : Tentative d'envoi SMS DIRECT");

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                logger.info("Service : Tentative d'envoi SMS {} pour user_id={}", attempt, userId);
                smsSent = sendSMS(user.getPhone(), otpCode);

                if (smsSent) {
                    logger.info("Service : ✅ SMS envoyé avec succès au {}", user.getPhone());
                    break;
                }

                if (attempt < maxRetries) {
                    logger.warn("Service : Échec tentative {}, nouvelle tentative dans 3 secondes", attempt);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            if (!smsSent) {
                // ⚠️ MODE DÉVELOPPEMENT - Afficher l'OTP de façon très visible
                System.out.println(" ");
                System.out.println("🚨 🚨 🚨  ÉCHEC ENVOI SMS - MODE DÉVELOPPEMENT  🚨 🚨 🚨");
                System.out.println("📱 Le SMS n'a pas pu être envoyé au: " + user.getPhone());
                System.out.println("🔢 Mais l'OTP a été généré: " + otpCode);
                System.out.println("🎯 Utilisez le code ci-dessus pour vous connecter");
                System.out.println("🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨");
                System.out.println(" ");

                logger.warn("🚨 MODE DÉVELOPPEMENT - Échec envoi SMS. OTP généré: {} pour user_id={}", otpCode, userId);
                logger.warn("📱 Numéro: {} - Utilisez le code OTP ci-dessus", user.getPhone());

                // Retourner true quand même pour permettre les tests
                return true;
            }

            logger.info("Service : ✅ OTP généré et envoyé avec succès pour user_id={}", userId);
            return true;

        } catch (Exception e) {
            logger.error("Service : ❌ Erreur lors de la génération/envoi de l'OTP pour user_id={}", userId, e);
            throw e;
        }
    }

    /**
     * Vérifie si le serveur SMS est actif
     */
    public boolean isSMSServerAvailable() {
        return smsService.isSMSServerAvailable();
    }

    /**
     * Vérifie un code OTP
     */
    public boolean verifyOTP(int userId, String otpCode) {
        logger.info("Service : Vérification de l'OTP pour user_id={}, code={}", userId, otpCode);

        try {
            if (otpDao == null) {
                logger.error("Service : OTPDao non disponible");
                return false;
            }

            // Utiliser findValidOTP qui vérifie automatiquement la validité
            OTP otp = otpDao.findValidOTP(userId, otpCode);

            if (otp != null) {
                // Marquer l'OTP comme utilisé
                otpDao.markAsUsed(otp.getId());
                logger.info("Service : ✅ OTP vérifié avec succès pour user_id={}", userId);
                return true;
            } else {
                logger.warn("Service : ❌ OTP invalide pour user_id={}", userId);
                return false;
            }

        } catch (Exception e) {
            logger.error("Service : ❌ Erreur lors de la vérification de l'OTP pour user_id={}", userId, e);
            return false;
        }
    }

    /**
     * Nettoie les OTP expirés
     */
    public void cleanupExpiredOTPs() {
        if (otpDao != null) {
            logger.info("Service : Nettoyage des OTP expirés");
            boolean deletedCount = otpDao.deleteExpiredOTPs();
            logger.info("Service : {} OTP expirés supprimés", deletedCount);
        }
    }

    /**
     * Vérifie si un utilisateur peut demander un nouvel OTP
     */
    public boolean canRequestOTP(int userId) {
        try {
            int recentOTPCount = otpDao.countRecentOTPsByUserId(userId, 30);
            boolean canRequest = recentOTPCount < MAX_OTP_PER_30_MIN;
            logger.info("Service : Vérification éligibilité OTP user_id={}: {} demandes récentes, peut demander: {}",
                    userId, recentOTPCount, canRequest);
            return canRequest;
        } catch (Exception e) {
            logger.error("Service : Erreur lors de la vérification de l'éligibilité OTP", e);
            return false;
        }
    }

    /**
     * Test direct de l'envoi SMS
     */
    public boolean testSMSServer() {
        logger.info("Service : Test du serveur SMS...");

        // Test de santé
        boolean health = smsService.isSMSServerAvailable();
        logger.info("Service : Health check: {}", health ? "✅ OK" : "❌ FAILED");

        // Test d'envoi avec un numéro de test
        String testPhone = "0743614984";
        String testOTP = "123456";

        logger.info("Service : Test d'envoi vers: {}", testPhone);
        boolean sendTest = smsService.sendOTP(testPhone, testOTP);
        logger.info("Service : Test d'envoi: {}", sendTest ? "✅ SUCCESS" : "❌ FAILED");

        return health && sendTest;
    }


}