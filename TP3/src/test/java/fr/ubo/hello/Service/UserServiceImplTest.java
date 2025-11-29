package fr.ubo.hello.Service;

import fr.ubo.hello.Dao.UserDao;
import fr.ubo.hello.Exceptions.UserNotFoundException;
import fr.ubo.hello.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao dao;

    @InjectMocks
    private UserServiceImpl service;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User(1, "Kaoutar", "kaoutar@gmail.com", "1234");
        user2 = new User(2, "Iabakriman", "iabakriman@gmail.com", "1234");
    }

    @Test
    void testFindAll_Success() {
        List<User> users = Arrays.asList(user1, user2);
        when(dao.findAll()).thenReturn(users);
        List<User> result = service.findAll();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Kaoutar", result.get(0).getName());
        assertEquals("Iabakriman", result.get(1).getName());
        verify(dao, times(1)).findAll();
    }

    @Test
    void testFindAll_EmptyList() {
        when(dao.findAll()).thenReturn(Arrays.asList());
        List<User> result = service.findAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dao, times(1)).findAll();
    }

    @Test
    void testFindAll_DaoThrowsException() {
        when(dao.findAll()).thenThrow(new RuntimeException("Database connection error"));
        assertThrows(RuntimeException.class, () -> service.findAll());
        verify(dao, times(1)).findAll();
    }

    @Test
    void testGetById_Success() {
        when(dao.findById(1)).thenReturn(user1);
        User result = service.getById(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Kaoutar", result.getName());
        assertEquals("kaoutar@gmail.com", result.getEmail());
        verify(dao, times(1)).findById(1);
    }

    @Test
    void testGetById_NotFound() {
        when(dao.findById(999)).thenReturn(null);
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> service.getById(999)
        );
        assertTrue(exception.getMessage().contains("999"));
        verify(dao, times(1)).findById(999);
    }

    @Test
    void testGetById_DaoThrowsException() {
        when(dao.findById(1)).thenThrow(new RuntimeException("Database error"));
        assertThrows(RuntimeException.class, () -> service.getById(1));
        verify(dao, times(1)).findById(1);
    }

    @Test
    void testCreate_Success() {
        User newUser = new User(0, "Marwa", "marwa@gmail.com", "pass123");
        when(dao.save(newUser)).thenReturn(true);
        assertDoesNotThrow(() -> service.create(newUser));
        verify(dao, times(1)).save(newUser);
    }

    @Test
    void testCreate_Failure() {
        User newUser = new User(0, "Marwa", "marwa@gmail.com", "pass123");
        when(dao.save(newUser)).thenReturn(false);
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.create(newUser)
        );
        assertTrue(exception.getMessage().contains("création"));
        verify(dao, times(1)).save(newUser);
    }

    @Test
    void testCreate_DaoThrowsException() {
        User newUser = new User(0, "Marwa", "marwa@gmail.com", "pass123");
        when(dao.save(newUser)).thenThrow(new RuntimeException("Duplicate key"));
        assertThrows(RuntimeException.class, () -> service.create(newUser));
        verify(dao, times(1)).save(newUser);
    }

    @Test
    void testUpdate_Success() {
        User updatedUser = new User(1, "Kaoutar Updated", "kaoutar.new@gmail.com", "newpass");
        when(dao.update(updatedUser)).thenReturn(true);
        assertDoesNotThrow(() -> service.update(updatedUser));
        verify(dao, times(1)).update(updatedUser);
    }

    @Test
    void testUpdate_NotFound() {
        User updatedUser = new User(999, "Unknown", "unknown@gmail.com", "pass");
        when(dao.update(updatedUser)).thenReturn(false);
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> service.update(updatedUser)
        );
        assertTrue(exception.getMessage().contains("999"));
        verify(dao, times(1)).update(updatedUser);
    }

    @Test
    void testUpdate_DaoThrowsException() {
        User updatedUser = new User(1, "Kaoutar", "kaoutar@gmail.com", "1234");
        when(dao.update(updatedUser)).thenThrow(new RuntimeException("Database error"));
        assertThrows(RuntimeException.class, () -> service.update(updatedUser));
        verify(dao, times(1)).update(updatedUser);
    }

    @Test
    void testDelete_Success() {
        when(dao.delete(1)).thenReturn(true);
        assertDoesNotThrow(() -> service.delete(1));
        verify(dao, times(1)).delete(1);
    }

    @Test
    void testDelete_NotFound() {
        when(dao.delete(999)).thenReturn(false);
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> service.delete(999)
        );
        assertTrue(exception.getMessage().contains("999"));
        verify(dao, times(1)).delete(999);
    }

    @Test
    void testDelete_DaoThrowsException() {
        when(dao.delete(1)).thenThrow(new RuntimeException("Database error"));
        assertThrows(RuntimeException.class, () -> service.delete(1));
        verify(dao, times(1)).delete(1);
    }
}
