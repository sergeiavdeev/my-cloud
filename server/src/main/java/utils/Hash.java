package utils;

import java.security.MessageDigest;
import java.util.UUID;

public class Hash {

    private static final String SOLT = "avdey_berendey";

    public static String sha256(String login, String password) {

        String data = login + SOLT + password;

        StringBuilder sb = new StringBuilder();
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data.getBytes());
            byte[] byteData = md.digest();

            for (byte byteDatum : byteData) {
                sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }
}
