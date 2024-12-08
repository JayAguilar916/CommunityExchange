package com.spring.jpa.controller;

import com.spring.jpa.model.Service;
import com.spring.jpa.model.ServiceRequest;
import com.spring.jpa.model.User;
import com.spring.jpa.repository.ServiceRepository;
import com.spring.jpa.repository.ServiceRequestRepository;
import com.spring.jpa.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes("user")  // This will store the logged-in user in session
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
    
    // Handle logout request
    @PostMapping("/logout")
    public String logout(Model model) {
        model.addAttribute("user", null);  // Remove the user from the session
        return "redirect:/login";  // Redirect to the login page after logging out
    }

    // Handle login POST request
    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, Model model) {
        // Find user by username from the database
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {  // Check password directly
            model.addAttribute("user", user);  // Store the logged-in user in the model (and session)
            return "redirect:/dashboard";  // Redirect to dashboard after successful login
        } else {
            model.addAttribute("error", "Invalid credentials!");  // Show error message on failure
            return "login";  // Stay on login page if credentials are invalid
        }
    }

    // Show dashboard page with links to "Provide Service" and "Request Service"
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        return "dashboard";  // Show dashboard page
    }
    
    // Show the Create Service page (form) - GET request handler
    @GetMapping("/create-service")
    public String showCreateServiceForm(Model model) {
        return "create-service";  // Return the view name for creating a new service
    }
    
    // Handle service creation (for adding new services)
    @PostMapping("/create-service")
    public String createService(@RequestParam String title, @RequestParam String description, @RequestParam int points, Model model) {
        // Get the logged-in user dynamically from the session
        User user = (User) model.getAttribute("user");

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
        User loggedInUser = (User) model.getAttribute("user");
        
        if (loggedInUser == null) {
            return "redirect:/login";  // Redirect to login if no user is logged in
        }

        // Fetch all services that have not been requested by the logged-in user
        List<Service> services = serviceRepository.findAll();

        // Filter out services that have already been requested by the logged-in user
        services.removeIf(service -> serviceRequestRepository.findByServiceAndUser(service, loggedInUser) != null);

        model.addAttribute("services", services);  // Add the filtered services to the model
        return "request-service";  // Return the request-service view
    }
    
 // Handle service request (Userb clicks "Request" button)
    @PostMapping("/request-service/{serviceId}")
    public String requestService(@PathVariable Long serviceId, Model model) {
        // Get the service from the database using serviceId
        Service service = serviceRepository.findById(serviceId).orElse(null);

        if (service == null) {
            return "redirect:/request-service";  // Redirect back if service is not found
        }

        // Get the logged-in user (requestor)
        User user = (User) model.getAttribute("user");
        if (user == null) {
            return "redirect:/login";  // Redirect if no user is logged in
        }

        // Check if a service request already exists for this service and user
        ServiceRequest existingRequest = serviceRequestRepository.findByServiceAndUser(service, user);
        if (existingRequest != null) {
            // If a request already exists, redirect back without creating a new one
            return "redirect:/request-service";
        }

        // Create a new service request with status 'PENDING'
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setService(service);
        serviceRequest.setUser(user);
        serviceRequest.setStatus(ServiceRequest.Status.Pending);
        serviceRequest.setCreatedAt(LocalDateTime.now());

        // Save the service request to the database
        serviceRequestRepository.save(serviceRequest);

        // Redirect back to the request-service page
        return "redirect:/request-service";
    }
    
    @PostMapping("/approve-service/{serviceId}")
    public String approveService(@PathVariable Long serviceId, Model model, RedirectAttributes redirectAttributes) {
        Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) {
            return "redirect:/provide-service";  // Redirect if service is not found
        }

        User loggedInUser = (User) model.getAttribute("user");
        if (loggedInUser == null) {
            return "redirect:/provide-service";  // Redirect if no user is logged in
        }

        // Find the first pending service request for the specified service
        ServiceRequest serviceRequest = serviceRequestRepository
                .findByServiceAndStatus(service, ServiceRequest.Status.Pending);

        if (serviceRequest != null) {
            // Update the status to "ACCEPTED"
            serviceRequest.setStatus(ServiceRequest.Status.Accepted);
            serviceRequestRepository.save(serviceRequest);  // Save the updated request

            // Add a success notification message to the redirect attributes
            redirectAttributes.addFlashAttribute("notification", "Request approved successfully.");
        } else {
            // If no pending request exists, add a different notification message
            redirectAttributes.addFlashAttribute("notification", "No pending requests found.");
        }

        return "redirect:/provide-service";  // Redirect back to the provider's service page
    }

    
    @GetMapping("/provide-service")
    public String showProvideServicePage(Model model) {
        User user = (User) model.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // Redirect to login if no user is logged in
        }

        // Fetch the services provided by the logged-in user
        List<Service> services = serviceRepository.findByUser(user);

        // Fetch the pending requests for each service from the database
        for (Service service : services) {
            // Retrieve the pending request from the service_request table
            ServiceRequest pendingRequest = serviceRequestRepository
                .findByServiceAndStatus(service, ServiceRequest.Status.Pending);

            // Log the status of the request to verify if the data is correct
            if (pendingRequest != null) {
                System.out.println("Found pending request for service: " + service.getTitle() + " with status: " + pendingRequest.getStatus());

                // Set the pending request on the service
                service.setPendingRequest(pendingRequest);
            } else {
                service.setPendingRequest(null);
                System.out.println("No pending request for service: " + service.getTitle());
            }
        }

        // Add the services (with updated pending requests) to the model
        model.addAttribute("services", services);
        
        // Return the "provide-service" view
        return "provide-service";
    }

    // Admin page route
    @GetMapping("/admin")
    public String showAdminPage() {
        return "admin";  // This returns the admin.html view
    }
    
    @GetMapping("/manage-users")
    public String showManageUsersPage() {
        return "manage-users";  // This will return the manage-users.html view
    }

    
    
}
