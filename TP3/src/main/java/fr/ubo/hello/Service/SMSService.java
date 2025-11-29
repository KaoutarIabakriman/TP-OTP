package fr.ubo.hello.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class SMSService {

    private static final Logger logger = LoggerFactory.getLogger(SMSService.class);
    private static final String SMS_SERVER_URL = "http://dosipa.univ-brest.fr/send-sms";
    private static final String HEALTH_CHECK_URL = "http://dosipa.univ-brest.fr";
    private static final String API_KEY = "DOSITPDJF";
    private static final int TIMEOUT = 10000;


    public boolean isSMSServerAvailable() {
        try {
            URL url = new URL(HEALTH_CHECK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-API-Key", API_KEY);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            int responseCode = conn.getResponseCode();
            logger.info("Health check - Code: {}", responseCode);

            conn.disconnect();

            boolean available = (responseCode == 200);
            logger.info("SMS Server health check: {}", available ? "OK" : "FAILED");
            return available;

        } catch (Exception e) {
            logger.warn("SMS Server non disponible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Envoie un SMS via le serveur d'envoi
     * @param phoneNumber Numéro de téléphone (format: 0612345678)
     * @param message Message à envoyer
     * @return true si l'envoi a réussi
     */
    public boolean sendSMS(String phoneNumber, String message) {
        logger.info("Tentative d'envoi SMS vers: {}", phoneNumber);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(SMS_SERVER_URL);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("x-api-key", API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            String jsonInputString = String.format(
                    "{\"to\": \"%s\", \"message\": \"%s\"}",
                    phoneNumber,
                    message
            );

            logger.info("Envoi vers: {}", SMS_SERVER_URL);
            logger.info("Payload JSON: {}", jsonInputString);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            logger.info("Code réponse serveur SMS: {}", responseCode);

            if (responseCode == 200 || responseCode == 201) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    logger.info("SMS envoyé avec succès. Réponse: {}", response.toString());
                    return true;
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    logger.error("Erreur serveur SMS ({}): {}", responseCode, errorResponse.toString());
                }
                return false;
            }

        } catch (Exception e) {
            logger.error("Exception lors de l'envoi SMS: {}", e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }    /**
     * Envoie un OTP par SMS
     * @param phoneNumber Numéro de téléphone
     * @param otpCode Code OTP
     * @return true si l'envoi a réussi
     */
    public boolean sendOTP(String phoneNumber, String otpCode) {
        String message = String.format(
                "Votre code de vérification est: %s. Valable 2 minutes.",
                otpCode
        );
        return sendSMS(phoneNumber, message);
    }

    /**
     * Méthode alternative avec différents formats de payload
     */
    public boolean sendSMSAlternative(String phoneNumber, String message) {
        logger.info("Tentative alternative d'envoi SMS vers: {}", phoneNumber);

        try {
            URL url = new URL(SMS_SERVER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("x-api-key", API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            String jsonInputString = String.format(
                    "{\"phoneNumber\": \"%s\", \"text\": \"%s\", \"apiKey\": \"%s\"}",
                    phoneNumber, message, API_KEY
            );

            logger.info("Format alternatif - Payload: {}", jsonInputString);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            logger.info("Code réponse (format alternatif): {}", responseCode);

            if (responseCode == 200 || responseCode == 201) {
                logger.info("SMS envoyé avec format alternatif");
                return true;
            } else {
                logger.error("Échec avec format alternatif: {}", responseCode);
                return false;
            }

        } catch (Exception e) {
            logger.error("Exception avec format alternatif: {}", e.getMessage());
            return false;
        }
    }
}