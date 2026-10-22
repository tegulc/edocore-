package com.educore.modules.auth.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * Verification et generation de codes TOTP (RFC 6238), utilises pour le 2FA
 * (Google Authenticator / Microsoft Authenticator / toute app compatible).
 *
 * Implementation volontairement sans dependance externe : HMAC-SHA1 est
 * fourni nativement par la JCE (javax.crypto), ce qui evite d'ajouter une
 * librairie tierce pour un algorithme de 30 lignes bien specifie.
 *
 * Regle 01 : fenetre de tolerance de +-1 pas (30s) pour absorber le decalage
 *            d'horloge raisonnable entre le telephone et le serveur.
 * Regle 02 : le secret n'est jamais logue, jamais renvoye apres activation.
 */
@Service
public class TotpService {

    private static final int PAS_TEMPS_SECONDES = 30;
    private static final int NB_CHIFFRES = 6;
    private static final int FENETRE_TOLERANCE = 1; // +-1 pas = +-30s
    private static final String ALGORITHME_HMAC = "HmacSHA1";
    private static final Base32 BASE32 = new Base32();

    /**
     * Verifie un code saisi par l'utilisateur par rapport a son secret Base32,
     * en tolerant un decalage d'horloge de +-1 pas de temps.
     */
    public boolean verifier(String secretBase32, String codeSaisi) {
        if (secretBase32 == null || codeSaisi == null || !codeSaisi.matches("\\d{6}")) {
            return false;
        }
        long pasCourant = System.currentTimeMillis() / 1000L / PAS_TEMPS_SECONDES;
        byte[] secret = BASE32.decode(secretBase32);

        for (int decalage = -FENETRE_TOLERANCE; decalage <= FENETRE_TOLERANCE; decalage++) {
            String codeAttendu = genererCodePourPas(secret, pasCourant + decalage);
            if (codeAttendu.equals(codeSaisi)) {
                return true;
            }
        }
        return false;
    }

    /** Genere un nouveau secret Base32 (160 bits), a afficher une seule fois via QR code lors de l'activation. */
    public String genererNouveauSecret() {
        byte[] octets = new byte[20];
        new SecureRandom().nextBytes(octets);
        return BASE32.encode(octets);
    }

    /**
     * Construit l'URI otpauth:// standard, a encoder en QR code par le frontend,
     * lisible par toute application d'authentification compatible RFC 6238.
     */
    public String construireUriProvisionnement(String secretBase32, String emailUtilisateur) {
        String emetteur = "EDUCORE";
        return "otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d&period=%d"
                .formatted(emetteur, emailUtilisateur, secretBase32, emetteur, NB_CHIFFRES, PAS_TEMPS_SECONDES);
    }

    private String genererCodePourPas(byte[] secret, long pas) {
        try {
            byte[] donneesPas = ByteBuffer.allocate(8).putLong(pas).array();
            Mac mac = Mac.getInstance(ALGORITHME_HMAC);
            mac.init(new SecretKeySpec(secret, ALGORITHME_HMAC));
            byte[] hash = mac.doFinal(donneesPas);

            int decalageTronque = hash[hash.length - 1] & 0xF;
            int binaire =
                    ((hash[decalageTronque] & 0x7F) << 24)
                            | ((hash[decalageTronque + 1] & 0xFF) << 16)
                            | ((hash[decalageTronque + 2] & 0xFF) << 8)
                            | (hash[decalageTronque + 3] & 0xFF);

            int code = binaire % (int) Math.pow(10, NB_CHIFFRES);
            return String.format("%06d", code);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors du calcul du code TOTP", e);
        }
    }

    /** Encodeur/decodeur Base32 minimal (RFC 4648), suffisant pour les secrets TOTP. */
    private static final class Base32 {
        private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        String encode(byte[] donnees) {
            StringBuilder resultat = new StringBuilder();
            int buffer = 0, bitsRestants = 0;
            for (byte b : donnees) {
                buffer = (buffer << 8) | (b & 0xFF);
                bitsRestants += 8;
                while (bitsRestants >= 5) {
                    int index = (buffer >> (bitsRestants - 5)) & 0x1F;
                    resultat.append(ALPHABET.charAt(index));
                    bitsRestants -= 5;
                }
            }
            if (bitsRestants > 0) {
                int index = (buffer << (5 - bitsRestants)) & 0x1F;
                resultat.append(ALPHABET.charAt(index));
            }
            return resultat.toString();
        }

        byte[] decode(String texte) {
            String nettoye = texte.trim().toUpperCase().replace("=", "");
            byte[] resultat = new byte[nettoye.length() * 5 / 8];
            int buffer = 0, bitsRestants = 0, indexResultat = 0;
            for (char c : nettoye.toCharArray()) {
                int valeur = ALPHABET.indexOf(c);
                if (valeur < 0) continue;
                buffer = (buffer << 5) | valeur;
                bitsRestants += 5;
                if (bitsRestants >= 8) {
                    resultat[indexResultat++] = (byte) ((buffer >> (bitsRestants - 8)) & 0xFF);
                    bitsRestants -= 8;
                }
            }
            return resultat;
        }
    }
}
