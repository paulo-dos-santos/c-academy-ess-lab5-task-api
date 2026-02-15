package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.util.AppConfig;
import cncs.academy.ess.util.PasswordUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TodoUserService {
    private final UserRepository repository;

    public TodoUserService(UserRepository userRepository) {
        this.repository = userRepository;
    }
    public User addUser(String username, String password) throws NoSuchAlgorithmException {

        String hashedPassword = PasswordUtil.hashPassword(password);

        User user = new User(username, hashedPassword);
        int id = repository.save(user);
        user.setId(id);
        return user;
    }
    public User getUser(int id) {
        return repository.findById(id);
    }

    public void deleteUser(int id) {
        repository.deleteById(id);
    }

    public String login(String username, String password) throws NoSuchAlgorithmException {
        User user = repository.findByUsername(username);
        if (user == null) {
            return null;
        }
        if (PasswordUtil.checkPassword(password, user.getPassword())) {
            return createAuthToken(user);
        }
        return null;
    }

    private String createAuthToken(User user) {

        Algorithm algorithm = Algorithm.HMAC256(AppConfig.getSecretKey());

        Instant now = Instant.now();
        Instant expiration = now.plus(1, ChronoUnit.HOURS); // Expira em 1 hora

        return JWT.create()
                .withIssuer("back-end")
                .withClaim("username", user.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expiration)
                .sign(algorithm);
    }
}
