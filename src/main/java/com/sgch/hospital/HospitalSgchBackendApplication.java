package com.sgch.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration; // Importación añadida
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = ThymeleafAutoConfiguration.class) // Excluir autoconfiguración de Thymeleaf
@EnableAsync
public class HospitalSgchBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HospitalSgchBackendApplication.class, args);
	}

}
