package com.demo.FHIR;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.demo.FHIR.controller.EligibilityController;

@SpringBootApplication
@ComponentScan(basePackageClasses = EligibilityController.class)
public class FhirApplication {

	public static void main(String[] args) {
		SpringApplication.run(FhirApplication.class, args);
		System.out.println("**********FHIR Service started ************** ");

	}
	
}
