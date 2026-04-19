  package com.camergo.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.boaz.ticketflow.common.security",
		"com.boaz.ticketflow.common.ws",
		"com.boaz.ticketflow.common.firebase",
		"com.boaz.ticketflow.common.wrappers",
		"com.boaz.ticketflow.common.exceptions",
		"com.boaz.ticketflow.common.utils",
		"com.camergo"
		//"com.camergo.notification.factory",
		//"com.camergo.notification.channel",
		//"com.camergo.notification.kafka",
		//"com.camergo.notification.application",
		//"com.camergo.notification.domain",
		//"com.camergo.notification.infrastructure",
		//"com.camergo.notification.config",
		//"com.camergo.notification.interfaces"
	}
)
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}
}
