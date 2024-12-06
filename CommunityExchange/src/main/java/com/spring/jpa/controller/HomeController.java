package com.spring.jpa.controller;

import com.spring.jpa.model.User;
import com.spring.jpa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

//    @PostMapping("/login")
//    public String login(User user, Model model) {
//        // Retrieve the user from the database
//        User dbUser = userRepository.findById(user.getUsername()).orElse(null);
//
//        if (dbUser != null && dbUser.getPassword().equals(user.getPassword())) {
//            // Credentials are valid, redirect to the dashboard
//            return "redirect:/dashboard";
//        } else {
//            // Invalid credentials, show error
//            model.addAttribute("error", "Invalid credentials");
//            return "login";
//        }
//    }
    
    @PostMapping("/login")
    public String login(User user, Model model) {
        System.out.println("User submitted: " + user.getUsername() + " with password: " + user.getPassword());

        // Attempt to retrieve the user from the database
        User dbUser = userRepository.findByUsername(user.getUsername());

        // Print the user fetched from DB and password comparison
        if (dbUser != null) {
            System.out.println("User found in DB: " + dbUser.getUsername() + " with password: " + dbUser.getPassword());
        } else {
            System.out.println("User not found in DB.");
        }

        // Compare passwords
        if (dbUser != null && dbUser.getPassword().equals(user.getPassword())) {
            System.out.println("Password matches, login successful.");
            return "redirect:/dashboard"; // Redirect to the dashboard
        } else {
            System.out.println("Invalid credentials or password mismatch.");
            model.addAttribute("error", "Invalid credentials");
            return "login"; // Stay on the login page
        }
    }


    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard";
    }

    @GetMapping("/request-service")
    public String requestService() {
        return "request-service";
    }

    @GetMapping("/provide-service")
    public String provideService() {
        return "provide-service";
    }
}
