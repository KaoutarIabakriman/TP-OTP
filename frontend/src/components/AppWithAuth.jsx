// AppWithAuth.jsx
import { useState } from "react";
import Login from "./Login";
import App from "./App";

export default function AppWithAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  if (!isAuthenticated) {
    return <Login onLoginSuccess={() => setIsAuthenticated(true)} />;
  }

  return (
    <div>
      <div style={{ padding: 10, backgroundColor: "#f8f9fa", borderBottom: "1px solid #dee2e6" }}>
        <button 
          onClick={() => setIsAuthenticated(false)}
          style={{ padding: "5px 10px", float: "right" }}
        >
          Déconnexion
        </button>
        <h3 style={{ margin: 0 }}>Application sécurisée</h3>
      </div>
      <App />
    </div>
  );
}