package backend.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // hash the password
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // verify the password
    public static boolean verify(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}