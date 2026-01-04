package org.microserviceteam.microlink_user;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public RuntimeService runtimeService() {
        return Mockito.mock(RuntimeService.class);
    }

    @Bean
    @Primary
    public TaskService taskService() {
        return Mockito.mock(TaskService.class);
    }
}
