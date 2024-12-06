package com.spring.jpa.controller;

import com.spring.jpa.model.Service;
import com.spring.jpa.model.ServiceRequest;
import com.spring.jpa.model.User;
import com.spring.jpa.repository.ServiceRepository;
import com.spring.jpa.repository.ServiceRequestRepository;
import com.spring.jpa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

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
        // Get the logged-in user (using a placeholder username here for now)
        User user = userRepository.findByUsername("pass");  // Replace with actual logged-in user logic

        if (user != null) {
            // Fetch all the services provided by the logged-in user
            model.addAttribute("services", serviceRepository.findByUser(user));
        }

        return "provide-service";  // Return the provide-service view
    }
    
 // Handle service approval (for service requests)
    @PostMapping("/approve-service/{serviceId}")
    public String approveService(@PathVariable Long serviceId, Model model) {
        // Find the service by serviceId
        Service service = serviceRepository.findById(serviceId).orElse(null);
        
        if (service != null) {
            // Assuming you have the logged-in user (replace this with actual logic)
            User loggedInUser = userRepository.findByUsername("pass");  // Replace with actual logged-in user logic

            if (loggedInUser != null) {
                // Find the pending service request for this service and user
                ServiceRequest serviceRequest = serviceRequestRepository
                        .findByServiceAndUserAndStatus(service, loggedInUser, ServiceRequest.Status.PENDING);
                
                if (serviceRequest != null) {
                    // Update the status to ACCEPTED
                    serviceRequest.setStatus(ServiceRequest.Status.ACCEPTED);
                    serviceRequestRepository.save(serviceRequest);  // Save the updated service request
                }
            }
        }

        return "redirect:/provide-service";  // Redirect back to the Provide Service page
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
    
    @PostMapping("/request-service/{serviceId}")
    public String requestService(@PathVariable Long serviceId, Model model) {
        // Handle the logic for requesting the service, e.g., create a service request for the logged-in user
        // You can fetch the service by its ID and process the request

        Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service != null) {
            // Logic to handle the request, e.g., saving a request to the database
            // You could save this request in a service request entity or process as needed
            System.out.println("Service requested: " + service.getTitle());
        }

        return "redirect:/request-service";  // Redirect back to the Request Service page
    }

    
}
