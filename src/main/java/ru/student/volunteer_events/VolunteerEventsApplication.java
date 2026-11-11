package ru.student.volunteer_events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VolunteerEventsApplication {

	public static void main(String[] args) {
		SpringApplication.run(VolunteerEventsApplication.class, args);
	}
}
