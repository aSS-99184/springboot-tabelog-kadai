package com.example.samuraitabelog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

}
