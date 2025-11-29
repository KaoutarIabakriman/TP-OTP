import { useState, useEffect } from "react";

interface User {
    id?: number;
    name: string;
    email: string;
    password: string;
    phone: string;
}

interface LoginForm {
    email: string;
    password: string;
}

interface OTPVerificationProps {
    email: string;
    userId: number;
    onSuccess: (user: User) => void;
    onBack: () => void;
}

function OTPVerification({ email, userId, onSuccess, onBack }: OTPVerificationProps) {
    const [otp, setOtp] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async () => {
        setError('');
        setLoading(true);

        try {
            const response = await fetch('http://localhost:8082/api/auth/verify-otp', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId,
                    otpCode: otp
                }),
            });

            const data = await response.json();

            if (response.ok) {
                onSuccess(data.user);
            } else {
                setError(data.message || 'Code OTP invalide');
            }
        } catch (err) {
            setError('Erreur de connexion au serveur');
        } finally {
            setLoading(false);
        }
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter' && otp.length === 6) {
            handleSubmit();
        }
    };

    return (
        <div style={{
            minHeight: "100vh",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: "#f3f4f6",
            padding: "20px"
        }}>
            <div style={{
                backgroundColor: "white",
                padding: "40px",
                borderRadius: "8px",
                boxShadow: "0 2px 10px rgba(0,0,0,0.1)",
                maxWidth: "450px",
                width: "100%"
            }}>
                <div style={{ textAlign: "center", marginBottom: "32px" }}>
                    <h2 style={{ margin: "0 0 16px 0", color: "#111827", fontSize: "24px" }}>
                        Vérification OTP
                    </h2>
                    <p style={{ margin: "0 0 8px 0", color: "#6b7280" }}>
                        Un code a été envoyé à votre numéro de téléphone
                    </p>
                    <p style={{ margin: 0, color: "#6b7280" }}>
                        Email: <span style={{ fontWeight: "600", color: "#111827" }}>{email}</span>
                    </p>
                </div>

                <div style={{ marginBottom: "24px" }}>
                    <label style={{ display: "block", marginBottom: "8px", color: "#374151", fontWeight: "500" }}>
                        Code OTP (6 chiffres)
                    </label>
                    <input
                        type="text"
                        value={otp}
                        onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                        onKeyPress={handleKeyPress}
                        placeholder="123456"
                        maxLength={6}
                        style={{
                            width: "100%",
                            padding: "12px 16px",
                            border: "2px solid #e5e7eb",
                            borderRadius: "6px",
                            textAlign: "center",
                            fontSize: "24px",
                            letterSpacing: "8px",
                            fontFamily: "monospace",
                            outline: "none",
                            transition: "border-color 0.2s",
                            boxSizing: "border-box"
                        }}
                        onFocus={(e) => e.target.style.borderColor = "#4F46E5"}
                        onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
                    />
                </div>

                {error && (
                    <div style={{
                        color: "#DC2626",
                        backgroundColor: "#FEE2E2",
                        padding: "12px 16px",
                        borderRadius: "6px",
                        marginBottom: "24px",
                        display: "flex",
                        alignItems: "center"
                    }}>
                        <span style={{ marginRight: "8px" }}>❌</span>
                        {error}
                    </div>
                )}

                <button
                    onClick={handleSubmit}
                    disabled={loading || otp.length !== 6}
                    style={{
                        width: "100%",
                        backgroundColor: (loading || otp.length !== 6) ? "#D1D5DB" : "#4F46E5",
                        color: "white",
                        padding: "12px",
                        fontWeight: "600",
                        border: "none",
                        borderRadius: "6px",
                        cursor: (loading || otp.length !== 6) ? "not-allowed" : "pointer",
                        marginBottom: "16px",
                        fontSize: "16px",
                        transition: "background-color 0.2s"
                    }}
                    onMouseOver={(e) => {
                        if (!(loading || otp.length !== 6)) {
                            e.currentTarget.style.backgroundColor = "#4338CA";
                        }
                    }}
                    onMouseOut={(e) => {
                        if (!(loading || otp.length !== 6)) {
                            e.currentTarget.style.backgroundColor = "#4F46E5";
                        }
                    }}
                >
                    {loading ? "Vérification..." : "Vérifier le code"}
                </button>

                <button
                    onClick={onBack}
                    style={{
                        width: "100%",
                        color: "#4F46E5",
                        padding: "8px",
                        border: "none",
                        background: "none",
                        cursor: "pointer",
                        fontWeight: "500",
                        fontSize: "14px"
                    }}
                >
                    ← Retour à la connexion
                </button>
            </div>
        </div>
    );
}

