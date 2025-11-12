package com.smile.blue_blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlueBlogApplication {

	public static void main(String[] args) {
		System.out.println("=== Spring Boot应用启动 ===");
		SpringApplication.run(BlueBlogApplication.class, args);
		System.out.println("Spring Boot应用启动完成");
	}

}
