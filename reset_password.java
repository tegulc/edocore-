import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ResetPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "admin12345";
        String hash = encoder.encode(password);
        System.out.println("Mot de passe: " + password);
        System.out.println("Hash BCrypt: " + hash);
    }
}