package fr.ubo.hello.Dao;

import fr.ubo.hello.Model.User;
import fr.ubo.hello.utils.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository("mysqlDAO")
@Primary
public class UserDao_BD implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao_BD.class);

    @Override
    public List<User> findAll() {
        logger.debug("DAO : SELECT * FROM users");
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(extractUser(rs));
            }

            logger.info("DAO : {} utilisateurs récupérés", users.size());

        } catch (SQLException e) {
            logger.error("Erreur DAO findAll(): {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs.", e);
        }

        return users;
    }

    @Override
    public User findById(int id) {
        logger.debug("DAO : SELECT * FROM users WHERE id={}", id);
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = extractUser(rs);
                    logger.info("DAO : Utilisateur id={} trouvé", id);
                    return user;
                } else {
                    logger.warn("DAO : Utilisateur id={} non trouvé", id);
                }
            }

        } catch (SQLException e) {
            logger.error("Erreur DAO findById({}): {}", id, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération de l'utilisateur.", e);
        }

        return null;
    }

    @Override
    public boolean save(User user) {
        logger.debug("DAO : INSERT INTO users(name,email,password,phone) VALUES (?, ?, ?, ?)");
        String sql = "INSERT INTO users(name, email, password, phone) VALUES(?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getPhone());

            boolean success = stmt.executeUpdate() > 0;

            if (success) {
                logger.info("DAO : Utilisateur '{}' créé avec succès", user.getName());
            } else {
                logger.warn("DAO : Échec création utilisateur '{}'", user.getName());
            }

            return success;

        } catch (SQLException e) {
            logger.error("Erreur DAO save({}): {}", user.getName(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'insertion de l'utilisateur.", e);
        }
    }

    @Override
    public boolean update(User user) {
        logger.debug("DAO : UPDATE users WHERE id={}", user.getId());
        String sql = "UPDATE users SET name=?, email=?, password=?, phone=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getPhone());
            stmt.setInt(5, user.getId());

            boolean success = stmt.executeUpdate() > 0;

            if (success) {
                logger.info("DAO : Utilisateur id={} mis à jour", user.getId());
            } else {
                logger.warn("DAO : Échec mise à jour utilisateur id={}", user.getId());
            }

            return success;

        } catch (SQLException e) {
            logger.error("Erreur DAO update(id={}): {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur.", e);
        }
    }

    @Override
    public boolean delete(int id) {
        logger.debug("DAO : DELETE FROM users WHERE id={}", id);
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            boolean success = stmt.executeUpdate() > 0;

            if (success) {
                logger.info("DAO : Utilisateur id={} supprimé", id);
            } else {
                logger.warn("DAO : Échec suppression utilisateur id={}", id);
            }

            return success;

        } catch (SQLException e) {
            logger.error("Erreur DAO delete(id={}): {}", id, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur.", e);
        }
    }

    @Override
    public User findByEmail(String email) {
        logger.debug("DAO : SELECT * FROM users WHERE email={}", email);
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = extractUser(rs);
                    logger.info("DAO : Utilisateur trouvé pour email={}", email);
                    return user;
                }
            }

            logger.warn("DAO : Aucun utilisateur trouvé pour email={}", email);
            return null;

        } catch (SQLException e) {
            logger.error("DAO : Erreur lors de la recherche par email", e);
            return null;
        }
    }

    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        return user;
    }
}