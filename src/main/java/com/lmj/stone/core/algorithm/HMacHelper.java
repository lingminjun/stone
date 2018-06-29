package com.lmj.stone.core.algorithm;

import com.lmj.stone.core.sign.Signable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

/**
 * 测试结论，对于HMacHelper这种，绝大部分时间都是同一个密钥在工作， 但是需要在多线程访问时进行同步的辅助类，使用ThreadLocal为每一个 线程缓存一个实例可以避免进行锁操作
 *
 * @author rendong
 */
public class HMacHelper implements Signable {
    private static final Logger logger = LoggerFactory.getLogger(HMacHelper.class);
    private Mac mac;

    /**
     * MAC算法可选以下多种算法
     * <pre>
     * HmacMD5
     * HmacSHA1
     * HmacSHA256
     * HmacSHA384
     * HmacSHA512
     * </pre>
     */
    private static final String KEY_MAC = "HmacMD5";

    public HMacHelper(String key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes("utf-8"), KEY_MAC);
            mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
        } catch (Exception e) {
            logger.error("create hmac helper failed.", e);
        }
    }

    @Override
    public String sign(byte[] content) {
        //        synchronized (this) {
        return Base64Util.encodeToString(mac.doFinal(content));
        //        }
    }

    @Override
    public boolean verify(String sig, byte[] content) {
        try {
            byte[] result = null;
            //            synchronized (this) {
            result = mac.doFinal(content);
            //            }
            return Arrays.equals(Base64Util.decode(sig), result);
        } catch (Exception e) {
            logger.error("varify signature failed.", e);
        }
        return false;
    }

}
