package com.example.routeweb.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashUtils {

    public static String calculateHash(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(content);
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}

/*    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    String filePath = exchange.getIn().getHeader("Path", String.class);

    // Создание потока ввода из файла по указанному пути
    FileInputStream fis = new FileInputStream(filePath);

    byte[] byteArray = new byte[1024];
    int bytesCount;
while ((bytesCount = fis.read(byteArray)) != -1) {
        digest.update(byteArray, 0, bytesCount);
        }
        fis.close(); // Закрытие потока ввода после чтения данных из файла

        byte[] bytes = digest.digest();

        exchange.getMessage().setHeader("hash", Base64.getEncoder().encodeToString(bytes));*/

