package com.akshay.student_service.controller;

import com.akshay.student_service.model.Course;
import com.akshay.student_service.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/add")
    public Course addCourse(@RequestBody Course course) {
        return courseService.addCourse(course);
    }

    @GetMapping("/all")
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{courseCode}")
    public Course getCourseByCourseCode(@PathVariable String courseCode) {
        return courseService.getCourseByCourseCode(courseCode);
    }
}
