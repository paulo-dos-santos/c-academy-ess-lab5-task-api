package cncs.academy.ess.util;
import io.github.cdimascio.dotenv.Dotenv;
public class AppConfig {
    private static final Dotenv dotenv = Dotenv.load();
    public static String getSecretKey() {
        String key = dotenv.get("SECRET_KEY");
        if (key == null || key.isEmpty()) {
            throw new RuntimeException("CRITICAL: SECRET_KEY not found in .env file!");
        }
        return key;
    }
}