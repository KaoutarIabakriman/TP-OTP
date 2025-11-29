package fr.ubo.hello.Controller;

import fr.ubo.hello.Model.User;
import fr.ubo.hello.Service.UserService;
import fr.ubo.hello.Exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService service;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        logger.info("Controller : GET /users - Récupération de tous les utilisateurs");

        try {
            List<User> list = service.findAll();
            logger.info("Controller : {} utilisateurs retournés avec succès", list.size());
            return ResponseEntity.ok(list);

        } catch (Exception e) {
            logger.error("Controller : Erreur lors de la récupération des utilisateurs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        logger.info("Controller : GET /users/{} - Récupération utilisateur", id);

        try {
            User user = service.getById(id);
            logger.info("Controller : Utilisateur id={} retourné avec succès", id);
            return ResponseEntity.ok(user);

        } catch (UserNotFoundException e) {
            logger.warn("Controller : Utilisateur id={} non trouvé - Retour 404", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            logger.error("Controller : Erreur lors de la récupération de l'utilisateur id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody User u) {
        logger.info("Controller : POST /users - Création utilisateur '{}'", u.getName());

        try {
            service.create(u);
            logger.info("Controller : Utilisateur '{}' créé avec succès - Retour 201", u.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body("User created");

        } catch (Exception e) {
            logger.error("Controller : Erreur lors de la création de l'utilisateur '{}'", u.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création de l'utilisateur");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable int id, @RequestBody User u) {
        logger.info("Controller : PUT /users/{} - Mise à jour utilisateur", id);

        try {
            u.setId(id);
            service.update(u);
            logger.info("Controller : Utilisateur id={} mis à jour avec succès - Retour 200", id);
            return ResponseEntity.ok("User updated");

        } catch (UserNotFoundException e) {
            logger.warn("Controller : Utilisateur id={} non trouvé pour mise à jour - Retour 404", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé");

        } catch (Exception e) {
            logger.error("Controller : Erreur lors de la mise à jour de l'utilisateur id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") int id) {
        logger.info("Controller : DELETE /users/{} - Suppression utilisateur", id);

        try {
            service.delete(id);
            logger.info("Controller : Utilisateur id={} supprimé avec succès - Retour 204", id);
            return ResponseEntity.noContent().build();

        } catch (UserNotFoundException e) {
            logger.warn("Controller : Utilisateur id={} non trouvé pour suppression - Retour 404", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé");

        } catch (Exception e) {
            logger.error("Controller : Erreur lors de la suppression de l'utilisateur id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression");
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("Controller : Exception non gérée interceptée", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Une erreur inattendue est survenue");
    }
}