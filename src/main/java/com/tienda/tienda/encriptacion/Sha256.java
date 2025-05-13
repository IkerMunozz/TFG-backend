package com.tienda.tienda.encriptacion;

import java.security.MessageDigest;

public class Sha256 {

    static public String getSha256(String inputVal) throws Exception {
        MessageDigest myDigest = MessageDigest.getInstance("SHA-256");
        myDigest.update(inputVal.getBytes());
        byte[] dataBytes = myDigest.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < dataBytes.length; i++) {
            sb.append(Integer.toString((dataBytes[i])).substring(1));
        }
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < dataBytes.length; i++) {
            String hex = Integer.toHexString(0xff & dataBytes[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        String retParam = hexString.toString();
        return retParam;
    }
}
