package fr.ubo.hello.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Lire les variables d'environnement avec valeurs par défaut
    private static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "mysql-djf");
    private static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "3306");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "djfdb");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "kaoutar");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "kaoutar");

    private static final String DB_URL = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            DB_HOST, DB_PORT, DB_NAME);

    // Configuration pour les tentatives de reconnexion
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 3000;

    public static Connection getConnection() throws SQLException {
        return getConnectionWithRetry();
    }

    /**
     * Tente de se connecter à la base avec plusieurs retries
     * Important pour le démarrage : MySQL peut prendre du temps à être prêt
     */
    private static Connection getConnectionWithRetry() throws SQLException {
        SQLException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Charger le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Tenter la connexion
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                if (attempt > 1) {
                    System.out.println("✅ Connexion MySQL réussie après " + attempt + " tentative(s)");
                } else {
                    System.out.println("✅ Connexion MySQL réussie");
                }
                System.out.println("📊 URL: " + DB_URL);
                System.out.println("👤 User: " + DB_USER);

                return connection;

            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MySQL non trouvé");
                throw new SQLException("Driver MySQL non trouvé", e);

            } catch (SQLException e) {
                lastException = e;

                if (attempt < MAX_RETRIES) {
                    System.err.println("⚠️  Tentative " + attempt + "/" + MAX_RETRIES + " échouée: " + e.getMessage());
                    System.out.println("⏳ Nouvelle tentative dans " + (RETRY_DELAY_MS/1000) + " secondes...");

                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interruption lors de la reconnexion", ie);
                    }
                } else {
                    System.err.println("❌ Échec de connexion MySQL après " + MAX_RETRIES + " tentatives");
                    System.err.println("💡 Vérifiez que:");
                    System.err.println("   - Le conteneur MySQL est démarré: docker ps");
                    System.err.println("   - Les credentials sont corrects");
                    System.err.println("   - Le nom du service est 'mysql' dans docker-compose.yml");
                }
            }
        }

        throw new SQLException("Impossible de se connecter à MySQL après " + MAX_RETRIES + " tentatives", lastException);
    }


}