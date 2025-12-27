package org.microserviceteam.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "microlink-user", path = "/api/user")
public interface UserClient {

    @PutMapping("/status/{id}")
    ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}
