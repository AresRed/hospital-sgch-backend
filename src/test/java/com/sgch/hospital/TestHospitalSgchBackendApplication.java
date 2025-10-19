package com.sgch.hospital;

import org.springframework.boot.SpringApplication;

public class TestHospitalSgchBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(HospitalSgchBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
