const API_URL = "http://localhost:8082/users";
const AUTH_URL = "http://localhost:8082/auth"; // 👈 URL corrigée

export async function getUsers() {
  const res = await fetch(API_URL);
  return res.json();
}

export async function createUser(user) {
  return fetch(API_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(user),
  });
}

export async function updateUser(id, user) {
  return fetch(`${API_URL}/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(user),
  });
}

export async function deleteUser(id) {
  return fetch(`${API_URL}/${id}`, { method: "DELETE" });
}

// 👇 CORRIGÉ : Utilise maintenant /auth/login
export async function login(credentials) {
  const res = await fetch(`${AUTH_URL}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  });
  
  if (!res.ok) {
    throw new Error("Login failed");
  }
  
  return res.json();
}

// 👇 NOUVEAU : Vérifier si un email existe
export async function checkEmail(email) {
  const res = await fetch(`${AUTH_URL}/check-email?email=${encodeURIComponent(email)}`);
  return res.json();
}