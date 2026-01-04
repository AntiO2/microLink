package org.microserviceteam.microlink_user.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.microserviceteam.microlink_user.model.User;
import org.microserviceteam.microlink_user.repository.UserRepository;
import org.microserviceteam.microlink_user.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserRejectedDelegate implements JavaDelegate {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void execute(DelegateExecution execution) {
        String username = (String) execution.getVariable("applicant");
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setStatus("REJECTED");
            userRepository.save(user);
            emailService.sendSimpleMessage(user.getEmail(), 
                "注册审批拒绝", 
                "亲爱的 " + user.getNickname() + "，很抱歉，您的 MicroLink 账号注册申请已被拒绝。");
        }
    }
}
