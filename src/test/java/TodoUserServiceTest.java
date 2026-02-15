import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.service.TodoUserService;
import cncs.academy.ess.util.AppConfig;
import cncs.academy.ess.util.PasswordUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoUserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private TodoUserService service;

    @Test
    void login_shouldReturnValidJWTTokenWhenCredentialsMatch() throws Exception {
        String username = "user1";
        String plainPassword = "password123";
        String testSecretKey = "secret_key_for_testing_only";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        User mockUser = new User(username, hashedPassword);
        mockUser.setId(1);
        try (MockedStatic<AppConfig> appConfigMock = mockStatic(AppConfig.class)) {

            appConfigMock.when(AppConfig::getSecretKey).thenReturn(testSecretKey);
            when(repository.findByUsername(username)).thenReturn(mockUser);

            String token = service.login(username, plainPassword);

            assertNotNull(token, "O token não deve ser nulo");

            Algorithm algorithm = Algorithm.HMAC256(testSecretKey);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("back-end")
                    .build()
                    .verify(token);

            assertEquals(username, decodedJWT.getClaim("username").asString());
        }
    }

    @Test
    void login_shouldReturnNull_WhenPasswordIsWrong() throws Exception {
        String username = "user";
        User mockUser = new User(username, PasswordUtil.hashPassword("correctPassword"));

        when(repository.findByUsername(username)).thenReturn(mockUser);

        String result = service.login(username, "wrongPassword");

        assertNull(result, "O login deve falhar com password errada");
    }
}