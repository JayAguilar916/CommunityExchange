package com.spring.jpa.controller;

import com.spring.jpa.model.Service;
import com.spring.jpa.model.User;
import com.spring.jpa.repository.ServiceRepository;
import com.spring.jpa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    // Show login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";  // Show login page
    }

    // Handle login POST request
    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password) {
        // Add your login logic here (replace with real login logic)
        User user = userRepository.findByUsername(username);  // Find user by username
        
        if (user != null && user.getPassword().equals(password)) {  // Check password
            return "redirect:/dashboard";  // Redirect to dashboard after successful login
        } else {
            return "login";  // Stay on login page if credentials are invalid
        }
    }

    // Show dashboard page with links to "Provide Service" and "Request Service"
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        return "dashboard";  // Show dashboard page
    }

    // Show the Provide Service page and list the services the user offers
    @GetMapping("/provide-service")
    public String showProvideServicePage(Model model) {
        // Assuming you have a way to identify the logged-in user
        User user = userRepository.findByUsername("pass"); // Replace with actual user lookup (e.g., from session or security context)

        if (user != null) {
            // Get the list of services the user offers
            model.addAttribute("services", serviceRepository.findByUser(user));
        }

        return "provide-service";  // Return the provide-service view
    }

    // Show the Create Service page (form) - GET request handler
    @GetMapping("/create-service")
    public String showCreateServiceForm(Model model) {
        // Add any necessary model attributes for the create service page (if needed)
        return "create-service";  // Return the view name for creating a new service
    }
    
    // Handle service creation (for adding new services)
    @PostMapping("/create-service")
    public String createService(@RequestParam String title, @RequestParam String description, @RequestParam int points, Model model) {
        // Assuming the user is logged in (replace with actual user lookup)
        User user = userRepository.findByUsername("pass");

        if (user != null) {
            Service service = new Service();
            service.setTitle(title);
            service.setDescription(description);
            service.setPoints(points);
            service.setUser(user);  // Associate the service with the logged-in user

            serviceRepository.save(service);  // Save the service to the database
        }

        return "redirect:/provide-service";  // Redirect back to the Provide Service page to display the new list
    }

    // Show the Request Service page and list available services
    @GetMapping("/request-service")
    public String showRequestServicePage(Model model) {
        model.addAttribute("services", serviceRepository.findAll());  // Example: showing all services
        return "request-service";  // Return the view name (request-service.html)
    }
}
