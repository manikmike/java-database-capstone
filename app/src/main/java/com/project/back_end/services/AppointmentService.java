package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import jakarta.transaction.Transactional;

//1. **Add @Service Annotation**:
//- To indicate that this class is a service layer class for handling business logic.
//- The `@Service` annotation should be added before the class declaration to mark it as a Spring service component.
//- Instruction: Add `@Service` above the class definition.
@org.springframework.stereotype.Service
public class AppointmentService {

	// 2. **Constructor Injection for Dependencies**:
	//    - The `AppointmentService` class requires several dependencies like `AppointmentRepository`, `Service`, `TokenService`, `PatientRepository`, and `DoctorRepository`.
	//    - These dependencies should be injected through the constructor.
	//    - Instruction: Ensure constructor injection is used for proper dependency management in Spring.
	private final AppointmentRepository appointmentRepository;
	private final Service service;
	private final TokenService tokenService;
	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;
	
	@Autowired
	public AppointmentService(AppointmentRepository appointmentRepository, Service service, TokenService tokenService,
			PatientRepository patientRepository, DoctorRepository doctorRepository) {
		this.appointmentRepository = appointmentRepository;
		this.service = service;
		this.tokenService = tokenService;
		this.patientRepository = patientRepository;
		this.doctorRepository = doctorRepository;
	}

	// 3. **Add @Transactional Annotation for Methods that Modify Database**:
	//    - The methods that modify or update the database should be annotated with `@Transactional` to ensure atomicity and consistency of the operations.
	//    - Instruction: Add the `@Transactional` annotation above methods that interact with the database, especially those modifying data.

	// 4. **Book Appointment Method**:
	//    - Responsible for saving the new appointment to the database.
	//    - If the save operation fails, it returns `0`; otherwise, it returns `1`.
	//    - Instruction: Ensure that the method handles any exceptions and returns an appropriate result code.
	@Transactional
	public int bookAppointment(Appointment appointment) {
		try {
			if (appointmentRepository.save(appointment) == null) return 0;
			else return 1;
		}
		catch(Exception e) {
			return 0;
		}
	}
	
	// 5. **Update Appointment Method**:
	//    - This method is used to update an existing appointment based on its ID.
	//    - It validates whether the patient ID matches, checks if the appointment is available for updating, and ensures that the doctor is available at the specified time.
	//    - If the update is successful, it saves the appointment; otherwise, it returns an appropriate error message.
	//    - Instruction: Ensure proper validation and error handling is included for appointment updates.
	@Transactional
	public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
		Map<String, String> response = new HashMap<>();
		try
		{
			Appointment foundAppointment = appointmentRepository.getReferenceById(appointment.getId());
			if (foundAppointment != null) {
				if (service.validateAppointment(appointment) == 0) {
					appointmentRepository.save(appointment);
					response.put("message", "Appointment updated");
					response.put("status", "200 OK");
					return ResponseEntity.status(HttpStatus.OK).body(response);
				}
				else {
					response.put("message", "Appointment is not valid");
					response.put("status", "401 Unauthorized");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
				}
			}
			else {
				response.put("message", "Appointment not found");
				response.put("status", "404 Not Found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
		}
		catch(Exception e) {
			response.put("message", "Error updating appointment");
			response.put("status", "500 Internal Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	

	// 6. **Cancel Appointment Method**:
	//    - This method cancels an appointment by deleting it from the database.
	//    - It ensures the patient who owns the appointment is trying to cancel it and handles possible errors.
	//    - Instruction: Make sure that the method checks for the patient ID match before deleting the appointment.
	@Transactional
	public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
		Map<String, String> response = new HashMap<>();
		try
		{
			Appointment foundAppointment = appointmentRepository.getReferenceById(id);
			if (foundAppointment != null) {
				if (foundAppointment.getPatient().getId() == patientRepository.findByEmail(tokenService.extractIdentifier(token)).getId()) {
					appointmentRepository.delete(foundAppointment);
					response.put("message", "Appointment deleted");
					response.put("status", "200 OK");
					return ResponseEntity.status(HttpStatus.OK).body(response);
				}
				else {
					response.put("message", "Appointment not deleted");
					response.put("status", "401 Unauthorized");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
				}
			}
			else {
				response.put("message", "Appointment not found");
				response.put("status", "404 Not Found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
		}
		catch(Exception e) {
			response.put("message", "Error deleting appointment");
			response.put("status", "500 Internal Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// 7. **Get Appointments Method**:
	//    - This method retrieves a list of appointments for a specific doctor on a particular day, optionally filtered by the patient's name.
	//    - It uses `@Transactional` to ensure that database operations are consistent and handled in a single transaction.
	//    - Instruction: Ensure the correct use of transaction boundaries, especially when querying the database for appointments.
	@Transactional
	public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
		Map<String, Object> appointmentMap = new HashMap<>();
		Long doctorId = doctorRepository.findByEmail(tokenService.extractIdentifier(token)).getId();
		LocalDateTime start = LocalDateTime.of(date, LocalTime.of(0, 0));
		LocalDateTime end = start.plusHours(23);
        List<Appointment> appointments = new ArrayList<>();
        if (pname == null) {
            appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
        }
        else {
        	appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctorId, pname, start, end);
        }
        appointmentMap.put("appointments", appointments);
		return appointmentMap;
	}

	// 8. **Change Status Method**:
	//    - This method updates the status of an appointment by changing its value in the database.
	//    - It should be annotated with `@Transactional` to ensure the operation is executed in a single transaction.
	//    - Instruction: Add `@Transactional` before this method to ensure atomicity when updating appointment status.
	@Transactional
	public void changeStatus(Appointment appointment, int status) {
		appointment.setStatus(status);
		appointmentRepository.save(appointment);
	}
}
