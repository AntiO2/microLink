package org.microserviceteam.social;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.microserviceteam.social.mapper")
public class MicrolinkSocialApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicrolinkSocialApplication.class, args);
	}

}
