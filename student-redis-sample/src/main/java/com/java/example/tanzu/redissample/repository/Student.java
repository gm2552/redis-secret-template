package com.java.example.tanzu.redissample.repository;

import java.io.Serializable;

import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RedisHash("Student")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student implements Serializable
{
    private static final long serialVersionUID = -2412738947803366031L;
    
	public enum Gender { 
        MALE, FEMALE
    }

    private String id;
    private String name;
    private Gender gender;
    private int grade;
}
