package fr.ubo.hello.Dao;

import fr.ubo.hello.Model.OTP;
import fr.ubo.hello.utils.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository("mysqlOTPDao")
public class OTPDao_BD implements OTPDao {

    private static final Logger logger = LoggerFactory.getLogger(OTPDao_BD.class);

    @Override
    public boolean save(OTP otp) {
        String sql = "INSERT INTO otp_codes (user_id, otp, expires_at) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, otp.getUserId());
            stmt.setString(2, otp.getOtpCode());
            stmt.setTimestamp(3, Timestamp.valueOf(otp.getExpiresAt()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        otp.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            logger.error("DAO OTP : Erreur lors de la sauvegarde de l'OTP", e);
        }
        return false;
    }


    @Override
    public OTP findValidOTP(int userId, String otpCode) {
        String sql = "SELECT * FROM otp_codes " +
                "WHERE user_id = ? AND otp = ? AND is_valid = 1 AND expires_at > NOW()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, otpCode);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractOTP(rs);
            }

        } catch (SQLException e) {
            logger.error("DAO OTP : Erreur lors de la recherche de l'OTP valide", e);
        }

        return null;
    }




    @Override
    public void markAsUsed(int otpId) {
        String sql = "UPDATE otp_codes SET is_valid = 0 WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, otpId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("DAO OTP : Erreur lors du marquage de l'OTP comme utilisé", e);
        }
    }





    @Override
    public boolean deleteExpiredOTPs() {
        String sql = "DELETE FROM otp_codes WHERE expires_at < NOW() OR is_used = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int deleted = stmt.executeUpdate();
            logger.info("DAO OTP : {} OTPs expirés supprimés", deleted);
            return true;

        } catch (SQLException e) {
            logger.error("DAO OTP : Erreur lors du nettoyage des OTPs expirés", e);
            return false;
        }
    }

    @Override
    public int countRecentOTPsByUserId(int userId, int minutes) {
        String sql = "SELECT COUNT(*) FROM otp_codes WHERE user_id = ? AND created_at > DATE_SUB(NOW(), INTERVAL ? MINUTE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, minutes);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                logger.debug("DAO OTP : {} OTPs récents pour l'utilisateur {}", count, userId);
                return count;
            }

        } catch (SQLException e) {
            logger.error("DAO OTP : Erreur lors du comptage des OTPs récents", e);
        }
        return 0;
    }

    private OTP extractOTP(ResultSet rs) throws SQLException {
        OTP otp = new OTP();
        otp.setId(rs.getInt("id"));
        otp.setUserId(rs.getInt("user_id"));
        otp.setOtpCode(rs.getString("otp")); // <-- correct

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            otp.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            otp.setExpiresAt(expiresAt.toLocalDateTime());
        }

        otp.setUsed(rs.getBoolean("is_valid") == false); // false = used/invalid

        return otp;
    }

}