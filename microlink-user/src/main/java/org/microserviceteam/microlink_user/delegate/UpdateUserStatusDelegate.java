package org.microserviceteam.microlink_user.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.microserviceteam.microlink_user.model.User;
import org.microserviceteam.microlink_user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("updateUserStatusDelegate")
public class UpdateUserStatusDelegate implements JavaDelegate {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Executing UpdateUserStatusDelegate...");
        String userIdStr = execution.getProcessInstanceBusinessKey();
        System.out.println("BusinessKey (User ID): " + userIdStr);
        if (userIdStr != null) {
            Long userId = Long.valueOf(userIdStr);
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
