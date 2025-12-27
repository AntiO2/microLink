package org.microserviceteam.microlink_content.workflow;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.engine.test.Deployment;
import org.flowable.spring.impl.test.FlowableSpringExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microserviceteam.microlink_content.model.Content;
import org.microserviceteam.microlink_content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.microserviceteam.microlink_content.service.EmailService;
import org.microserviceteam.microlink_content.client.UserServiceClient;
import org.microserviceteam.microlink_content.client.UserDTO;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(FlowableSpringExtension.class)
public class ContentWorkflowTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @MockBean
    private ContentService contentService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    public void testFullSuccessWorkflow() {
        // 1. Prepare Data
        String authorId = "testUser";
        Long contentId = 1L;
        
        UserDTO mockUser = new UserDTO();
        mockUser.setId(100L);
        mockUser.setUsername("testUser");
        mockUser.setEmail("test@example.com");
        when(userServiceClient.getUser(authorId)).thenReturn(mockUser);
        when(contentService.checkContent(contentId)).thenReturn(true);

        // 2. Start Process
        Map<String, Object> variables = new HashMap<>();
        variables.put("contentId", contentId);
        variables.put("authorId", authorId);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("content-publish-process-v2", variables);
        assertNotNull(processInstance);

        // 3. Check for Admin Approval Task
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey("adminApprovalTask")
                .singleResult();
        assertNotNull(task);
        assertEquals("admin", task.getAssignee());

        // 4. Complete Admin Approval Task (Approved)
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("approved", true);
        taskService.complete(task.getId(), taskVariables);

        // 5. Verify Workflow Finished
        long count = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstance.getId())
                .count();
        assertEquals(0, count);

        // 6. Verify Notifications and Status (Status update is mockable or check DB if using real service)
        verify(emailService, times(1)).sendSimpleMessage(eq("test@example.com"), contains("通过"), anyString());
    }

    @Test
    public void testAutoRejectWorkflow() {
        // 1. Prepare Data
        Long contentId = 2L;
        String authorId = "userReject";
        when(contentService.checkContent(contentId)).thenReturn(false);
        
        UserDTO mockUser = new UserDTO();
        mockUser.setEmail("reject@example.com");
        mockUser.setUsername("userReject");
        when(userServiceClient.getUser(authorId)).thenReturn(mockUser);

        // 2. Start Process
        Map<String, Object> variables = new HashMap<>();
        variables.put("contentId", contentId);
        variables.put("authorId", authorId);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("content-publish-process-v2", variables);
        
        // Verify Workflow Finished immediately
        long count = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstance.getId())
                .count();
        assertEquals(0, count);

        // Verify Rejection Notification
        verify(emailService, times(1)).sendSimpleMessage(eq("reject@example.com"), contains("拒绝"), anyString());
    }
}
