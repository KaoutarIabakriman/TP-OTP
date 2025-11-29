package fr.ubo.hello.Dao;

import fr.ubo.hello.Model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("mockDAO")

public class UserDao_Mock implements UserDao {

    private List<User> users = new ArrayList<>();

    public UserDao_Mock() {
        users.add(new User(1, "Kaoutar", "kaoutar@gmail.com", "1234"));
        users.add(new User(2, "Iabakriman", "iabakriman@gmail.com", "1234"));
    }

    public List<User> findAll() { return users; }
    public User findById(int id) {
        return users.stream().filter(user -> user.getId() == id).findFirst().orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }


    @Override
    public boolean update(User user) {

        return false;
    }

    @Override
    public boolean delete(int id) {

        return false;
    }

    public boolean save(User u) { users.add(u);
        return false;
    }

}
