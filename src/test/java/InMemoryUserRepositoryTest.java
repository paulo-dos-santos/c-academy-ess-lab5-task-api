import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {
    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void saveAndFindById_ShouldReturnSavedUser() {
        User user = new User("jane", "password");
        int id = repository.save(user);
        User savedUser = repository.findById(id);
        assertEquals(user, savedUser);
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        repository.save(new User("alice", "1234"));
        repository.save(new User("bob", "5678"));
        User result = repository.findByUsername("alice");
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        assertEquals("1234", result.getPassword());
    }

    @Test
    void findByUsername_ShouldReturnNull_WhenMissing() {
        User result = repository.findByUsername("non_existent_user");
        assertNull(result, "Deve retornar null se o utilizador não existir");
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        int id = repository.save(new User("to_delete", "pass"));
        assertNotNull(repository.findById(id));
        repository.deleteById(id);
        User deletedUser = repository.findById(id);
        assertNull(deletedUser, "O utilizador não deve existir após deleteById");
    }

    @Test
    void findAll_ShouldReturnAllSavedUsers() {
        repository.save(new User("u1", "p1"));
        repository.save(new User("u2", "p2"));
        repository.save(new User("u3", "p3"));
        List<User> allUsers = repository.findAll();
        assertEquals(3, allUsers.size(), "Deve retornar exatamente 3 utilizadores");
    }
}