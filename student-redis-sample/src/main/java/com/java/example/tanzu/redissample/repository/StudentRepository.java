package com.java.example.tanzu.redissample.repository;

import org.springframework.data.repository.CrudRepository;

public interface StudentRepository extends CrudRepository<Student, String>
{

}
