package com.boaz.ticketflow.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	"com.boaz.ticketflow.common.security", 
	"com.boaz.ticketflow.ticket"
	//"com.boaz.ticketflow.ticket.interfaces.mapper"
})
public class TicketServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketServiceApplication.class, args);
	}

}

