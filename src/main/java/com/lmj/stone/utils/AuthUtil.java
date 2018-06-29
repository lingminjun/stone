package com.lmj.stone.utils;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-27
 * Time: 下午2:41
 */
public final class AuthUtil {
    public static final String DEVICE_ID_COOKIE_NAME = "__dd";
    public static final String ACCOUNT_ID_COOKIE_NAME = "__ad";
    public static final String USER_ID_COOKIE_NAME = "__ud";
    public static final String DEVICE_TOKEN_COOKIE_NAME = "__ds";
    public static final String USER_TOKEN_COOKIE_NAME = "__tk";
    public static final String AUTHORIZATION_HEAD_KEY = "Authorization";

    public static void addDeviceToken(HttpServletResponse response, String jwt, String did, int appId, String domain, int maxAge) {

        if (did != null && did.length() > 0) {
            CookieUtil.setCookie(response,appId + DEVICE_ID_COOKIE_NAME,did,0,false,false,domain);
        }

        if (jwt != null && jwt.length() > 0) {
            CookieUtil.setCookie(response,appId + DEVICE_TOKEN_COOKIE_NAME,jwt,maxAge,true,false,domain);
        }
    }

    public static void addUserToken(HttpServletResponse response,String jwt, String did, String uid, String acnt,int appId, String domain, int maxAge) {

        if (did != null && did.length() > 0) {
            CookieUtil.setCookie(response,appId + DEVICE_ID_COOKIE_NAME,did,0,false,false,domain);
        }

        if (acnt != null && acnt.length() > 0) {
            CookieUtil.setCookie(response,appId + ACCOUNT_ID_COOKIE_NAME,acnt,0,false,false,domain);
        }

        if (uid != null && uid.length() > 0) {
            CookieUtil.setCookie(response,appId + USER_ID_COOKIE_NAME,uid,0,false,false,domain);
        }

        if (jwt != null && jwt.length() > 0) {
            CookieUtil.setCookie(response,appId + USER_TOKEN_COOKIE_NAME,jwt,maxAge,true,false,domain);
        }

    }

    public static String getAuthorization(HttpHeaders headers, boolean checkCookie, int appId) {
        String jwt = headers.getFirst(AUTHORIZATION_HEAD_KEY);
        if (jwt != null && jwt.length() > 0) {
            return jwt;
        }

        if (!checkCookie) {
            return null;
        }

        String targetKey1 = appId + USER_TOKEN_COOKIE_NAME;
        String targetKey2 = appId + DEVICE_TOKEN_COOKIE_NAME;

        Map<String,String> map = CookieUtil.getCookie(headers);
        for (String key : map.keySet()) {

            if (key.equals(targetKey1)) {
                return map.get(key);
            } else if (key.equals(targetKey2)) {
                return map.get(key);
            }

        }
        return null;
    }

    public static String getHostDomain(HttpHeaders headers) {
        String host = headers.getFirst("Host");
        String[] ss = host.split("\\:");//去掉host
        if (ss.length > 0) {
            host = ss[0];
        }

        //判断是否为ip，若为ip则直接返回
        if (isipv4(host)) {
            return host;
        }

        //放到到主域名下
        String[] strs = host.split("\\.");
        if (strs.length <= 2) {
            return host;
        } else {
            return "." + strs[strs.length - 2] + "." + strs[strs.length - 1];
        }
    }

    public static boolean isipv4(String ip){
        //判断是否是一个ip地址
        boolean a=false;
        boolean flag =ip.matches("\\d{1,3}\\.d{1,3}\\.d{1,3}\\.d{1,3}");
        if (flag) {
            String s[] = ip.split("\\.");
            int i1= Integer.parseInt(s[0]);
            int i2= Integer.parseInt(s[1]);
            System.out.println(i2);
            int i3= Integer.parseInt(s[2]);
            int i4= Integer.parseInt(s[3]);
            if(i1>0&&i1<256&&i2<256&&i3<256&&i4<256) {
                a = true;
            }

        }
        return a;
    }


    private static final String HTTP_HEADER_SEPARATE = ", ";
    public static String getClientIP(HttpHeaders headers) {
        String ip = null;
        if (headers != null) {
            ip = headers.getFirst("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = headers.getFirst("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = headers.getFirst("http-x-forwarded-for");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = headers.getFirst("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = headers.getFirst("remote-addr");
            }
            //
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = headers.getFirst("Remote Address");//request.getRemoteAddr();
            }
        }

        if (ip  ==   null   ||  ip.length()  ==   0   ||  "unknown" .equalsIgnoreCase(ip)) {
            return ip;
        }

        //X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 192.168.1.100
        String[] ips = ip.split(",");
        if (ips != null && ips.length > 0) {
            return ips[0];
        }

        return ip;
    }
}
