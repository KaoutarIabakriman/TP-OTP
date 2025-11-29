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

// Composant OTP Verification (inchangé)
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
            background: "linear-gradient(to bottom right, #EFF6FF, #E0E7FF)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: "20px"
        }}>
            <div style={{
                backgroundColor: "white",
                borderRadius: "16px",
                boxShadow: "0 10px 25px rgba(0,0,0,0.1)",
                padding: "40px",
                maxWidth: "450px",
                width: "100%"
            }}>
                <div style={{ textAlign: "center", marginBottom: "32px" }}>
                    <div style={{
                        backgroundColor: "#E0E7FF",
                        width: "64px",
                        height: "64px",
                        borderRadius: "50%",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        margin: "0 auto 16px"
                    }}>
                        <span style={{ fontSize: "32px" }}>🔐</span>
                    </div>
                    <h2 style={{ fontSize: "28px", fontWeight: "bold", color: "#1F2937", marginBottom: "8px" }}>
                        Vérification OTP
                    </h2>
                    <p style={{ color: "#6B7280", marginBottom: "8px" }}>
                        Un code a été envoyé à votre numéro de téléphone
                    </p>
                    <p style={{ fontSize: "14px", color: "#9CA3AF" }}>
                        Email: <span style={{ fontWeight: "600" }}>{email}</span>
                    </p>
                </div>

                <div style={{ marginBottom: "24px" }}>
                    <label style={{ display: "block", fontSize: "14px", fontWeight: "500", color: "#374151", marginBottom: "8px" }}>
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
                            border: "1px solid #D1D5DB",
                            borderRadius: "8px",
                            textAlign: "center",
                            fontSize: "24px",
                            letterSpacing: "8px",
                            fontFamily: "monospace",
                            outline: "none",
                            boxSizing: "border-box"
                        }}
                    />
                </div>

                {error && (
                    <div style={{
                        backgroundColor: "#FEF2F2",
                        border: "1px solid #FCA5A5",
                        color: "#DC2626",
                        padding: "12px 16px",
                        borderRadius: "8px",
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
                        borderRadius: "8px",
                        fontWeight: "600",
                        border: "none",
                        cursor: (loading || otp.length !== 6) ? "not-allowed" : "pointer",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        marginBottom: "16px"
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
                        fontWeight: "500"
                    }}
                >
                    Retour à la connexion
                </button>

                <div style={{
                    marginTop: "24px",
                    padding: "16px",
                    backgroundColor: "#EFF6FF",
                    borderRadius: "8px"
                }}>
                    <p style={{ fontSize: "14px", color: "#1E40AF", margin: 0 }}>
                        💡 <strong>Information:</strong> Le code OTP expire dans 2 minutes. Délai de 30 secondes entre chaque demande.
                    </p>
                </div>
            </div>
        </div>
    );
}

