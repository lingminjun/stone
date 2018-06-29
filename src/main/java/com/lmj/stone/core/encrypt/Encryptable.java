package com.lmj.stone.core.encrypt;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-05-09
 * Time: 上午11:18
 */
public interface Encryptable {
    byte[] encrypt(byte[] content);
    byte[] decrypt(byte[] secret);
}
