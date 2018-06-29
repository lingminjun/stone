package com.lmj.stone.core.sign;

import com.lmj.stone.core.algorithm.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-27
 * Time: 下午11:04
 */
public enum Signature {

    CRC16,
    CRC32,
    MD5,
    SHA1,
    HMAC,
    RSA,
    ECC;

    public static Signature signatureOf(String name) {
        //循环输出 值
        for (Signature e : Signature.values()) {
            if (e.name().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return Signature.SHA1;//默认
    }

    public static Signable getSignable(String signature, String pubKey, String priKey) {
        return getSignable(signatureOf(signature),pubKey,priKey);
    }

    public static Signable getSignable(Signature signature, String pubKey, String priKey) {
        switch (signature) {
            case CRC16:return new CRC16Helper(pubKey);
            case CRC32:return new CRC32Helper(pubKey);
            case MD5:return new Md5Helper(pubKey);
            case SHA1:return new SHAHelper(pubKey);
            case HMAC:return new HMacHelper(pubKey);
            case RSA:return new RsaHelper(pubKey,priKey);
            case ECC:return new EccHelper(pubKey,priKey);
            default:return new SHAHelper(pubKey);
        }
    }
}
