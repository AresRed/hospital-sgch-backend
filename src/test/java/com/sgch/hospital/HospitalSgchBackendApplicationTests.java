package com.sgch.hospital;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class HospitalSgchBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
