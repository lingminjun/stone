package com.lmj.stone.core.algorithm;

import com.lmj.stone.core.encrypt.Encryptable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

public class AesHelper implements Encryptable {
    private SecretKeySpec   keySpec;
    private IvParameterSpec iv;
    // 需要使用无填充时使用，此时会为密钥计算出一个唯一的iv来使用
    private boolean useCFB = false;


    static {
        Provider provider = Security.getProvider("BC");
        if (provider == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    //生成 aes秘钥
    public static void main(String[] args) {
        try {
            byte[] key = AesHelper.randomKey(256);
            System.out.println("priKey:" + Base64Util.encodeToString(key));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] randomKey(int size) {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(size > 0 ? size : 256, new SecureRandom());
            return gen.generateKey().getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AesHelper(String aesKey) {
        this(Base64Util.decode(aesKey),null);
    }

    public AesHelper(byte[] aesKey, byte[] iv) {
        if (aesKey == null || aesKey.length < 16 || (iv != null && iv.length < 16)) {
            throw new RuntimeException("错误的初始密钥");
        }
        if (iv == null) {
            iv = Md5Helper.compute(aesKey);
        }
        keySpec = new SecretKeySpec(aesKey, "AES");
        this.iv = new IvParameterSpec(iv);
    }

    public AesHelper(byte[] aesKey, boolean cfb) {
        if (aesKey == null || aesKey.length < 16) {
            throw new RuntimeException("错误的初始密钥");
        }
        useCFB = cfb;
        keySpec = new SecretKeySpec(aesKey, "AES");
        this.iv = new IvParameterSpec(Md5Helper.compute(aesKey));
    }

    @Override
    public byte[] encrypt(byte[] data) {
        Cipher cipher = null;
        try {
            if (useCFB) {
                cipher = Cipher.getInstance("AES/CFB/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            } else {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            }

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] secret) {
        Cipher cipher = null;
        try {
            if (useCFB) {
                cipher = Cipher.getInstance("AES/CFB/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            } else {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            }

            return cipher.doFinal(secret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
