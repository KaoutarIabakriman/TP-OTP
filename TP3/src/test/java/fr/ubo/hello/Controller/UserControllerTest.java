package fr.ubo.hello.Controller;

import fr.ubo.hello.Exceptions.UserNotFoundException;
import fr.ubo.hello.Model.User;
import fr.ubo.hello.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService service;

    @InjectMocks
    private UserController controller;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User(1, "Kaoutar", "kaoutar@gmail.com", "1234");
        user2 = new User(2, "Iabakriman", "iabakriman@gmail.com", "1234");
    }


    @Test
    void testGetUsers_Success() {

        List<User> users = Arrays.asList(user1, user2);
        when(service.findAll()).thenReturn(users);


        ResponseEntity<List<User>> response = controller.getUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Kaoutar", response.getBody().get(0).getName());
        verify(service, times(1)).findAll();
    }

    @Test
    void testGetUsers_EmptyList() {
        when(service.findAll()).thenReturn(Arrays.asList());

        ResponseEntity<List<User>> response = controller.getUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(service, times(1)).findAll();
    }

    @Test
    void testGetUsers_ServiceThrowsException() {

        when(service.findAll()).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<List<User>> response = controller.getUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(service, times(1)).findAll();
    }


    @Test
    void testGetUser_Success() {
        when(service.getById(1)).thenReturn(user1);

        ResponseEntity<User> response = controller.getUser(1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Kaoutar", response.getBody().getName());
        assertEquals(1, response.getBody().getId());
        verify(service, times(1)).getById(1);
    }

    @Test
    void testGetUser_NotFound() {
        when(service.getById(999)).thenThrow(new UserNotFoundException(999));

        ResponseEntity<User> response = controller.getUser(999);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(service, times(1)).getById(999);
    }

    @Test
    void testGetUser_ServiceThrowsException() {
        when(service.getById(1)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<User> response = controller.getUser(1);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(service, times(1)).getById(1);
    }


    @Test
    void testCreate_Success() {
        User newUser = new User(0, "Iab", "Iab@gmail.com", "pass123");
        doNothing().when(service).create(newUser);

        ResponseEntity<String> response = controller.create(newUser);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User created", response.getBody());
        verify(service, times(1)).create(newUser);
    }

    @Test
    void testCreate_ServiceThrowsException() {
        User newUser = new User(0, "Iab", "Iab@gmail.com", "pass123");
        doThrow(new RuntimeException("Database error")).when(service).create(newUser);

        ResponseEntity<String> response = controller.create(newUser);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erreur lors de la création de l'utilisateur", response.getBody());
        verify(service, times(1)).create(newUser);
    }


    @Test
    void testUpdate_Success() {
        User updatedUser = new User(1, "Kaoutar Updated", "kaoutar.new@gmail.com", "newpass");
        doNothing().when(service).update(any(User.class));

        ResponseEntity<String> response = controller.update(1, updatedUser);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User updated", response.getBody());
        assertEquals(1, updatedUser.getId());
        verify(service, times(1)).update(updatedUser);
    }

    @Test
    void testUpdate_NotFound() {
        User updatedUser = new User(999, "Unknown", "unknown@gmail.com", "pass");
        doThrow(new UserNotFoundException(999)).when(service).update(any(User.class));

        ResponseEntity<String> response = controller.update(999, updatedUser);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Utilisateur non trouvé", response.getBody());
        verify(service, times(1)).update(updatedUser);
    }

    @Test
    void testUpdate_ServiceThrowsException() {
        User updatedUser = new User(1, "Kaoutar", "kaoutar@gmail.com", "1234");
        doThrow(new RuntimeException("Database error")).when(service).update(any(User.class));

        ResponseEntity<String> response = controller.update(1, updatedUser);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erreur lors de la mise à jour", response.getBody());
        verify(service, times(1)).update(updatedUser);
    }


    @Test
    void testDeleteUser_Success() {
        doNothing().when(service).delete(1);

        ResponseEntity<String> response = controller.deleteUser(1);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(service, times(1)).delete(1);
    }

    @Test
    void testDeleteUser_NotFound() {
        doThrow(new UserNotFoundException(999)).when(service).delete(999);

        ResponseEntity<String> response = controller.deleteUser(999);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Utilisateur non trouvé", response.getBody());
        verify(service, times(1)).delete(999);
    }

    @Test
    void testDeleteUser_ServiceThrowsException() {
        doThrow(new RuntimeException("Database error")).when(service).delete(1);

        ResponseEntity<String> response = controller.deleteUser(1);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erreur lors de la suppression", response.getBody());
        verify(service, times(1)).delete(1);
    }


}