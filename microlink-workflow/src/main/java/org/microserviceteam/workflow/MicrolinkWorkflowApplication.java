package org.microserviceteam.workflow;

import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "org.microserviceteam.workflow.client")
class MicrolinkWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicrolinkWorkflowApplication.class, args);
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessStartedEvent> processStartedListener() {
        return processStartedEvent -> {
        };
    }
}
