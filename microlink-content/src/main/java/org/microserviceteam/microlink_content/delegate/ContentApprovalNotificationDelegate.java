package org.microserviceteam.microlink_content.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.microserviceteam.microlink_content.client.UserDTO;
import org.microserviceteam.microlink_content.client.UserServiceClient;
import org.microserviceteam.microlink_content.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContentApprovalNotificationDelegate implements JavaDelegate {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public void execute(DelegateExecution execution) {
        Boolean approved = (Boolean) execution.getVariable("approved");
        String authorId = (String) execution.getVariable("authorId");
        Long contentId = (Long) execution.getVariable("contentId");
        
        UserDTO user = userServiceClient.getUser(authorId);
        if (user == null || user.getEmail() == null) {
            return;
        }

        if (approved != null && approved) {
            emailService.sendSimpleMessage(user.getEmail(), 
                "内容审批通过", 
                "亲爱的 " + user.getUsername() + "，您的内容 (ID: " + contentId + ") 已通过审批并发布！");
        } else {
            emailService.sendSimpleMessage(user.getEmail(), 
                "内容审批拒绝", 
                "亲爱的 " + user.getUsername() + "，很抱歉，您的内容 (ID: " + contentId + ") 审批未通过。");
        }
    }
}
