package com.example.moki_campaign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@ConfigurationPropertiesScan
@SpringBootApplication
public class MokiCampaignApplication {

	public static void main(String[] args) {
		SpringApplication.run(MokiCampaignApplication.class, args);
	}

}
