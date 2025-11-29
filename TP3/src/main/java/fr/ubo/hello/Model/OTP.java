// OTP.java
package fr.ubo.hello.Model;

import java.time.LocalDateTime;

public class OTP {
    private int id;
    private int userId;
    private String otpCode;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isUsed;

    public OTP() {}

    public OTP(int userId, String otpCode, LocalDateTime expiresAt) {
        this.userId = userId;
        this.otpCode = otpCode;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.isUsed = false;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
}