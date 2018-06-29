package com.lmj.stone.core.encrypt;

import com.lmj.stone.core.algorithm.AesHelper;
import com.lmj.stone.core.algorithm.EccHelper;
import com.lmj.stone.core.algorithm.RsaHelper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-27
 * Time: 下午11:21
 */
public enum Encryption {
    AES,// = "aes";
    RSA,// = "rsa";
    ECC;// = "ecc";

    public static Encryption encryptionOf(String name) {
        //循环输出 值
        for (Encryption e : Encryption.values()) {
            if (e.name().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return Encryption.RSA;//默认
    }

    public static Encryptable getEncryptable(String encryption, String pubKey, String priKey) {
        return getEncryptable(encryptionOf(encryption),pubKey,priKey);
    }

    public static Encryptable getEncryptable(Encryption encryption, String pubKey, String priKey) {
        switch (encryption) {
            case AES:return new AesHelper(priKey);
            case RSA:return new RsaHelper(pubKey,priKey);
            case ECC:return new EccHelper(pubKey,priKey);
            default:return new AesHelper(priKey);
        }
    }
}