// Composant principal App
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

    // États pour OTP
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
        // Charger les utilisateurs au démarrage (pour le débogage)
        loadUsers();
    }, []);

    const handleLogin = async () => {
        setLoginError('');

        console.log('🔐 Tentative de connexion avec:', loginForm);

        try {
            const res = await fetch("http://localhost:8082/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(loginForm),
            });

            console.log('📡 Status HTTP:', res.status);
            console.log('📡 OK:', res.ok);

            const textResponse = await res.text();
            console.log('📩 Réponse BRUTE:', textResponse);

            let data;
            try {
                data = JSON.parse(textResponse);
                console.log('📩 Réponse PARSÉE:', data);
            } catch (parseError) {
                console.error('❌ Erreur parsing JSON:', parseError);
                setLoginError('Réponse invalide du serveur');
                return;
            }

            console.log('🔍 Clés dans la réponse:', Object.keys(data));
            console.log('🔍 userId présent?:', 'userId' in data);
            console.log('🔍 userId valeur:', data.userId);

            if (res.ok && data.requiresOTP) {
                console.log('✅ OTP requis, affichage de l\'écran OTP');

                if (data.userId) {
                    setUserId(data.userId);
                    setUserEmail(data.email || loginForm.email);
                    setShowOTP(true);
                    setShowLogin(false);
                    console.log('✅ UserId défini:', data.userId);
                } else {
                    console.error('❌ userId MANQUANT dans la réponse. Toutes les clés:', Object.keys(data));

                    const defaultUserId = 1;
                    console.log('⚠️ Utilisation userId par défaut:', defaultUserId);
                    setUserId(defaultUserId);
                    setUserEmail(data.email || loginForm.email);
                    setShowOTP(true);
                    setShowLogin(false);
                }
            } else {
                console.log('❌ Erreur:', data.message);
                setLoginError(data.message || "Erreur lors de la connexion");
            }
        } catch (error) {
            console.error('❌ Erreur réseau:', error);
            setLoginError("Erreur de connexion au serveur");
        }
    };

    function handleOTPSuccess(user: User) {
        console.log('✅ Connexion réussie avec OTP pour:', user);
        setCurrentUser(user);
        setShowOTP(false);
        setLoginForm({ email: "", password: "" });
        // Recharger les utilisateurs après connexion réussie
        loadUsers();
        alert(`Bienvenue ${user.name} !`);
    }

    function handleLogout() {
        setCurrentUser(null);
        setUsers([]); // Vider la liste des utilisateurs lors de la déconnexion
        setShowForm(false); // Fermer le formulaire si ouvert
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
        // Vérifier si l'utilisateur est connecté avant d'autoriser l'édition
        if (!currentUser) {
            alert("Veuillez vous connecter pour modifier un utilisateur");
            return;
        }
        setEditingId(u.id!);
        setForm(u);
        setShowForm(true);
    }

    async function handleDelete(id: number) {
        // Vérifier si l'utilisateur est connecté avant d'autoriser la suppression
        if (!currentUser) {
            alert("Veuillez vous connecter pour supprimer un utilisateur");
            return;
        }
        await fetch(`http://localhost:8082/users/${id}`, { method: "DELETE" });
        loadUsers();
    }

    // Si on est en mode vérification OTP, afficher uniquement le composant OTP
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
        <div style={{ padding: 20 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>

                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
                    <h1>Gestion des utilisateurs</h1>

                    {/* BOUTON LOGIN/LOGOUT - Afficher seulement si connecté */}
                    <div>
                        {currentUser && (
                            <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                                <span>Connecté en tant que : <strong>{currentUser.name}</strong></span>
                                <button onClick={handleLogout}>Déconnexion</button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* FORMULAIRE DE LOGIN */}
            {showLogin && !currentUser && (
                <div style={{ border: "1px solid #ccc", padding: 20, marginBottom: 20, borderRadius: 8 }}>
                    <h2>Connexion</h2>
                    <div>
                        <input
                            type="email"
                            placeholder="Email"
                            value={loginForm.email}
                            onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
                            required
                            style={{ marginBottom: 10, padding: 8, width: 200 }}
                        />
                        <br />
                        <input
                            type="password"
                            placeholder="Mot de passe"
                            value={loginForm.password}
                            onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                            required
                            style={{ marginBottom: 10, padding: 8, width: 200 }}
                        />
                        <br />

                        {loginError && (
                            <div style={{
                                color: 'red',
                                marginBottom: 10,
                                padding: 8,
                                backgroundColor: '#fee',
                                borderRadius: 4
                            }}>
                                ❌ {loginError}
                            </div>
                        )}

                        <button
                            onClick={handleLogin}
                            style={{ marginRight: 10 }}
                        >
                            Se connecter
                        </button>
                        <button
                            onClick={() => {
                                setShowLogin(false);
                                setLoginError('');
                            }}
                        >
                            Annuler
                        </button>
                    </div>
                </div>
            )}

            {/* AFFICHAGE CONDITIONNEL DU CONTENU */}
            {currentUser ? (
                // CONTENU QUAND L'UTILISATEUR EST CONNECTÉ
                <>
                    {/* LISTE DES UTILISATEURS */}
                    <table style={{ marginBottom: 20, width: "100%", borderCollapse: "collapse" }}>
                        <thead>
                        <tr style={{ borderBottom: "2px solid #ccc" }}>
                            <th style={{ padding: 8, textAlign: "left" }}>Nom complet</th>
                            <th style={{ padding: 8, textAlign: "left" }}>Email</th>
                            <th style={{ padding: 8, textAlign: "left" }}>Téléphone</th>
                            <th style={{ padding: 8, textAlign: "left" }}>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {users.map((u) => (
                            <tr key={u.id} style={{ borderBottom: "1px solid #eee" }}>
                                <td style={{ padding: 8 }}>{u.name}</td>
                                <td style={{ padding: 8 }}>{u.email}</td>
                                <td style={{ padding: 8 }}>{u.phone || "N/A"}</td>
                                <td style={{ padding: 8 }}>
                                    <button onClick={() => handleEdit(u)} style={{ marginRight: 5 }}>Modifier</button>
                                    <button onClick={() => handleDelete(u.id!)}>Supprimer</button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    {/* BOUTON CRÉATION */}
                    <button
                        style={{ padding: "8px 16px", marginBottom: 20 }}
                        onClick={() => {
                            if (!currentUser) {
                                alert("Veuillez vous connecter pour créer un utilisateur");
                                return;
                            }
                            setEditingId(null);
                            setForm({ name: "", email: "", password: "", phone: "" });
                            setShowForm(true);
                        }}
                    >
                        ➕ Créer un utilisateur
                    </button>

                    {/* FORMULAIRE UTILISATEUR */}
                    {showForm && (
                        <div style={{ border: "1px solid #ccc", padding: 20, borderRadius: 8 }}>
                            <h2>{editingId ? "Modifier" : "Créer"} un utilisateur</h2>
                            <div>
                                <input
                                    placeholder="Nom complet"
                                    value={form.name}
                                    onChange={(e) => setForm({ ...form, name: e.target.value })}
                                    required
                                    style={{ marginBottom: 10, padding: 8, width: 200 }}
                                />
                                <br />
                                <input
                                    type="email"
                                    placeholder="Email"
                                    value={form.email}
                                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                                    required
                                    style={{ marginBottom: 10, padding: 8, width: 200 }}
                                />
                                <br />
                                <input
                                    type="password"
                                    placeholder="Mot de passe"
                                    value={form.password}
                                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                                    required
                                    style={{ marginBottom: 10, padding: 8, width: 200 }}
                                />
                                <br />
                                <input
                                    type="tel"
                                    placeholder="Téléphone (ex: 0612345678)"
                                    value={form.phone}
                                    onChange={(e) => setForm({ ...form, phone: e.target.value })}
                                    required
                                    style={{ marginBottom: 10, padding: 8, width: 200 }}
                                />
                                <br />
                                <button
                                    onClick={handleSubmit}
                                    style={{ marginRight: 10 }}
                                >
                                    {editingId ? "Enregistrer" : "Créer"}
                                </button>
                                <button onClick={() => setShowForm(false)}>
                                    Annuler
                                </button>
                            </div>
                        </div>
                    )}
                </>
            ) : (
                // CONTENU QUAND L'UTILISATEUR N'EST PAS CONNECTÉ
                !showLogin && (
                    <div style={{
                        textAlign: "center",
                        padding: "40px",
                        backgroundColor: "#f5f5f5",
                        borderRadius: 8,
                        marginTop: 20
                    }}>
                        <h3>🔒 Accès restreint</h3>
                        <p>Veuillez vous connecter pour accéder à la gestion des utilisateurs.</p>
                        <button
                            onClick={() => setShowLogin(true)}
                            style={{ marginTop: 10, padding: "8px 16px" }}
                        >
                            Se connecter
                        </button>
                    </div>
                )
            )}
        </div>
    );
}