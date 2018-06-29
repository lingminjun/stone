package com.lmj.stone.core.coding;

import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-27
 * Time: 下午10:49
 */
public interface Codeable {

    public static final Charset UTF8 = Charset.forName("utf-8");
    public static final Charset ASCII = Charset.forName("ascii");

    byte[] encode(byte[] data);
    byte[] decode(byte[] data);
}
