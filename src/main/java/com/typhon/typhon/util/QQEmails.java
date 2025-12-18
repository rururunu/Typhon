package com.typhon.typhon.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.Properties;

/**
 * QQ邮箱
 *
 * @author GuoLv
 */
public class QQEmails {

    public static void plusEmail(String title, String content, String email) throws Exception {

        Properties properties = new Properties();

        properties.put("mail.transport.protocol", "smtp"); // 连接协议

        properties.put("mail.smtp.host", "smtp.qq.com"); // 主机名

        properties.put("mail.smtp.port", 465); // 端口号

        properties.put("mail.smtp.auth", "true");

        properties.put("mail.smtp.ssl.enable", "true"); // 设置是否使用ssl安全连接 --- 一般都使用

        properties.put("mail.debug", "true"); // 设置是否显示 debug 信息 true 会在控制台显示相关信息

        // 得到回话对象

        Session session = Session.getInstance(properties);

        // 获取邮件对象
        Message message = new MimeMessage(session);

        // 设置发件人邮箱地址 *

        message.setFrom(new InternetAddress("guolvaita@qq.com"));

        // 设置收件人地址*
        message.setRecipients(
                MimeMessage.RecipientType.TO,
                new InternetAddress[]{
                        new InternetAddress(email)
                }
        );

        // 设置邮件标题 *

        message.setSubject(title);

        // 设置邮件内容 *
        BodyPart body = new MimeBodyPart();
        body.setContent(content, "text/html;charset=utf-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(body);
        message.setContent(multipart); // 为传过来的值
        // 得到邮差对象

        Transport transport = session.getTransport();

        // 连接自己的邮箱账户 *

        transport.connect("guolvaita@qq.com", "qnwnxzxbotjzdfbh");// 密码为刚才得到的授权码

        // 发送邮件
        transport.sendMessage(message, message.getAllRecipients());
    }
}
