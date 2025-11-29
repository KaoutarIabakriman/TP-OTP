import { useState } from "react";
import { requestOTP } from "./authApi";

export default function Login({ onLoginSuccess }) {
    const [email, setEmail] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [showOTPInput, setShowOTPInput] = useState(false);

    const handleRequestOTP = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage("");

        try {
            const result = await requestOTP(email);

            if (result.status === "success") {
                setMessage("✅ Code OTP envoyé par SMS");
                setShowOTPInput(true);
            } else {
                setMessage(`❌ ${result.message}`);
            }
        } catch (error) {
            setMessage("❌ Erreur de connexion au serveur");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: 400, margin: "50px auto", padding: 20 }}>
            <h2>Connexion sécurisée</h2>

            <form onSubmit={handleRequestOTP}>
                <div style={{ marginBottom: 15 }}>
                    <label>Email:</label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        style={{ width: "100%", padding: 8, marginTop: 5 }}
                        placeholder="votre@email.com"
                    />
                </div>

                {!showOTPInput && (
                    <button
                        type="submit"
                        disabled={loading}
                        style={{ width: "100%", padding: 10, backgroundColor: loading ? "#ccc" : "#007bff", color: "white", border: "none" }}
                    >
                        {loading ? "Envoi en cours..." : "Recevoir le code OTP"}
                    </button>
                )}
            </form>

            {message && (
                <div style={{
                    marginTop: 15,
                    padding: 10,
                    backgroundColor: message.includes("❌") ? "#f8d7da" : "#d4edda",
                }}>
                    {message}
                </div>
            )}

            {showOTPInput && (
                <OTPVerification
                    email={email}
                    onSuccess={onLoginSuccess}
                    onBack={() => {
                        setShowOTPInput(false);
                        setMessage("");
                    }}
                />
            )}
        </div>
    );
}