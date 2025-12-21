package cn.lizongyi.shareaccount.util;

import cn.lizongyi.shareaccount.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

@Slf4j
public class EmailUtil {

    /**
     * 发送邮件工具方法
     *
     * @param to      收件人邮箱地址
     * @param subject 邮件标题
     * @param content 邮件正文（支持换行符、空格等）
     * @param smtpHost SMTP服务器地址（例如：smtp.qq.com）
     * @param from     发件人邮箱地址
     * @param password 发件人邮箱密码或授权码
     */
    public static void sendEmail(String from, String to, String subject, String content) {
        String fromUser = StringUtils.isEmpty(from) ? Constants.EMAIL_ADMIN_ACCOUNT : from;
        String toUser = StringUtils.isEmpty(to) ? Constants.EMAIL_ADMIN_ACCOUNT : to;

        String password = "rmghwpblitsqbhib"; // 发件人邮箱授权码（非登录密码）
        String smtpHost = "smtp.qq.com"; // SMTP服务器地址（QQ邮箱为例）

        // 配置邮件属性
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true"); // 开启认证
        properties.put("mail.smtp.host", smtpHost); // SMTP服务器地址
        properties.put("mail.smtp.port", "465"); // SMTP端口（SSL协议）
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // 使用SSL

        // 创建会话对象
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromUser, password); // 认证发件人邮箱和密码
            }
        });

        try {
            // 创建邮件消息对象
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromUser)); // 设置发件人
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toUser)); // 设置收件人
            message.setSubject(subject + " - 清甜水站"); // 设置邮件标题

            // 设置邮件正文（支持换行符、空格等）
            message.setContent(content, "text/html;charset=UTF-8"); // 使用HTML格式，确保换行符和空格正常显示

            // 发送邮件
            Transport.send(message);

            log.info("邮件发送成功！");
        } catch (MessagingException e) {
            log.error("邮件发送失败：", e);
        }
    }

    public static void main(String[] args) {
        // 示例调用
        String to = Constants.EMAIL_ADMIN_ACCOUNT; // 收件人邮箱地址
        String subject = "清甜水站"; // 邮件标题
        String content = "这是一封测试邮件。<br><br>"
                + "以下是正文内容：<br>"
                + "1. 测试换行符<br>"
                + "2. 测试空格：    这里有多个空格<br>"
                + "3. 测试特殊字符：&nbsp; &lt; &gt;<br>"
                + "<br>结束。"; // 邮件正文（支持HTML标签）

        String from = Constants.EMAIL_ADMIN_ACCOUNT; // 发件人邮箱地址

        // 调用发送邮件方法
        sendEmail(from, to, subject, content);
    }
}