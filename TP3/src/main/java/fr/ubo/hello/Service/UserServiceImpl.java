package fr.ubo.hello.Service;

import fr.ubo.hello.Dao.UserDao;
import fr.ubo.hello.Model.User;
import fr.ubo.hello.Exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of UserService interface.
 * Handles CRUD operations and authentication for users.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    @Qualifier("mysqlDAO")
    private UserDao dao;

    /**
     * Retrieves all users.
     *
     * @return list of users.
     */
    @Override
    public List<User> findAll() {
        logger.info("Service : findAll()");
        return dao.findAll();
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id user ID.
     * @return User object.
     * @throws UserNotFoundException if user not found.
     */
    @Override
    public User getById(int id) {
        User u = dao.findById(id);
        if (u == null) throw new UserNotFoundException(id);
        return u;
    }

    /**
     * Creates a new user.
     *
     * @param u user to create.
     */
    @Override
    public void create(User u) {
        if (!dao.save(u)) throw new RuntimeException("Erreur lors de la création de l'utilisateur.");
    }

    /**
     * Updates an existing user.
     *
     * @param u updated user object.
     * @throws UserNotFoundException if user does not exist.
     */
    @Override
    public void update(User u) {
        if (!dao.update(u)) throw new UserNotFoundException(u.getId());
    }

    /**
     * Deletes a user by ID.
     *
     * @param id user ID.
     * @throws UserNotFoundException if user does not exist.
     */
    @Override
    public void delete(int id) {
        if (!dao.delete(id)) throw new UserNotFoundException(id);
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param email    user email.
     * @param password user password.
     * @return User object if credentials valid, null otherwise.
     */
    @Override
    public User authenticateUser(String email, String password) {
        User user = dao.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) return user;
        return null;
    }

    /**
     * Finds a user by email.
     *
     * @param email email of the user.
     * @return User object or null if not found.
     */
    @Override
    public User findByEmail(String email) {
        return dao.findByEmail(email);
    }
}
