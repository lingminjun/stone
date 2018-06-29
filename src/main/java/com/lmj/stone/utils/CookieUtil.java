package com.lmj.stone.utils;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-27
 * Time: 下午2:03
 */
public final class CookieUtil {
    public static void setCookie(HttpServletResponse response, String key, String value, int maxAge, boolean httpOnly, boolean secure, String domain) {
        Cookie cookie = new Cookie(key, value);

        cookie.setMaxAge(maxAge <= 0 ? Integer.MAX_VALUE : maxAge); //有效时长 秒
        cookie.setHttpOnly(httpOnly); // 有設定時，Cookie只限被伺服端存取，無法在用戶端讀取。
        cookie.setSecure(secure);     // 有設定時，Cookie只能透過https的方式傳輸。
        cookie.setPath("/");
        cookie.setDomain(domain);

        response.addCookie(cookie);
    }

    //获取cookie: __da=-847366894926686; 301_uinfo=15673886363%2C%E6%9C%AA%E7%A1%AE%E8%AE%A4%2Ccms%2C1%2C0
    public static Map<String,String> getCookie(HttpHeaders headers) {
        List<String> cookies = headers.get("Cookie");//直接取cookie
        Map<String,String> map = new HashMap<String, String>();
        for (String str : cookies) {
            String[] ss = str.split("; ");
            for (String s : ss) {
                int idx = s.indexOf("=");
                if (idx > 0 && idx + 1 < s.length()) {
                    String key = s.substring(0,idx);
                    String value = s.substring(idx+1,s.length());//decode
                    try {
                        value = URLDecoder.decode(value,"utf-8");
                    } catch (Throwable e) {e.printStackTrace();}
                    map.put(key,value);
                }
            }
        }
        return map;
    }

    public static String getCPSStringFromCookie(String[] keys, Map<String,String> cookie) {
        if (keys == null || keys.length == 0 || cookie == null || cookie.size() == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            String value = cookie.get(key);

            if (value != null && value.length() > 0) {

                if (builder.length() > 0) {
                    builder.append("; ");//分隔符
                }

                builder.append(key);
                builder.append(":");
                builder.append(value);
            }
        }

        if (builder.length() == 0) {
            return null;
        }

        return builder.toString();
    }
}
