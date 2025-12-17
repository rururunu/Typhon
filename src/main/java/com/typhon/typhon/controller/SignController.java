package com.typhon.typhon.controller;

import com.typhon.typhon.entity.User;
import com.typhon.typhon.util.R;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sign")
public class SignController {

    private static class SignUser {
        public String email;
        public String name;
        public String account;
        public String password;
    }

    @PostMapping
    public R sign(@RequestBody SignUser signUser) {
        try {
            if (signUser.email.isEmpty()) {
                return R.error("邮箱不能为空");
            }
            if (signUser.account.isEmpty() || signUser.password.isEmpty()) {
                return R.error("账号密码不能为空");
            }
            if (signUser.name.isEmpty()) {
                return R.error("用户名不能为空");
            }
            if (User.create()
                    .where(User::getAccount)
                    .eq(signUser.account)
                    .one() != null
            ) {
                return R.error("用户已存在");
            }
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (
                    User.create()
                            .setAccount(signUser.account)
                            .setPassword(encoder.encode(signUser.password))
                            .setEmail(signUser.email)
                            .setName(signUser.name)
                            .save()
            ) {
                return R.ok("创建成功");
            } else {
                return R.error("创建失败");
            }
        } catch (Exception e) {
            return R.error("创建失败", e.getMessage());
        }
    }

}
