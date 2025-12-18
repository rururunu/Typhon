package com.typhon.typhon.controller;

import com.alibaba.fastjson2.JSONObject;
import com.typhon.typhon.entity.User;
import com.typhon.typhon.util.QQEmails;
import com.typhon.typhon.util.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.security.SecureRandom;
import java.util.Objects;

import static com.typhon.typhon.entity.table.UserTableDef.USER;

@RestController
@RequestMapping("sign")
@Slf4j
public class SignController {

    @Resource
    private JedisPool jedisPool;

    public static class SignUser {
        public String email;
        public String name;
        public String account;
        public String password;
        public Integer code;
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
            if (signUser.code == null) {
                return R.error("验证码不能为空");
            }
            String codeKey = "verifyCode:email:" + signUser.email;
            String codeJsonStr = jedisPool.getResource().get(codeKey);
            if (codeJsonStr == null || codeJsonStr.isEmpty()) {
                return R.error("验证码已过期");
            }
            JSONObject codeJson = JSONObject.parse(codeJsonStr);
            if (codeJson.getInteger("attempts") >= 5) {
                jedisPool.getResource().del(codeKey);
                return R.error("尝试 5 次错误,请重新获取验证码");
            }
            if (!Objects.equals(signUser.code, codeJson.getInteger("code"))) {
                return R.error("验证码错误");
            }
            if (
                    User.create()
                            .where(User::getAccount)
                            .eq(signUser.account)
                            .or(
                                    USER.EMAIL.eq(signUser.email)
                            )
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
                jedisPool.getResource().del(codeKey);
                return R.ok("注册成功");
            } else {
                return R.error("注册失败");
            }
        } catch (Exception e) {
            log.error("注册失败", e);
            return R.error("注册失败", e.getMessage());
        }
    }

    @GetMapping("getCode/{email}")
    public R getCode(@PathVariable String email) {
        if (email.isEmpty()) {
            return R.error("邮箱不能为空");
        }
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1000000); // 0~999999
        String verifyCode = String.format("%06d", code);
        try {
            QQEmails.plusEmail(
                    "Typhon 注册码",
                    buildEmailHtml(verifyCode),
                    email
            );
            JSONObject codeJson = new JSONObject();
            codeJson.put("code", verifyCode);
            codeJson.put("attempts", 0);
            jedisPool.getResource().set(
                    "verifyCode:email:" + email,
                    codeJson.toString(),
                    SetParams.setParams()
                            .ex(300)
            );
            return R.ok("获取验证码成功请查看邮箱");
        } catch (Exception e) {
            log.error("获取验证码失败", e);
            return R.error("获取验证码失败");
        }
    }

    private String buildEmailHtml(String verifyCode) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Typhon 验证码</title>
                    <style>
                        /* 基础重置 */
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
                            background-color: #000;
                            line-height: 1.6;
                            color: #fff;
                            margin: 0;
                            padding: 0;
                        }
                
                        /* 邮件容器 */
                        .email-container {
                            max-width: 600px;
                            margin: 0 auto;
                            background: #000;
                        }
                
                        /* 头部 */
                        .email-header {
                            background: #000;
                            color: white;
                            padding: 50px 30px 30px;
                            text-align: center;
                            border-bottom: 1px solid #333;
                        }
                
                        .logo {
                            font-size: 42px;
                            font-weight: 900;
                            letter-spacing: 6px;
                            text-transform: uppercase;
                            margin-bottom: 10px;
                            color: #fff;
                            text-shadow: 0 2px 10px rgba(255, 255, 255, 0.1);
                        }
                
                        .tagline {
                            font-size: 13px;
                            letter-spacing: 4px;
                            margin-top: 5px;
                            color: #aaa;
                            text-transform: uppercase;
                        }
                
                        /* 内容区 */
                        .email-content {
                            padding: 40px 30px;
                        }
                
                        .welcome-text {
                            font-size: 20px;
                            margin-bottom: 30px;
                            color: #fff;
                            font-weight: 300;
                            text-align: center;
                        }
                
                        /* 验证码卡片 */
                        .code-card {
                            background: #111;
                            border-radius: 8px;
                            padding: 40px 30px;
                            text-align: center;
                            margin: 40px 0;
                            border: 1px solid #333;
                            position: relative;
                        }
                
                        .code-label {
                            font-size: 12px;
                            color: #888;
                            margin-bottom: 15px;
                            letter-spacing: 2px;
                            text-transform: uppercase;
                            font-weight: 600;
                        }
                
                        .verification-code {
                            font-size: 56px;
                            font-weight: 700;
                            letter-spacing: 12px;
                            color: #fff;
                            margin: 25px 0;
                            font-family: 'Courier New', monospace;
                            text-shadow: 0 0 20px rgba(255, 255, 255, 0.2);
                        }
                
                        .expire-time {
                            font-size: 14px;
                            color: #888;
                            margin-top: 20px;
                        }
                
                        .expire-time strong {
                            color: #fff;
                            font-weight: 600;
                        }
                
                        /* 分割线 */
                        .divider {
                            height: 1px;
                            background: linear-gradient(90deg, transparent, #333, transparent);
                            margin: 40px 0;
                        }
                
                        /* 安全提示 */
                        .security-tips {
                            background: #111;
                            padding: 25px;
                            margin: 30px 0;
                            border-radius: 6px;
                            border-left: 3px solid #fff;
                        }
                
                        .security-tips h3 {
                            color: #fff;
                            margin-bottom: 20px;
                            font-size: 16px;
                            font-weight: 600;
                            display: flex;
                            align-items: center;
                            gap: 10px;
                        }
                
                        .security-tips ul {
                            list-style: none;
                            padding-left: 0;
                        }
                
                        .security-tips li {
                            margin-bottom: 12px;
                            padding-left: 24px;
                            position: relative;
                            color: #aaa;
                            line-height: 1.5;
                        }
                
                        .security-tips li::before {
                            content: "—";
                            color: #fff;
                            position: absolute;
                            left: 0;
                        }
                
                        /* 提示文本 */
                        .hint-text {
                            background: #111;
                            padding: 20px;
                            border-radius: 6px;
                            margin: 30px 0;
                            text-align: center;
                            color: #aaa;
                            font-size: 14px;
                            border: 1px solid #222;
                        }
                
                        .hint-text strong {
                            color: #fff;
                            font-weight: 600;
                        }
                
                        /* 脚部 */
                        .email-footer {
                            background: #000;
                            color: #666;
                            padding: 40px 30px;
                            text-align: center;
                            font-size: 12px;
                            border-top: 1px solid #333;
                        }
                
                        .footer-links {
                            margin: 25px 0;
                            display: flex;
                            justify-content: center;
                            gap: 30px;
                        }
                
                        .footer-links a {
                            color: #888;
                            text-decoration: none;
                            transition: color 0.3s ease;
                            font-size: 11px;
                            letter-spacing: 1px;
                            text-transform: uppercase;
                        }
                
                        .footer-links a:hover {
                            color: #fff;
                        }
                
                        .copyright {
                            margin-top: 25px;
                            color: #555;
                            line-height: 1.8;
                        }
                
                        .highlight {
                            color: #fff;
                            font-weight: 600;
                        }
                
                        /* 响应式设计 */
                        @media (max-width: 600px) {
                            .email-header, .email-content, .email-footer {
                                padding: 30px 20px;
                            }
                
                            .logo {
                                font-size: 32px;
                                letter-spacing: 4px;
                            }
                
                            .verification-code {
                                font-size: 44px;
                                letter-spacing: 8px;
                            }
                
                            .code-card {
                                padding: 30px 20px;
                            }
                
                            .footer-links {
                                flex-direction: column;
                                gap: 15px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <!-- 头部 -->
                        <div class="email-header">
                            <div class="logo">TYPHON</div>
                            <div class="tagline">VERIFICATION CODE</div>
                        </div>
                
                        <!-- 内容 -->
                        <div class="email-content">
                            <h1 class="welcome-text">您的注册验证码</h1>
                
                            <!-- 验证码卡片 -->
                            <div class="code-card">
                                <div class="code-label">Verification Code</div>
                                <div class="verification-code">%s</div>
                                <div class="expire-time">有效期：<strong>10分钟</strong></div>
                            </div>
                
                            <!-- 使用提示 -->
                            <div class="hint-text">
                                请复制上方验证码并返回注册页面完成验证
                            </div>
                
                            <!-- 分割线 -->
                            <div class="divider"></div>
                
                            <!-- 安全提示 -->
                            <div class="security-tips">
                                <h3>⚠️ 安全须知</h3>
                                <ul>
                                    <li>此验证码仅用于 Typhon 账号注册，请勿用于其他用途</li>
                                    <li>请不要与任何人分享此验证码，包括 Typhon 客服人员</li>
                                    <li>验证码将在 10 分钟后自动失效，请及时使用</li>
                                    <li>如非本人操作，请立即忽略此邮件</li>
                                </ul>
                            </div>
                
                            <!-- 提示文本 -->
                            <div class="hint-text">
                                <strong>重要：</strong>请勿回复此邮件。如需帮助，请访问我们的官方网站。
                            </div>
                        </div>
                
                        <!-- 脚部 -->
                        <div class="email-footer">
                            <div class="footer-links">
                                <a href="#">官方支持</a>
                                <a href="#">隐私政策</a>
                                <a href="#">服务条款</a>
                                <a href="#">安全中心</a>
                            </div>
                
                            <div class="copyright">
                                © 2023 <span class="highlight">TYPHON</span> SYSTEM<br>
                                <span style="color: #444;">此邮件由系统自动生成，请勿回复</span><br>
                                <span style="color: #444;">Digital Identity Verification</span>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """, verifyCode);
    }
}
