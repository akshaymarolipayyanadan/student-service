package com.akshay.student_service.repository;

import com.akshay.student_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Student findByUsername(String username);

    Student findByEmail(String email);

    Optional<Student> findByStudentId(String studentId);


}
