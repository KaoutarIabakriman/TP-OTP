// authApi.js
const AUTH_API_URL = "http://localhost:8082/auth";

export async function requestOTP(email) {
  const response = await fetch(`${AUTH_API_URL}/request-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });
  return response.json();
}

export async function verifyOTP(email, otpCode) {
  const response = await fetch(`${AUTH_API_URL}/verify-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, otpCode }),
  });
  return response.json();
}