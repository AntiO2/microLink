package org.microserviceteam.microlink_user.listener;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.microserviceteam.microlink_user.model.User;
import org.microserviceteam.microlink_user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("updateUserStatusListener")
public class UpdateUserStatusListener implements TaskListener {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("Executing UpdateUserStatusListener for task: " + delegateTask.getName());
        
        // Try multiple ways to get the user ID
        Object userIdObj = delegateTask.getVariable("userId");
        System.out.println("Variable (userId): " + userIdObj);
        
        if (userIdObj == null) {
            String businessKey = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(delegateTask.getProcessInstanceId())
                    .singleResult()
                    .getBusinessKey();
            System.out.println("Fallback to BusinessKey from RuntimeService: " + businessKey);
            if (businessKey != null) {
                userIdObj = Long.valueOf(businessKey);
            }
        }
        
        if (userIdObj != null) {
            Long userId;
            if (userIdObj instanceof Long) {
                userId = (Long) userIdObj;
            } else {
                userId = Long.valueOf(userIdObj.toString());
            }
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setStatus("ACTIVE");
                userRepository.save(user);
                System.out.println("User status updated to ACTIVE for user ID: " + userId);
            } else {
                System.out.println("User not found for ID: " + userId);
            }
        } else {
            System.out.println("No BusinessKey found in process instance!");
        }
    }
}
