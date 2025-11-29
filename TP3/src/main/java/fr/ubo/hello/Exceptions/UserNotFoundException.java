package fr.ubo.hello.Exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(int id) {
        super("Utilisateur avec l'id " + id + " introuvable.");
    }
}
