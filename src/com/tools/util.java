package com.tools;

import com.google.common.base.CharMatcher;
import com.google.common.primitives.Bytes;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class util {
    public String clean_filename(String filename){

        CharMatcher matcher = CharMatcher.ascii()
                .or(CharMatcher.inRange('0', '9'))
                .or(CharMatcher.anyOf("[]"));
        String output = matcher.retainFrom(filename);

        return output;
    }
    public byte[] get_blowfish_key(String track_id) throws NoSuchAlgorithmException {
        String secret = "g4el58wc0zvf9na1";
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (char ch: track_id.toCharArray()) {
            int ascii = ch;
            md.update((byte) ascii);
        }
        byte[] digest = md.digest();
        String id_md5 = DatatypeConverter.printHexBinary(digest).toLowerCase();
        ArrayList<Integer> key = new ArrayList<>();
        for (char ch = 0; ch < 16; ch++){
            key.add(id_md5.charAt(ch) ^ id_md5.charAt(ch+16) ^ secret.charAt(ch));
        }
        return Bytes.toArray(key);
    }
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
