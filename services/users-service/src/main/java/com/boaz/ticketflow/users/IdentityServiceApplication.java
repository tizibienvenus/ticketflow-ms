package com.boaz.ticketflow.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	"com.boaz.ticketflow.common.security", 
	"com.boaz.ticketflow.users", 
	//"com.boaz.ticketflow.users.domain", 
	//"com.boaz.ticketflow.users.infrastructure", 
	//"com.boaz.ticketflow.users.config", 
	//"com.boaz.ticketflow.users.interfaces"
})
public class IdentityServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityServiceApplication.class, args);
	}

}