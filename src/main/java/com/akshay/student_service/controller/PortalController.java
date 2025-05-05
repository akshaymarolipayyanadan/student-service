package com.akshay.student_service.controller;

import com.akshay.student_service.dto.Invoice;
import com.akshay.student_service.dto.StudentProfileDTO;
import com.akshay.student_service.model.Course;
import com.akshay.student_service.model.Enrollment;
import com.akshay.student_service.model.Student;
import com.akshay.student_service.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class PortalController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private RestTemplate restTemplate;


    @GetMapping("/")
    public String redirectBasedOnSession(HttpSession session) {
        return (session.getAttribute("student") != null) ? "redirect:/home" : "redirect:/login";
    }
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("student");
        if (student == null) {
            return "redirect:/login";
        }

        StudentProfileDTO profile = studentService.getStudentProfile(student.getStudentId());
        model.addAttribute("profile", profile);
        return "profile";
    }



    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        try {
            Student student = studentService.loginStudent(username, password);
            session.setAttribute("student", student);
            return "redirect:/home";
        } catch (RuntimeException e) {
            session.setAttribute("error", "Invalid credentials");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerStudent(@RequestParam String firstName,
                                  @RequestParam String lastName,
                                  @RequestParam String email,
                                  @RequestParam String username,
                                  @RequestParam String password,
                                  HttpSession session) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setUsername(username);
        student.setPassword(password);

        Student saved = studentService.registerStudent(student);
        session.setAttribute("student", saved);
        return "redirect:/home";
    }

//    @GetMapping("/home")
//    public String home(HttpSession session, Model model) {
//        Student student = (Student) session.getAttribute("student");
//        if (student == null) {
//            return "redirect:/login";
//        }
//        model.addAttribute("username", student.getUsername());
//        return "home";
//    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("student");

        if (student != null) {
            model.addAttribute("username", student.getFirstName());

            StudentProfileDTO profile = studentService.getStudentProfile(student.getStudentId());
            boolean hasEnrollments = profile.getEnrolledCourses() != null && !profile.getEnrolledCourses().isEmpty();
            System.out.println("hasEnrollments: " + hasEnrollments);

            session.setAttribute("hasEnrollments", hasEnrollments);
            model.addAttribute("hasEnrollments", hasEnrollments);
        }

        return "home";
    }

    @GetMapping("/courses")
    public String showCourses(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("student");
        if (student == null) {
            return "redirect:/login";
        }

        String url = "http://localhost:8080/courses/all";
        Course[] courses = restTemplate.getForObject(url, Course[].class);

        model.addAttribute("courses", courses);
        model.addAttribute("studentId", student.getStudentId());
        return "courses";
    }



    @PostMapping("/enroll")
    public String enrollInCourse(@RequestParam String courseCode,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Student student = (Student) session.getAttribute("student");
        if (student == null) {
            return "redirect:/login";
        }

        try {
            String url = "http://localhost:8080/enrollments/enroll?studentId=" + student.getStudentId()
                    + "&courseCode=" + courseCode;
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);

            String invoiceRef = (String) response.get("invoiceReference");
            redirectAttributes.addFlashAttribute("success", "Enrolled successfully! Invoice Reference: " + invoiceRef);

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            if (errorMessage.startsWith("\"") && errorMessage.endsWith("\"")) {
                errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
            }
            redirectAttributes.addFlashAttribute("error", "Enrollment failed: " + errorMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Enrollment failed due to unexpected error.");
        }

        return "redirect:/courses";

    }

    @GetMapping("/enrollments")
    public String showEnrollments(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("student");
        if (student == null) {
            return "redirect:/login";
        }


        if (student != null) {
            model.addAttribute("username", student.getFirstName());

            StudentProfileDTO profile = studentService.getStudentProfile(student.getStudentId());
            boolean hasEnrollments = profile.getEnrolledCourses() != null && !profile.getEnrolledCourses().isEmpty();
            System.out.println("hasEnrollments: " + hasEnrollments);

            session.setAttribute("hasEnrollments", hasEnrollments);
            model.addAttribute("hasEnrollments", hasEnrollments);
        }

        String enrollmentsUrl = "http://localhost:8080/enrollments/student/" + student.getStudentId();
        Enrollment[] enrollments = restTemplate.getForObject(enrollmentsUrl, Enrollment[].class);

        for (Enrollment e : enrollments) {
            String courseUrl = "http://localhost:8080/courses/" + e.getCourseCode();
            Course course = restTemplate.getForObject(courseUrl, Course.class);
            e.setCourse(course);
        }

        model.addAttribute("enrollments", enrollments);
        return "enrollments";
    }

    @GetMapping("/graduation")
    public String viewGraduationStatus(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("student");
        if (student == null) {
            return "redirect:/login";
        }

        String url = "http://localhost:8080/students/graduation-status/" + student.getStudentId();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        model.addAttribute("graduationStatus", response);
        return "graduation";
    }

    @PostMapping("/complete")
    public String completeCourse(@RequestParam String studentId,
                                 @RequestParam String courseCode,
                                 RedirectAttributes redirectAttributes) {
        try {
            String url = "http://localhost:8080/enrollments/complete?studentId=" + studentId + "&courseCode=" + courseCode;
            restTemplate.put(url, null);
            redirectAttributes.addFlashAttribute("success", "Course marked as completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to mark as completed.");
        }
        return "redirect:/enrollments";
    }
    @GetMapping("/invoices")
    public String showInvoices(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("student");
        if (student == null) {
            return "redirect:/login";
        }

        try {
            String url = "http://localhost:8081/invoices";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            System.out.println("Finance service response: " + response);

            Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
            if (embedded == null) {
                System.out.println("No _embedded section in response.");
                model.addAttribute("invoices", List.of());
                return "invoices";
            }

            List<Map<String, Object>> invoiceList = (List<Map<String, Object>>) embedded.get("invoiceList");
            List<Invoice> studentInvoices = new ArrayList<>();

            for (Map<String, Object> invoiceData : invoiceList) {
                String invoiceStudentId = (String) invoiceData.get("studentId");
                if (student.getStudentId().equals(invoiceStudentId)) {
                    Invoice invoice = new Invoice();
                    invoice.setReference((String) invoiceData.get("reference"));
                    invoice.setAmount(((Number) invoiceData.get("amount")).doubleValue());
                    invoice.setDueDate((String) invoiceData.get("dueDate"));
                    invoice.setType((String) invoiceData.get("type"));
                    invoice.setStatus((String) invoiceData.get("status"));
                    invoice.setStudentId(invoiceStudentId);
                    studentInvoices.add(invoice);
                }
            }

            System.out.println("Invoices matched for student: " + studentInvoices.size());
            model.addAttribute("invoices", studentInvoices);

        } catch (Exception e) {
            System.out.println("Error fetching invoices: " + e.getMessage());
            model.addAttribute("invoices", List.of());
            model.addAttribute("error", "Unable to load invoices.");
        }

        return "invoices";
    }



    @PostMapping("/invoices/pay")
    public String payInvoice(@RequestParam String reference,
                             RedirectAttributes redirectAttributes) {
        try {
            String url = "http://localhost:8081/invoices/" + reference + "/pay";
            restTemplate.put(url, null);
            redirectAttributes.addFlashAttribute("success", "Invoice " + reference + " paid successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to pay invoice: " + e.getMessage());
        }
        return "redirect:/invoices";
    }





}
