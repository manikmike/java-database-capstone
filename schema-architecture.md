# Architecture Summary 

This Spring Boot application uses both MVC and REST controllers. Thymeleaf templates are used for the Admin and Doctor dashboards, while REST APIs serve all other modules. The application interacts with two databases—MySQL (for patient, doctor, appointment, and admin data) and MongoDB (for prescriptions). All controllers route requests through a common service layer, which in turn delegates to the appropriate repositories. MySQL uses JPA entities while MongoDB uses document models.

---

# Numbered flow of data and control

1. User accesses AdminDashboard or Appointment pages.
2. The action is routed to the appropriate Thymeleaf or REST controller.
3. The controller calls the service layer at the heart of the backend system.
4. The service layer communicates with the repository layer to perform data access operations.
5. The repository layer interfaces directly with the appropriate MySQL or MongoDB database engine.
6. The data is retrieved from the database and mapped into Java classes the model can work with.
7. The bound models are used in the response layer in an MVC or REST flow.
