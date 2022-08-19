package com.java.example.tanzu.redissample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.java.example.tanzu.redissample.repository.Student;
import com.java.example.tanzu.redissample.repository.StudentRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StudentOpsRunner implements CommandLineRunner
{
	@Autowired
	protected StudentRepository studentRepository;
	
	@Override
	public void run(String... args) throws Exception 
	{
		log.info("Creating and saving student.");
		
		var student = new Student(
				  "Eng2015001", "John Doe", Student.Gender.MALE, 1);
				studentRepository.save(student);
				
		studentRepository.save(student);		
		
		
	}
	
}
