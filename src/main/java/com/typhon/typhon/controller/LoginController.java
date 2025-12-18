package com.typhon.typhon.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.typhon.typhon.config.security.SecurityUserDetail;
import com.typhon.typhon.entity.User;
import com.typhon.typhon.util.JwtUtil;
import com.typhon.typhon.util.R;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;

import static com.typhon.typhon.entity.table.UserTableDef.USER;

@RestController
@RequestMapping("login")
public class LoginController {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JedisPool jedisPool;

    public static class LoginUser {
        public String loginStr;
        public String password;
    }

    @PostMapping
    public R login(@RequestBody LoginUser loginUser) {
        if (loginUser.loginStr.isEmpty() || loginUser.password.isEmpty()) {
            return R.ok("(用户/邮箱)或密码不能为空");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        loginUser.loginStr,
                        loginUser.password
                );
        Authentication authentication = authenticationManager
                .authenticate(authenticationToken);
        if (Objects.isNull(authentication)) {
            return R.error("用户名或密码错误");
        }
        SecurityUserDetail securityUserDetail = (SecurityUserDetail) authentication.getPrincipal();
        String jwt = JwtUtil.createToken(loginUser.loginStr);
        jedisPool.getResource().set(
                "jwt:" + loginUser.loginStr,
                JSON.toJSONString(securityUserDetail),
                SetParams.setParams()
                        .ex(30 * 24 * 60 * 60)
        );

        User user = User.create()
                .where(
                        USER.ACCOUNT.eq(loginUser.loginStr)
                                .or(
                                        USER.EMAIL.eq(loginUser.loginStr)
                                )
                )
                .one();
        user.setPassword(null);
        JSONObject rData = new JSONObject();
        rData.put("token", jwt);
        rData.put(
                "expires",
                System.currentTimeMillis() +
                        (
                                jedisPool.getResource().ttl("jwt:" + loginUser.loginStr)
                        ) * 1000
        );
        rData.put("data", user);
        return R.ok(rData);
    }
}
