package com.project.back_end.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

import jakarta.transaction.Transactional;

//1. **Add @Service Annotation**:
//- The `@Service` annotation is used to mark this class as a Spring service component. 
//- It will be managed by Spring's container and used for business logic related to patients and appointments.
//- Instruction: Ensure that the `@Service` annotation is applied above the class declaration.
@org.springframework.stereotype.Service
public class PatientService {

	// 2. **Constructor Injection for Dependencies**:
	//    - The `PatientService` class has dependencies on `PatientRepository`, `AppointmentRepository`, and `TokenService`.
	//    - These dependencies are injected via the constructor to maintain good practices of dependency injection and testing.
	//    - Instruction: Ensure constructor injection is used for all the required dependencies.
	private final PatientRepository patientRepository;
	private final AppointmentRepository appointmentRepository;
	private final TokenService tokenService;
	
	@Autowired
	public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository,
			TokenService tokenService) {
		this.patientRepository = patientRepository;
		this.appointmentRepository = appointmentRepository;
		this.tokenService = tokenService;
	}
	
	// 3. **createPatient Method**:
	//    - Creates a new patient in the database. It saves the patient object using the `PatientRepository`.
	//    - If the patient is successfully saved, the method returns `1`; otherwise, it logs the error and returns `0`.
	//    - Instruction: Ensure that error handling is done properly and exceptions are caught and logged appropriately.
	@Transactional
	public int createPatient(Patient patient) {
		try {
			patientRepository.save(patient);
			return 1;
		}
		catch(Exception e) {
			System.err.print(e.getMessage());
			return 0;
		}
	}

	// 4. **getPatientAppointment Method**:
	//    - Retrieves a list of appointments for a specific patient, based on their ID.
	//    - The appointments are then converted into `AppointmentDTO` objects for easier consumption by the API client.
	//    - This method is marked as `@Transactional` to ensure database consistency during the transaction.
	//    - Instruction: Ensure that appointment data is properly converted into DTOs and the method handles errors gracefully.
	@Transactional
	public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
		Map<String, Object> response = new HashMap<>();
		String identifier = tokenService.extractIdentifier(token);
		Patient patient = patientRepository.findByEmail(identifier);
		Long tokenId = patient.getId();
		try {
			if (id == tokenId) {
				List<Appointment> appointments = appointmentRepository.findByPatientId(id);
				List<AppointmentDTO> appointmentDTOs = new ArrayList<>();
				for (Appointment a : appointments) appointmentDTOs.add(a.toDTO());
				response.put("message", "Got appointments for patient id " + id);
				response.put("status", "200 OK");
				response.put("data", appointmentDTOs);
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
			else {
				response.put("message", "Unable to get appointments for patient id " + id);
				response.put("status", "401 Unauthorized");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}
		}
		catch (Exception e) {
			response.put("message", "Error getting appointments for patient id " + id);
			response.put("status", "500 Internal Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// 5. **filterByCondition Method**:
	//    - Filters appointments for a patient based on the condition (e.g., "past" or "future").
	//    - Retrieves appointments with a specific status (0 for future, 1 for past) for the patient.
	//    - Converts the appointments into `AppointmentDTO` and returns them in the response.
	//    - Instruction: Ensure the method correctly handles "past" and "future" conditions, and that invalid conditions are caught and returned as errors.
	@Transactional
	public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
		Map<String, Object> response = new HashMap<>();
		try {
			int status = 0; // assume "future" for condition
			if (condition.equals("past")) status = 1;
			List<Appointment> appointments = appointmentRepository.findByPatientIdAndStatusOrderByAppointmentTimeAsc(id, status);
			List<AppointmentDTO> appointmentDTOs = new ArrayList<>();
			for (Appointment a : appointments) appointmentDTOs.add(a.toDTO());
			response.put("message", "Filtered patient appointments by condition");
			response.put("status", "200 OK");
			response.put("data", appointmentDTOs);
			return ResponseEntity.status(HttpStatus.OK).body(response);

		}
		catch (Exception e)
		{
			response.put("message", "Error filtering patient appointments by condition");
			response.put("status","500 Internal Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// 6. **filterByDoctor Method**:
	//    - Filters appointments for a patient based on the doctor's name.
	//    - It retrieves appointments where the doctor’s name matches the given value, and the patient ID matches the provided ID.
	//    - Instruction: Ensure that the method correctly filters by doctor's name and patient ID and handles any errors or invalid cases.
	@Transactional
	public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long id) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(name, id);
			List<AppointmentDTO> appointmentDTOs = new ArrayList<>();
			for (Appointment a : appointments) appointmentDTOs.add(a.toDTO());
			response.put("message", "Filtered patient appointments by doctor name");
			response.put("status", "200 OK");
			response.put("data", appointmentDTOs);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		catch (Exception e)
		{
			response.put("message", "Error filtering patient appointments by condition");
			response.put("status","500 Internal Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// 7. **filterByDoctorAndCondition Method**:
	//    - Filters appointments based on both the doctor's name and the condition (past or future) for a specific patient.
	//    - This method combines filtering by doctor name and appointment status (past or future).
	//    - Converts the appointments into `AppointmentDTO` objects and returns them in the response.
	//    - Instruction: Ensure that the filter handles both doctor name and condition properly, and catches errors for invalid input.
	@Transactional
	public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, Long id) {
		Map<String, Object> response = new HashMap<>();
		try {
			int status = 0; // assume "future" for condition
			if (condition.equals("past")) status = 1;
			List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, id, status);
			List<AppointmentDTO> appointmentDTOs = new ArrayList<>();
			for (Appointment a : appointments) appointmentDTOs.add(a.toDTO());
			response.put("message", "Filtered patient appointments by doctor name and condition");
			response.put("status", "200 OK");
			response.put("data", appointmentDTOs);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		catch (Exception e)
		{
			response.put("message", "Error filtering patient appointments by condition");
			response.put("status","500 Internal Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// 8. **getPatientDetails Method**:
	//    - Retrieves patient details using the `tokenService` to extract the patient's email from the provided token.
	//    - Once the email is extracted, it fetches the corresponding patient from the `patientRepository`.
	//    - It returns the patient's information in the response body.
	//    - Instruction: Make sure that the token extraction process works correctly and patient details are fetched properly based on the extracted email.
	@Transactional
	public ResponseEntity<Map<String, Object>> getPatienDetails(String token) {
		Map<String, Object> response = new HashMap<>();
		try {
			String identifier = tokenService.extractIdentifier(token);
			Patient patient = patientRepository.findByEmail(identifier);
			response.put("message", "Got patient details");
			response.put("status", "200 OK");
			response.put("data", patient);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		catch(Exception e) {
			response.put("message", "Error getting patient details");
			response.put("status","500 Internal Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	// 9. **Handling Exceptions and Errors**:
	//    - The service methods handle exceptions using try-catch blocks and log any issues that occur. If an error occurs during database operations, the service responds with appropriate HTTP status codes (e.g., `500 Internal Server Error`).
	//    - Instruction: Ensure that error handling is consistent across the service, with proper logging and meaningful error messages returned to the client.

	// 10. **Use of DTOs (Data Transfer Objects)**:
	//    - The service uses `AppointmentDTO` to transfer appointment-related data between layers. This ensures that sensitive or unnecessary data (e.g., password or private patient information) is not exposed in the response.
	//    - Instruction: Ensure that DTOs are used appropriately to limit the exposure of internal data and only send the relevant fields to the client.
}
