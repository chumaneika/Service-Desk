package com.bachelor.service_desk;

import com.bachelor.service_desk.config.HostingEnvironment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceDeskApplication {

	public static void main(String[] args) {
		HostingEnvironment.configureDatabaseUrl();
		SpringApplication.run(ServiceDeskApplication.class, args);
	}

}
