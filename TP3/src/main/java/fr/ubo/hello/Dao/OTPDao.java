package fr.ubo.hello.Dao;

import fr.ubo.hello.Model.OTP;
import org.springframework.stereotype.Repository;

@Repository
public interface OTPDao {
    boolean save(OTP otp);
    OTP findValidOTP(int userId, String otpCode);
    void markAsUsed(int otpId);
    int countRecentOTPsByUserId(int userId, int minutes);  // Ajouté
    boolean deleteExpiredOTPs();  // Ajouté (pour compatibilité)
}