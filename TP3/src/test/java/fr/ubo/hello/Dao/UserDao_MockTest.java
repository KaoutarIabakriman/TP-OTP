package fr.ubo.hello.Dao;

import fr.ubo.hello.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDao_MockTest {

    private UserDao_Mock dao;

    @BeforeEach
    void setUp() {
        dao = new UserDao_Mock();
    }

    @Test
    void testFindAll_ReturnsInitialUsers() {
        List<User> users = dao.findAll();
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("Kaoutar", users.get(0).getName());
        assertEquals("Iabakriman", users.get(1).getName());
    }

    @Test
    void testFindAll_ContainsCorrectData() {
        List<User> users = dao.findAll();

        User user1 = users.get(0);
        assertEquals(1, user1.getId());
        assertEquals("Kaoutar", user1.getName());
        assertEquals("kaoutar@gmail.com", user1.getEmail());
        assertEquals("1234", user1.getPassword());

        User user2 = users.get(1);
        assertEquals(2, user2.getId());
        assertEquals("Iabakriman", user2.getName());
        assertEquals("iabakriman@gmail.com", user2.getEmail());
        assertEquals("1234", user2.getPassword());
    }

    @Test
    void testFindById_ExistingUser() {
        User user = dao.findById(1);
        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("Kaoutar", user.getName());
        assertEquals("kaoutar@gmail.com", user.getEmail());
    }

    @Test
    void testFindById_SecondUser() {
        User user = dao.findById(2);
        assertNotNull(user);
        assertEquals(2, user.getId());
        assertEquals("Iabakriman", user.getName());
        assertEquals("iabakriman@gmail.com", user.getEmail());
    }

    @Test
    void testFindById_NonExistentUser() {
        User user = dao.findById(999);
        assertNull(user);
    }

    @Test
    void testFindById_ZeroId() {
        User user = dao.findById(0);
        assertNull(user);
    }

    @Test
    void testFindById_NegativeId() {
        User user = dao.findById(-1);
        assertNull(user);
    }

    @Test
    void testSave_AddsUserToList() {
        User newUser = new User(3, "Alae", "Alae@gmail.com", "pass123");
        dao.save(newUser);
        List<User> users = dao.findAll();
        assertEquals(3, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Alae")));
    }

    @Test
    void testSave_UserCanBeRetrievedById() {
        User newUser = new User(3, "Alae", "Alae@gmail.com", "pass123");
        dao.save(newUser);
        User retrieved = dao.findById(3);
        assertNotNull(retrieved);
        assertEquals("Alae", retrieved.getName());
        assertEquals("Alae@gmail.com", retrieved.getEmail());
    }

    @Test
    void testSave_ReturnsFalse() {
        User newUser = new User(3, "Alae", "Alae@gmail.com", "pass123");
        boolean result = dao.save(newUser);
        assertFalse(result, "save() devrait retourner false (méthode non implémentée)");
    }

    @Test
    void testSave_MultipleUsers() {
        User user3 = new User(3, "User3", "user3@gmail.com", "pass");
        User user4 = new User(4, "User4", "user4@gmail.com", "pass");
        dao.save(user3);
        dao.save(user4);
        List<User> users = dao.findAll();
        assertEquals(4, users.size());
        assertNotNull(dao.findById(3));
        assertNotNull(dao.findById(4));
    }

    @Test
    void testUpdate_ReturnsFalse() {
        User updatedUser = new User(1, "Kaoutar Updated", "kaoutar.new@gmail.com", "newpass");
        boolean result = dao.update(updatedUser);
        assertFalse(result, "update() devrait retourner false (méthode non implémentée)");
    }

    @Test
    void testUpdate_DoesNotModifyData() {
        User updatedUser = new User(1, "Kaoutar Updated", "kaoutar.new@gmail.com", "newpass");
        dao.update(updatedUser);
        User original = dao.findById(1);
        assertEquals("Kaoutar", original.getName(), "Le nom ne devrait pas changer");
        assertEquals("kaoutar@gmail.com", original.getEmail(), "L'email ne devrait pas changer");
    }

    @Test
    void testDelete_ReturnsFalse() {
        boolean result = dao.delete(1);
        assertFalse(result, "delete() devrait retourner false (méthode non implémentée)");
    }

    @Test
    void testDelete_DoesNotRemoveUser() {
        dao.delete(1);
        User user = dao.findById(1);
        assertNotNull(user, "L'utilisateur ne devrait pas être supprimé");
        assertEquals("Kaoutar", user.getName());
    }

    @Test
    void testDelete_NonExistentUser() {
        boolean result = dao.delete(999);
        assertFalse(result);
    }

    @Test
    void testMultipleOperations() {
        User newUser = new User(3, "TestUser", "test@gmail.com", "test");
        assertEquals(2, dao.findAll().size());
        dao.save(newUser);
        assertEquals(3, dao.findAll().size());
        User found = dao.findById(3);
        assertNotNull(found);
        assertEquals("TestUser", found.getName());
        dao.delete(3);
        assertNotNull(dao.findById(3), "L'utilisateur devrait toujours exister");
    }

    @Test
    void testFindAll_ReturnsActualList() {
        List<User> initialUsers = dao.findAll();
        assertEquals(2, initialUsers.size(), "État initial devrait avoir 2 utilisateurs");
        User newUser = new User(3, "NewUser", "new@gmail.com", "pass");
        dao.save(newUser);
        List<User> updatedUsers = dao.findAll();
        assertEquals(3, updatedUsers.size(), "Devrait avoir 3 utilisateurs après ajout");
        assertTrue(updatedUsers.stream()
                .anyMatch(user -> user.getId() == 3 && "NewUser".equals(user.getName())));
    }
}