export default function App() {
    const [users, setUsers] = useState<User[]>([]);
    const [form, setForm] = useState<User>({
        name: "",
        email: "",
        password: "",
        phone: "",
    });
    const [loginForm, setLoginForm] = useState<LoginForm>({
        email: "",
        password: "",
    });
    const [editingId, setEditingId] = useState<number | null>(null);
    const [showForm, setShowForm] = useState(false);
    const [showLogin, setShowLogin] = useState(false);
    const [currentUser, setCurrentUser] = useState<User | null>(null);
    const [showOTP, setShowOTP] = useState(false);
    const [userEmail, setUserEmail] = useState('');
    const [userId, setUserId] = useState<number>(0);
    const [loginError, setLoginError] = useState('');

    async function loadUsers() {
        const res = await fetch("http://localhost:8082/users");
        const data = await res.json();
        setUsers(data);
    }

    useEffect(() => {
        loadUsers();
    }, []);

    const handleLogin = async () => {
        setLoginError('');

        try {
            const res = await fetch("http://localhost:8082/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(loginForm),
            });

            const textResponse = await res.text();
            let data;
            try {
                data = JSON.parse(textResponse);
            } catch (parseError) {
                setLoginError('Réponse invalide du serveur');
                return;
            }

            if (res.ok && data.requiresOTP) {
                if (data.userId) {
                    setUserId(data.userId);
                    setUserEmail(data.email || loginForm.email);
                    setShowOTP(true);
                    setShowLogin(false);
                } else {
                    const defaultUserId = 1;
                    setUserId(defaultUserId);
                    setUserEmail(data.email || loginForm.email);
                    setShowOTP(true);
                    setShowLogin(false);
                }
            } else {
                setLoginError(data.message || "Erreur lors de la connexion");
            }
        } catch (error) {
            setLoginError("Erreur de connexion au serveur");
        }
    };

    function handleOTPSuccess(user: User) {
        setCurrentUser(user);
        setShowOTP(false);
        setLoginForm({ email: "", password: "" });
        loadUsers();
        alert(`Bienvenue ${user.name} !`);
    }

    function handleLogout() {
        setCurrentUser(null);
        setUsers([]);
        setShowForm(false);
    }

    async function handleSubmit() {
        if (editingId) {
            await fetch(`http://localhost:8082/users/${editingId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(form),
            });
        } else {
            await fetch("http://localhost:8082/users", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(form),
            });
        }

        setForm({ name: "", email: "", password: "", phone: "" });
        setEditingId(null);
        setShowForm(false);
        loadUsers();
    }

    function handleEdit(u: User) {
        if (!currentUser) {
            alert("Veuillez vous connecter pour modifier un utilisateur");
            return;
        }
        setEditingId(u.id!);
        setForm(u);
        setShowForm(true);
    }

    async function handleDelete(id: number) {
        if (!currentUser) {
            alert("Veuillez vous connecter pour supprimer un utilisateur");
            return;
        }
        if (confirm("Êtes-vous sûr de vouloir supprimer cet utilisateur ?")) {
            await fetch(`http://localhost:8082/users/${id}`, { method: "DELETE" });
            loadUsers();
        }
    }

    const inputStyle = {
        width: "100%",
        padding: "10px 12px",
        border: "2px solid #e5e7eb",
        borderRadius: "6px",
        fontSize: "14px",
        outline: "none",
        transition: "border-color 0.2s",
        boxSizing: "border-box" as const,
        marginBottom: "12px"
    };

    const buttonStyle = {
        padding: "10px 20px",
        border: "none",
        borderRadius: "6px",
        fontSize: "14px",
        fontWeight: "500" as const,
        cursor: "pointer",
        transition: "all 0.2s"
    };

    const primaryButtonStyle = {
        ...buttonStyle,
        backgroundColor: "#4F46E5",
        color: "white"
    };

    const secondaryButtonStyle = {
        ...buttonStyle,
        backgroundColor: "#f3f4f6",
        color: "#374151"
    };
    if (showOTP) {
        return (
            <OTPVerification
                email={userEmail}
                userId={userId}
                onSuccess={handleOTPSuccess}
                onBack={() => {
                    setShowOTP(false);
                    setShowLogin(true);
                }}
            />
        );
    }

    return (
        <div style={{
            minHeight: "100vh",
            backgroundColor: "#f9fafb",
            padding: "20px"
        }}>
            <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
                {/* Header */}
                <div style={{
                    backgroundColor: "white",
                    padding: "20px 30px",
                    borderRadius: "8px",
                    boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                    marginBottom: "20px",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center"
                }}>
                    <h1 style={{ margin: 0, fontSize: "24px", color: "#111827" }}>
                        👥 Gestion des Utilisateurs
                    </h1>
                    {currentUser && (
                        <div style={{ display: "flex", alignItems: "center", gap: "15px" }}>
                            <span style={{ color: "#6b7280" }}>
                                Connecté : <strong style={{ color: "#111827" }}>{currentUser.name}</strong>
                            </span>
                            <button
                                onClick={handleLogout}
                                style={{
                                    ...buttonStyle,
                                    backgroundColor: "#f3f4f6",
                                    color: "#374151"
                                }}
                                onMouseOver={(e) => e.currentTarget.style.backgroundColor = "#e5e7eb"}
                                onMouseOut={(e) => e.currentTarget.style.backgroundColor = "#f3f4f6"}
                            >
                                Déconnexion
                            </button>
                        </div>
                    )}
                </div>

                {/* Login Form */}
                {showLogin && !currentUser && (
                    <div style={{
                        backgroundColor: "white",
                        padding: "30px",
                        borderRadius: "8px",
                        boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                        maxWidth: "400px",
                        margin: "0 auto"
                    }}>
                        <h2 style={{ marginTop: 0, marginBottom: "20px", color: "#111827" }}>
                            🔐 Connexion
                        </h2>

                        <input
                            type="email"
                            placeholder="Email"
                            value={loginForm.email}
                            onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
                            style={inputStyle}
                            onFocus={(e) => e.target.style.borderColor = "#4F46E5"}
                            onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
                        />

                        <input
                            type="password"
                            placeholder="Mot de passe"
                            value={loginForm.password}
                            onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                            style={inputStyle}
                            onFocus={(e) => e.target.style.borderColor = "#4F46E5"}
                            onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
                        />

                        {loginError && (
                            <div style={{
                                color: "#DC2626",
                                backgroundColor: "#FEE2E2",
                                padding: "12px",
                                borderRadius: "6px",
                                marginBottom: "16px",
                                fontSize: "14px"
                            }}>
                                ❌ {loginError}
                            </div>
                        )}

                        <div style={{ display: "flex", gap: "10px" }}>
                            <button
                                onClick={handleLogin}
                                style={{ ...primaryButtonStyle, flex: 1 }}
                                onMouseOver={(e) => e.currentTarget.style.backgroundColor = "#4338CA"}
                                onMouseOut={(e) => e.currentTarget.style.backgroundColor = "#4F46E5"}
                            >
                                Se connecter
                            </button>
                            <button
                                onClick={() => {
                                    setShowLogin(false);
                                    setLoginError('');
                                }}
                                style={secondaryButtonStyle}
                                onMouseOver={(e) => e.currentTarget.style.backgroundColor = "#e5e7eb"}
                                onMouseOut={(e) => e.currentTarget.style.backgroundColor = "#f3f4f6"}
                            >
                                Annuler
                            </button>
                        </div>
                    </div>
                )}

                {/* Main Content */}
                {currentUser ? (
                    <div style={{
                        backgroundColor: "white",
                        padding: "30px",
                        borderRadius: "8px",
                        boxShadow: "0 1px 3px rgba(0,0,0,0.1)"
                    }}>
                        {/* Users Table */}
                        <div style={{ overflowX: "auto", marginBottom: "20px" }}>
                            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                                <thead>
                                <tr style={{ backgroundColor: "#f9fafb", borderBottom: "2px solid #e5e7eb" }}>
                                    <th style={{ padding: "12px", textAlign: "left", color: "#374151", fontWeight: "600" }}>Nom complet</th>
                                    <th style={{ padding: "12px", textAlign: "left", color: "#374151", fontWeight: "600" }}>Email</th>
                                    <th style={{ padding: "12px", textAlign: "left", color: "#374151", fontWeight: "600" }}>Téléphone</th>
                                    <th style={{ padding: "12px", textAlign: "left", color: "#374151", fontWeight: "600" }}>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {users.map((u) => (
                                    <tr key={u.id} style={{ borderBottom: "1px solid #e5e7eb" }}>
                                        <td style={{ padding: "12px", color: "#111827" }}>{u.name}</td>
                                        <td style={{ padding: "12px", color: "#6b7280" }}>{u.email}</td>
                                        <td style={{ padding: "12px", color: "#6b7280" }}>{u.phone || "N/A"}</td>
                                        <td style={{ padding: "12px" }}>
                                            <div style={{ display: "flex", gap: "8px" }}>
                                                <button
                                                    onClick={() => handleEdit(u)}
                                                    style={{
                                                        ...buttonStyle,
                                                        padding: "6px 12px",
                                                        backgroundColor: "#f3f4f6",
                                                        color: "#4F46E5"
                                                    }}
                                                    onMouseOver={(e) => e.currentTarget.style.backgroundColor = "#e5e7eb"}
                                                    onMouseOut={(e) => e.currentTarget.style.backgroundColor = "#f3f4f6"}
                                                >
                                                    Modifier
                                                </button>
                                                <button
                                                    onClick={() => handleDelete(u.id!)}
                                                    style={{
                                                        ...buttonStyle,
                                                        padding: "6px 12px",
                                                        backgroundColor: "#FEE2E2",
                                                        color: "#DC2626"
                                                    }}
                                                    onMouseOver={(e) => e.currentTarget.style.backgroundColor = "#FECACA"}
                                                    onMouseOut={(e) => e.currentTarget.style.backgroundColor = "#FEE2E2"}
                                                >
                                                    Supprimer
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        <button
                            onClick={() => {
                                setEditingId(null);
                                setForm({ name: "", email: "", password: "", phone: "" });
                                setShowForm(true);
                            }}
                            style={primaryButtonStyle}
                            onMouseOver={(e) => e.currentTarget.style.backgroundColor = "#4338CA"}
                            onMouseOut={(e) => e.currentTarget.style.backgroundColor = "#4F46E5"}
                        >
                            ➕ Créer un utilisateur
                        </button>

                        {/* Create/Edit Form */}
                        {showForm && (
                            <div style={{
                                marginTop: "30px",
                                padding: "30px",
                                backgroundColor: "#f9fafb",
                                borderRadius: "8px",
                                border: "2px solid #e5e7eb"
                            }}>
                                <h2 style={{ marginTop: 0, marginBottom: "20px", color: "#111827" }}>
                                    {editingId ? "✏️ Modifier" : "➕ Créer"} un utilisateur
                                </h2>

                                <input
                                    placeholder="Nom complet"
                                    value={form.name}
                                    onChange={(e) => setForm({ ...form, name: e.target.value })}
                                    style={inputStyle}
                                    onFocus={(e) => e.target.style.borderColor = "#4F46E5"}
                                    onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
                                />

                                <input
                                    type="email"
                                    placeholder="Email"
                                    value={form.email}
                                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                                    style={inputStyle}
                                    onFocus={(e) => e.target.style.borderColor = "#4F46E5"}
                                    onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
                                />

                                <input
                                    type="password"
                                    placeholder="Mot de passe"
                                    value={form.password}
                                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                                    style={inputStyle}
                                    onFocus={(e) => e.target.style.borderColor = "#4F46E5"}
                                    onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
                                />

                                <input
                                    type="tel"
                                    placeholder="Téléphone"
                                    value={form.phone}
                                    onChange={(e) => setForm({ ...form, phone: e.target.value })}
                                    style={inputStyle}
                                    onFocus={(e) => e.target.style.borderColor = "#4F46E5"}
                                    onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
                                />

                                <div style={{ display: "flex", gap: "10px", marginTop: "16px" }}>
                                    <button
                                        onClick={handleSubmit}
                                        style={{ ...primaryButtonStyle, flex: 1 }}
                                        onMouseOver={(e) => e.currentTarget.style.backgroundColor = "#4338CA"}
                                        onMouseOut={(e) => e.currentTarget.style.backgroundColor = "#4F46E5"}
                                    >
                                        {editingId ? "💾 Enregistrer" : "➕ Créer"}
                                    </button>
                                    <button
                                        onClick={() => setShowForm(false)}
                                        style={secondaryButtonStyle}
                                        onMouseOver={(e) => e.currentTarget.style.backgroundColor = "#e5e7eb"}
                                        onMouseOut={(e) => e.currentTarget.style.backgroundColor = "#f3f4f6"}
                                    >
                                        Annuler
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                ) : (
                    !showLogin && (
                        <div style={{
                            textAlign: "center",
                            padding: "60px 40px",
                            backgroundColor: "white",
                            borderRadius: "8px",
                            boxShadow: "0 1px 3px rgba(0,0,0,0.1)"
                        }}>
                            <div style={{ fontSize: "48px", marginBottom: "20px" }}>🔒</div>
                            <h3 style={{ margin: "0 0 10px 0", color: "#111827", fontSize: "20px" }}>
                                Accès restreint
                            </h3>
                            <p style={{ margin: "0 0 20px 0", color: "#6b7280" }}>
                                Veuillez vous connecter pour accéder à la gestion des utilisateurs.
                            </p>
                            <button
                                onClick={() => setShowLogin(true)}
                                style={primaryButtonStyle}
                                onMouseOver={(e) => e.currentTarget.style.backgroundColor = "#4338CA"}
                                onMouseOut={(e) => e.currentTarget.style.backgroundColor = "#4F46E5"}
                            >
                                Se connecter
                            </button>
                        </div>
                    )
                )}
            </div>
        </div>
    );
}