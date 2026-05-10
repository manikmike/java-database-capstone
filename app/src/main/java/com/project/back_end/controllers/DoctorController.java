package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;

// 1. Set Up the Controller Class:
//  - Annotate the class with `@RestController` to define it as a REST controller that serves JSON responses.
//  - Use `@RequestMapping("${api.path}doctor")` to prefix all endpoints with a configurable API path followed by "doctor".
//  - This class manages doctor-related functionalities such as registration, login, updates, and availability.
@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

	// 2. Autowire Dependencies:
	//    - Inject `DoctorService` for handling the core logic related to doctors (e.g., CRUD operations, authentication).
	//    - Inject the shared `Service` class for general-purpose features like token validation and filtering.
	@Autowired
	private final DoctorService doctorService;
	private final Service service;
	private final TokenService tokenService;
	
	public DoctorController(DoctorService doctorService, Service service, TokenService tokenService) {
		this.doctorService = doctorService;
		this.service = service;
		this.tokenService = tokenService;
	}
    
	// 3. Define the `getDoctorAvailability` Method:
	//    - Handles HTTP GET requests to check a specific doctor’s availability on a given date.
	//    - Requires `user` type, `doctorId`, `date`, and `token` as path variables.
	//    - First validates the token against the user type.
	//    - If the token is invalid, returns an error response; otherwise, returns the availability status for the doctor.
	@GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
	public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable String user, @PathVariable long doctorId, @PathVariable LocalDate date, @PathVariable String token) {
		Map<String, Object> response = new HashMap<>();
		if (tokenService.validateToken(token, "patient")) {
			List<String> availableTimes = doctorService.getDoctorAvailability(doctorId, date);
	        if (availableTimes != null) {
				response.put("message", "Doctor availability on date" + date);
				response.put("status", "200 OK");
				response.put("data", availableTimes);
				return ResponseEntity.status(HttpStatus.OK).body(response);
	        }
	        else {
				response.put("message", "Doctor has no availability on date" + date);
				response.put("status", "404 Not Found");
				response.put("data", availableTimes);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	        }
		}
		else  {
			response.put("message", "Doctor availability can only be viewed by patients");
			response.put("status", "401 Unauthorized");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

	}

	// 4. Define the `getDoctor` Method:
	//    - Handles HTTP GET requests to retrieve a list of all doctors.
	//    - Returns the list within a response map under the key `"doctors"` with HTTP 200 OK status.
	@GetMapping
	public ResponseEntity<Map<String, Object>> getDoctor() {
		Map<String, Object> response = new HashMap<>();
		List<Doctor> doctors = doctorService.getDoctors();
		if (doctors != null) {
			response.put("message", "Doctors found");
			response.put("status", "200 OK");
			response.put("doctors", doctors);
			return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        else {
			response.put("message", "No doctors found");
			response.put("status", "404 Not Found");
			response.put("doctors", doctors);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
	}

	// 5. Define the `saveDoctor` Method:
	//    - Handles HTTP POST requests to register a new doctor.
	//    - Accepts a validated `Doctor` object in the request body and a token for authorization.
	//    - Validates the token for the `"admin"` role before proceeding.
	//    - If the doctor already exists, returns a conflict response; otherwise, adds the doctor and returns a success message.
	@PostMapping("/{token}")
	public ResponseEntity<Map<String, String>> saveDoctor(@PathVariable String token, @RequestBody Doctor doctor) {
		Map<String, String> response = new HashMap<>();
		if (tokenService.validateToken(token, "admin")) {
			int result = doctorService.saveDoctor(doctor);
			if (result == 1) {
				response.put("message", "Doctor added to db");
				response.put("status", "200 OK");
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
			else if (result == -1) {
				response.put("message", "Doctor already exists");
				response.put("status", "409 Conflict");
				return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
			}
			else {
				response.put("message", "Some internal error occured");
				response.put("status", "500 Internal Server Error");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}
		}
		else {
			response.put("message", "Only admin can add doctors to the db");
			response.put("status", "401 Unauthorized");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}
 

	// 6. Define the `doctorLogin` Method:
	//    - Handles HTTP POST requests for doctor login.
	//    - Accepts a validated `Login` DTO containing credentials.
	//    - Delegates authentication to the `DoctorService` and returns login status and token information.
	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
		return doctorService.validateDoctor(login);
	}

	// 7. Define the `updateDoctor` Method:
	//    - Handles HTTP PUT requests to update an existing doctor's information.
	//    - Accepts a validated `Doctor` object and a token for authorization.
	//    - Token must belong to an `"admin"`.
	//    - If the doctor exists, updates the record and returns success; otherwise, returns not found or error messages.
	@PutMapping("/{token}")
	public ResponseEntity<Map<String, String>> updateDoctor(@PathVariable String token, @RequestBody Doctor doctor) {
		Map<String, String> response = new HashMap<>();
		if (tokenService.validateToken(token, "admin")) {
			int result = doctorService.updateDoctor(doctor);
			if (result == 1) {
				response.put("message", "Doctor updated");
				response.put("status", "200 OK");
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
			else if (result == -1) {
				response.put("message", "Doctor not found");
				response.put("status", "404 Not Found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
			else {
				response.put("message", "Some internal error occured");
				response.put("status", "500 Internal Server Error");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}
		}
		else {
			response.put("message", "Only admin can update doctors in the db");
			response.put("status", "401 Unauthorized");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

	// 8. Define the `deleteDoctor` Method:
	//    - Handles HTTP DELETE requests to remove a doctor by ID.
	//    - Requires both doctor ID and an admin token as path variables.
	//    - If the doctor exists, deletes the record and returns a success message; otherwise, responds with a not found or error message.
	@DeleteMapping("/{id}/{token}")
	public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable long id, @PathVariable String token) {
		Map<String, String> response = new HashMap<>();
		if (tokenService.validateToken(token, "admin")) {
			int result = doctorService.deleteDoctor(id);
			if (result == 1) {
				response.put("message", "Doctor deleted successfully");
				response.put("status", "200 OK");
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
			else if (result == -1) {
				response.put("message", "Doctor not found with id " + id);
				response.put("status", "404 Not Found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
			else {
				response.put("message", "Some internal error occured");
				response.put("status", "500 Internal Server Error");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}
		}
		else {
			response.put("message", "Only admin can delete doctors from the db");
			response.put("status", "401 Unauthorized");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

	// 9. Define the `filter` Method:
	//    - Handles HTTP GET requests to filter doctors based on name, time, and specialty.
	//    - Accepts `name`, `time`, and `speciality` as path variables.
	//    - Calls the shared `Service` to perform filtering logic and returns matching doctors in the response.
	@GetMapping("/filter/{name}/{time}/{speciality}")
	public ResponseEntity<Map<String, Object>> filter(@PathVariable String name, @PathVariable String time, @PathVariable String specialty) {
		Map<String, Object> response = new HashMap<>();
		try {
			response = service.filterDoctor(name, specialty, time);
			response.put("message", "Filtered doctors successfully");
			response.put("status", "200 OK");
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		catch (Exception e) {
			response.put("message", "Some internal error occured");
			response.put("status", "500 Internal Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}
