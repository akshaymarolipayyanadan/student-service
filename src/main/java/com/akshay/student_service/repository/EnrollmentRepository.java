package com.akshay.student_service.repository;

import com.akshay.student_service.model.Course;
import com.akshay.student_service.model.Enrollment;
import com.akshay.student_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentIdString(String studentIdString);
    boolean existsByStudentAndCourse(Student student, Course course);
    Optional<Enrollment> findByStudentIdStringAndCourseCode(String studentIdString, String courseCode);


}
