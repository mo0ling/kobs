package com.kob.backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
//JwtUtil 类为jwt 工具类，用来创建、解析 jwt token。
@Component
public class JwtUtil {
    public static final long JWT_TTL = 60 * 60 * 1000L * 24 * 14;  // 有效期14天
    public static final String JWT_KEY = "SDFGjhdsfalshdfHFdsjkdsfds121232131afasdfac";

    public static String getUUID() {//通用唯一识别码,-换空
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    /**
     * 用户登录成功后生成Jwt
     * 使用Hs256算法
     *
     * @param exp    jwt过期时间
     * @param claims 保存在Payload（有效载荷）中的内容
     * @return token字符串
     */
    public static String createJWT(String subject) {
        JwtBuilder builder = getJwtBuilder(subject, null, getUUID());
        return builder.compact();
    }

    private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis, String uuid) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;//指定签名的时候使用的签名算法加密算法
        SecretKey secretKey = generalKey();// 生成秘钥
        long nowMillis = System.currentTimeMillis();//生成JWT的时间，获取系统的当前时间
        Date now = new Date(nowMillis);
        if (ttlMillis == null) {
            ttlMillis = JwtUtil.JWT_TTL;
        }

        long expMillis = nowMillis + ttlMillis;//jwt过期时间
        Date expDate = new Date(expMillis);
        return Jwts.builder()//创建一个JwtBuilder，设置jwt的body
                .setId(uuid)//唯一的ID
                .setSubject(subject)// 主题  可以是JSON数据
                .setIssuer("sg")// 签发者
                .setIssuedAt(now)// 签发时间
                .signWith(signatureAlgorithm, secretKey) //使用HS256对称加密算法签名, 第二个参数为秘钥
                .setExpiration(expDate);//jwt过期时间
    }

    public static SecretKey generalKey() {
        byte[] encodeKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        return new SecretKeySpec(encodeKey, 0, encodeKey.length, "HmacSHA256");
    }
    /**
     * 解析token，获取到Payload（有效载荷）中的内容，包括验证签名，判断是否过期
     *
     * @param jwt
     * @return
     */
    public static Claims parseJWT(String jwt) throws Exception {
        SecretKey secretKey = generalKey();
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)//设置签名的秘钥
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
}