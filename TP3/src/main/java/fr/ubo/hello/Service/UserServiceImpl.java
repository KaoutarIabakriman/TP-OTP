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

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    @Qualifier("mysqlDAO")
    private UserDao dao;


    @Override
    public List<User> findAll() {
        logger.info("Service : findAll()");
        List<User> users = dao.findAll();
        logger.info("Service : {} utilisateurs récupérés", users.size());
        return users;
    }

    public User getById(int id) {
        logger.info("Service : récupération utilisateur id={}", id);

        try {
            User u = dao.findById(id);

            if (u == null) {
                logger.warn("Service : Utilisateur id={} non trouvé", id);
                throw new UserNotFoundException(id);
            }

            logger.info("Service : Utilisateur id={} récupéré avec succès", id);
            return u;

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Service : erreur lors de la récupération de l'utilisateur id={}", id, e);
            throw e;
        }
    }

    @Override
    public void create(User u) {
        logger.info("Service : création utilisateur '{}'", u.getName());

        try {
            if (!dao.save(u)) {
                logger.error("Service : Échec création utilisateur '{}'", u.getName());
                throw new RuntimeException("Erreur lors de la création de l'utilisateur.");
            }

            logger.info("Service : Utilisateur '{}' créé avec succès", u.getName());

        } catch (Exception e) {
            logger.error("Service : erreur lors de la création de l'utilisateur '{}'", u.getName(), e);
            throw e;
        }
    }

    public void update(User u) {
        logger.info("Service : mise à jour utilisateur id={}", u.getId());

        try {
            boolean updated = dao.update(u);

            if (!updated) {
                logger.warn("Service : Mise à jour impossible - utilisateur id={} non trouvé", u.getId());
                throw new UserNotFoundException(u.getId());
            }

            logger.info("Service : Utilisateur id={} mis à jour avec succès", u.getId());

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Service : erreur lors de la mise à jour de l'utilisateur id={}", u.getId(), e);
            throw e;
        }
    }

    public void delete(int id) {
        logger.info("Service : suppression utilisateur id={}", id);

        try {
            boolean deleted = dao.delete(id);

            if (!deleted) {
                logger.warn("Service : Suppression impossible - utilisateur id={} non trouvé", id);
                throw new UserNotFoundException(id);
            }

            logger.info("Service : Utilisateur id={} supprimé avec succès", id);

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Service : erreur lors de la suppression de l'utilisateur id={}", id, e);
            throw e;
        }
    }



    @Override
    public User authenticateUser(String email, String password) {
        logger.info("Service : authentification pour email={}", email);

        try {
            User user = dao.findByEmail(email);

            if (user == null) {
                logger.warn("Service : Aucun utilisateur trouvé avec l'email={}", email);
                return null;
            }

            // Vérification du mot de passe (en clair pour l'instant)
            if (user.getPassword().equals(password)) {
                logger.info("Service : Authentification réussie pour user_id={}", user.getId());
                return user;
            } else {
                logger.warn("Service : Mot de passe incorrect pour email={}", email);
                return null;
            }

        } catch (Exception e) {
            logger.error("Service : erreur lors de l'authentification", e);
            throw e;
        }
    }

    @Override
    public User findByEmail(String email) {
        logger.info("Service : recherche utilisateur par email={}", email);
        return dao.findByEmail(email);
    }

}