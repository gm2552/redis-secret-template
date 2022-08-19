package com.java.example.tanzu.redissample.resources;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.example.tanzu.redissample.repository.Student;
import com.java.example.tanzu.redissample.repository.StudentRepository;

@RestController
@RequestMapping("student")
public class StudentResource 
{
	@Autowired
	protected StudentRepository studentRepository;
	
	@GetMapping
	public Iterable<Student> getAllStudents()
	{
		return studentRepository.findAll();
	}
}
