package cncs.academy.ess.controller;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.util.AppConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.casbin.jcasbin.main.Enforcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationMiddleware implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationMiddleware.class);

    private final UserRepository userRepository;

    private final Enforcer enforcer;

    public AuthorizationMiddleware(UserRepository userRepository) {

        this.userRepository = userRepository;

        this.enforcer = new Enforcer("api-access-control/model.conf", "api-access-control/policy.csv");
    }

    @Override
    public void handle(Context ctx) throws Exception {
        if (ctx.header("Access-Control-Request-Headers") != null) {
            return;
        }

        if (ctx.path().equals("/user") && ctx.method().name().equals("POST")
        || ctx.path().equals("/login") && ctx.method().name().equals("POST")) {
            return;
        }

        // Check if authorization header exists
        String authorizationHeader = ctx.header("Authorization");
        String path = ctx.path();
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.info("Authorization header is missing or invalid '{}' for path '{}'", authorizationHeader, path);
            throw new UnauthorizedResponse("Token missing");
        }

        // Extract token from authorization header
        String token = authorizationHeader.substring(7).trim(); // Remove "Bearer "


        try {

            Algorithm algorithm = Algorithm.HMAC256(AppConfig.getSecretKey());
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("back-end")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            String username = jwt.getClaim("username").asString();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                logger.info("User not found for token: {}", username);
                throw new UnauthorizedResponse("User invalid");
            }

            String obj = ctx.path();
            String act = ctx.method().name();
            if (!enforcer.enforce(username, obj, act)) {
                logger.warn("Acesso negado: User '{}' tentou {} em {}", username, act, obj);
                throw new ForbiddenResponse("You don't have permission to access this resource");
            }

            ctx.attribute("userId", user.getId());
            ctx.attribute("username", user.getUsername());

        } catch (Exception e) {
            logger.error("Token verification failed: {}", e.getMessage());
            throw new UnauthorizedResponse("Token invalid or expired");
        }

        // Check if token is valid (perform authentication logic)
        /* int userId = validateTokenAndGetUserId(ctx, token);
        if (userId == -1) {
            logger.info("Authorization token is invalid {}", token  );
            throw new UnauthorizedResponse();
        }

        // Add user ID to context for use in route handlers
        ctx.attribute("userId", userId);*/
    }

    /*private int validateTokenAndGetUserId(Context cts, String token) {
        User user = userRepository.findByUsername(token);
        if (user == null) {
            // user not found, token is invalid
            return -1;
        }
        return user.getId();
    }*/
}

