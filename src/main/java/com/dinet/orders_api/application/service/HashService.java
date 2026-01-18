package com.dinet.orders_api.application.service;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class HashService {

    public String calcularSHA256(InputStream inputStream) throws IOException {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while((bytesRead = inputStream.read(buffer)) != -1){
                digest.update(buffer, 0 , bytesRead);
            }
            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e){
            log.error("No se encontro el algoritmo SHA-256", e);
            throw new RuntimeException("Error calculando hash: algoritmo no disponible", e);
        }
    }

    private String bytesToHex(byte[] bytes){
        StringBuilder result = new StringBuilder();

        for (byte b : bytes){
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }
}
