package com.lmj.stone.core.algorithm;

import com.lmj.stone.core.encrypt.Encryptable;
import com.lmj.stone.core.sign.Signable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA工具类, blockSize = keySize - 11;
 */
public class RsaHelper implements Signable,Encryptable {
    private static final Logger logger = LoggerFactory.getLogger(RsaHelper.class);

    private RSAPublicKey     publicKey;
    private RSAPrivateCrtKey privateKey;

    //采用当前机器默认的provider,SunJCE
    static {
        Provider provider = Security.getProvider("BC");
        if (provider == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
//        Security.addProvider(new com.sun.crypto.provider.SunJCE());//jdk 1.7以上 and also the JAVA_HOME/jre/lib/ext/ contains the sunec.jar. Also the US_export_policy.jar and local_policy.jar are en the JAVA_HOME/jre/lib/security folder.
    }

    //生成 rsa秘钥
    public static void main(String[] args) {
        try {
            byte[][] key = randomKey(1024);
            System.out.println("pubKey:" + Base64Util.encodeToString(key[0]));
            System.out.println("priKey:" + Base64Util.encodeToString(key[1]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[][] randomKey(int size) {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            keygen.initialize(size <= 0 ? 1024 : size, random);
            KeyPair kp = keygen.generateKeyPair();
            byte[] pub = kp.getPublic().getEncoded();
            byte[] pri = kp.getPrivate().getEncoded();
            byte[][] rt = new byte[2][];
            rt[0] = pub;
            rt[1] = pri;
            return rt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RsaHelper(String publicKey, String privateKey) {
        this(Base64Util.decode(publicKey), privateKey == null ? null : Base64Util.decode(privateKey));
    }

    public RsaHelper(byte[] publicKey, byte[] privateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (publicKey != null && publicKey.length > 0) {
                this.publicKey = (RSAPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
            }
            if (privateKey != null && privateKey.length > 0) {
                this.privateKey = (RSAPrivateCrtKey)keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RsaHelper(String publicKey) {
        this(Base64Util.decode(publicKey));
    }

    public RsaHelper(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (publicKey != null && publicKey.length > 0) {
                this.publicKey = (RSAPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encrypt(byte[] content) {
        if (publicKey == null) {
            throw new RuntimeException("public key is null.");
        }
        if (content == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int size = publicKey.getModulus().bitLength() / 8 - 11;
            ByteArrayOutputStream baos = new ByteArrayOutputStream((content.length + size - 1) / size * (size + 11));
            int left = 0;
            for (int i = 0; i < content.length; ) {
                left = content.length - i;
                if (left > size) {
                    cipher.update(content, i, size);
                    i += size;
                } else {
                    cipher.update(content, i, left);
                    i += left;
                }
                baos.write(cipher.doFinal());
            }

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] secret) {
        if (privateKey == null) {
            throw new RuntimeException("private key is null.");
        }
        if (secret == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int size = privateKey.getModulus().bitLength() / 8;
            ByteArrayOutputStream baos = new ByteArrayOutputStream((secret.length + size - 12) / (size - 11) * size);
            int left = 0;
            for (int i = 0; i < secret.length; ) {
                left = secret.length - i;
                if (left > size) {
                    cipher.update(secret, i, size);
                    i += size;
                } else {
                    cipher.update(secret, i, left);
                    i += left;
                }

                baos.write(cipher.doFinal());
            }

            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("rsa decrypt failed.", e);
        }
        return null;
    }

    @Override
    public String sign(byte[] content) {
        if (privateKey == null) {
            throw new RuntimeException("private key is null.");
        }
        if (content == null) {
            return null;
        }
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(privateKey);
            signature.update(content);
            return Base64Util.encodeToString(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verify(String sign, byte[] content) {
        if (publicKey == null) {
            throw new RuntimeException("public key is null.");
        }
        if (sign == null || content == null) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(publicKey);
            signature.update(content);
            return signature.verify(Base64Util.decode(sign));
        } catch (Exception e) {
            logger.error("rsa verify failed.", e);
        }
        return false;
    }

    public static byte[] encrypt(byte[] content, byte[] publicKey) {
        if (content == null || publicKey == null) {
            return null;
        }
        return new RsaHelper(publicKey, null).encrypt(content);
    }

    public static byte[] decrypt(byte[] secret, byte[] privateKey) {
        if (secret == null || privateKey == null) {
            return null;
        }
        return new RsaHelper(null, privateKey).decrypt(secret);
    }

    public static String sign(byte[] content, byte[] privateKey) {
        if (content == null || privateKey == null) {
            return null;
        }
        return new RsaHelper(null, privateKey).sign(content);
    }

    public static boolean verify(String sign, byte[] content, byte[] publicKey) {
        if (sign == null || content == null || publicKey == null) {
            return false;
        }
        return new RsaHelper(publicKey, null).verify(sign, content);
    }
}
