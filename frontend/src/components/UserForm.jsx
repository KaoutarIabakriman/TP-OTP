import { useState } from "react";
import { createUser, updateUser } from "../api";

export default function UserForm({ user, onClose }) {
  const [form, setForm] = useState(
    user || { name: "", email: "", password: "" }
  );

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();

    if (user) {
      await updateUser(user.id, form);
    } else {
      await createUser(form);
    }

    onClose();
  }

  return (
    <div style={{ marginTop: 20 }}>
      <h2>{user ? "Modifier" : "Créer"} un utilisateur</h2>

      <form onSubmit={handleSubmit}>
        <input
          name="name"
          value={form.name}
          onChange={handleChange}
          placeholder="Nom complet"
          required
        /><br />

        <input
          name="email"
          type="email"
          value={form.email}
          onChange={handleChange}
          placeholder="Email"
          required
        /><br />

        <input
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          placeholder="Mot de passe"
          required={!user} // obligatoire seulement en création
        /><br />

        <button type="submit">Valider</button>
        <button type="button" onClick={onClose}>Annuler</button>
      </form>
    </div>
  );
}
