package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
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

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;

//1. Set Up the Controller Class:
//- Annotate the class with `@RestController` to define it as a REST API controller.
//- Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
//- This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.
@RestController
@RequestMapping("${api.path}" + "appointments")
public class AppointmentController {

	// 2. Autowire Dependencies:
	//    - Inject `AppointmentService` for handling the business logic specific to appointments.
	//    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.
	private final AppointmentService appointmentService;
	private final Service service;
	private final TokenService tokenService;
	
	@Autowired
	public AppointmentController(AppointmentService appointmentService, Service service, TokenService tokenService) {
		this.appointmentService = appointmentService;
		this.service = service;
		this.tokenService = tokenService;
	}

	// 3. Define the `getAppointments` Method:
	//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
	//    - Takes the appointment date, patient name, and token as path variables.
	//    - First validates the token for role `"doctor"` using the `TokenService`.
	//    - If the token is valid, returns appointments for the given patient on the specified date.
	//    - If the token is invalid or expired, responds with the appropriate message and status code.
	@GetMapping("/{date}/{patientName}/{token}")
	public ResponseEntity<Map<String, Object>> getAppointments(@PathVariable LocalDate date, @PathVariable String patientName, @PathVariable String token) {
		Map<String, Object> response = new HashMap<>();
		if (!tokenService.validateToken(token, "doctor")) {
			response.put("message", "Only doctors can access appointment data");
			response.put("status", "401 Unauthorized");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
		else {
			response = appointmentService.getAppointment(patientName, date, token);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
	}

	// 4. Define the `bookAppointment` Method:
	//    - Handles HTTP POST requests to create a new appointment.
	//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
	//    - Validates the token for the `"patient"` role.
	//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
	//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.
	@PostMapping("/{token}")
	public ResponseEntity<Map<String, String>> bookAppointment(@PathVariable String token, @RequestBody Appointment appointment) {
		Map<String, String> response = new HashMap<>();
		if (tokenService.validateToken(token, "patient")) {
			if (service.validateAppointment(appointment) == 1) {
				if (appointmentService.bookAppointment(appointment) == 1) {
					response.put("message", "Appointment booked");
					response.put("status", "201 OK");
					return ResponseEntity.status(HttpStatus.OK).body(response);
				}
				else {
					response.put("message", "Unable to book appointment");
					response.put("status", "500 Internal Server Error");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
				}
			}
			else {
				response.put("message", "Appointment request is not valid");
				response.put("status", "500 Internal Server Error");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}
		}
		else  {
			response.put("message", "Appointment must be scheduled by patients");
			response.put("status", "401 Unauthorized");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}	
	}
	
	// 5. Define the `updateAppointment` Method:
	//    - Handles HTTP PUT requests to modify an existing appointment.
	//    - Accepts a validated `Appointment` object and a token as input.
	//    - Validates the token for `"patient"` role.
	//    - Delegates the update logic to the `AppointmentService`.
	//    - Returns an appropriate success or failure response based on the update result.
	@PutMapping("/{token}")
	public ResponseEntity<Map<String, String>> updateAppointment(@PathVariable String token, @RequestBody Appointment appointment) {
		Map<String, String> response = new HashMap<>();
		if (tokenService.validateToken(token, "patient")) {
			if (service.validateAppointment(appointment) == 1) {
				return appointmentService.updateAppointment(appointment);
			}
			else {
				response.put("message", "Appointment request is not valid");
				response.put("status", "500 Internal Server Error");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}
		}
		else  {
			response.put("message", "Appointment must be scheduled by patients");
			response.put("status", "401 Unauthorized");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

	// 6. Define the `cancelAppointment` Method:
	//    - Handles HTTP DELETE requests to cancel a specific appointment.
	//    - Accepts the appointment ID and a token as path variables.
	//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
	//    - Calls `AppointmentService` to handle the cancellation process and returns the result.
	@DeleteMapping("/{id}/{token}")
	public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable long id, @PathVariable String token) {
		Map<String, String> response = new HashMap<>();
		if (tokenService.validateToken(token, "patient")) {
			return appointmentService.cancelAppointment(id, token);
		}
		else  {
			response.put("message", "Appointment must be scheduled by patients");
			response.put("status", "401 Unauthorized");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}
}
