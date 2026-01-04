package org.microserviceteam.microlink_user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendSimpleMessage(String to, String subject, String text) {
        logger.info("=========================================");
        logger.info("模拟发送邮件通知:");
        logger.info("收件人: {}", to);
        logger.info("主题: {}", subject);
        logger.info("内容: {}", text);
        logger.info("=========================================");
    }
}
