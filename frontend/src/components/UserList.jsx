export default function UserList({ onEdit }) {
  const [users, setUsers] = useState([]);

  const load = async () => {
    setUsers(await getUsers());
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <div>
      <h2>Liste des utilisateurs</h2>

      <table border="1" cellPadding="8">
        <thead>
          <tr>
            <th>Nom complet</th>
            <th>Email</th>
            <th>Actions</th>
          </tr>
        </thead>

        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.name}</td>
              <td>{u.email}</td>
              <td>
                <button onClick={() => onEdit(u)}>Modifier</button>
                <button onClick={() => deleteUser(u.id).then(load)}>Supprimer</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* BOUTON + */}
      <button 
        style={{ marginTop: 15, padding: "8px 12px" }} 
        onClick={() => onEdit(null)}
      >
        ➕ Créer un utilisateur
      </button>
    </div>
  );
}
