package fr.ubo.hello.Dao;

import fr.ubo.hello.Model.User;
import java.util.List;

public interface UserDao {
    List<User> findAll();
    User findById(int id);
    User findByEmail(String email); // ← Ajouter cette méthode
    boolean save(User user);
    boolean update(User user);
    boolean delete(int id);
}