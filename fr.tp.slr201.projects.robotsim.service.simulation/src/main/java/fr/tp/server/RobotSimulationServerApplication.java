package fr.tp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class RobotSimulationServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(RobotSimulationServerApplication.class, args);
	}

}
