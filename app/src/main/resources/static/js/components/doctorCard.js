// doctorCard.js

/*
Import the overlay function for booking appointments from loggedPatient.js
*/
import { showBookingOverlay } from "../loggedPatient";

/*
Import the deleteDoctor API function to remove doctors (admin role) from doctorServices.js
*/
import  { deleteDoctor }  from '../services/doctorServices.js';

/*
Import function to fetch patient details (used during booking) from patientServices.js
*/
import { getPatientData } from '../services/patientServices.js';

// Function to create and return a DOM element for a single doctor card
export function createDoctorCard(doctor) {

	// Create the main container for the doctor card
	const card = document.createElement("div");
	card.classList.add("doctor-card");
	 
	//Retrieve the current user role from localStorage
	const role = localStorage.getItem("userRole");
	
    // Create a div to hold doctor information
	const infoDiv = document.createElement("div");
	infoDiv.classList.add("doctor-info");
	
    // Create and set the doctor’s name
	const name = document.createElement("h3");
	name.textContent = doctor.name;
	
    // Create and set the doctor's specialization
	const specialization = document.createElement("h3");
	specialization.textContent = doctor.specialization;
	
    // Create and set the doctor's email
	const email = document.createElement("h3");
	email.textContent = doctor.email;
	
    // Create and list available appointment times
	const availability = document.createElement("h3");
	availability.textContent = doctor.availability.join(", ");
	
    // Append all info elements to the doctor info container
	infoDiv.appendChild(name);
	infoDiv.appendChild(specialization);
	infoDiv.appendChild(email);
	infoDiv.appendChild(availability);
	
    // Create a container for card action buttons
	const actionsDiv = document.createElement("div");
	actionsDiv.classList.add("card-actions");
	
    // == ADMIN ROLE ACTIONS ===
	if (role === "admin") {
		// Create a delete button
		const removeBtn = document.createElement("button");
		removeBtn.textContent = "Delete";
		// Add click handler for delete button
		removeBtn.addEventListener("click", async () => {
		  // 1. Confirm deletion
		  if (confirm("Are you sure you want to delete this doctor?")) {
			  // Get the admin token from localStorage
			  // 2. Get token from localStorage
			  const token = localStorage.getItem("token");
			  // Call API to delete the doctor
	 		  // 3. Call API to delete
			  const result = await deleteDoctor(token);
			  // Show result and remove card if successful
			  // 4. On success: remove the card from the DOM
			  if (result) card.remove();
		  }
		});
		// Add delete button to actions container
		actionsDiv.appendChild(removeBtn);
  	}
	
    // === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
	else if (role === "patient") {
	  	// Create a book now button
		const bookNow = document.createElement("button");
	  	bookNow.textContent = "Book Now";
	  	bookNow.addEventListener("click", () => {
			// Alert patient to log in before booking
	    	alert("Patient needs to login first.");
	  	});
		// Add button to actions container
		actionsDiv.appendChild(bookNow);
	}
  
    // === LOGGED-IN PATIENT ROLE ACTIONS === 
	else if (role === "loggedPatient") {
		// Create a book now button
	 	const bookNow = document.createElement("button");
	 	bookNow.textContent = "Book Now";
		// Handle booking logic for logged-in patient
	 	bookNow.addEventListener("click", async (e) => {
			// Redirect if token not available
	    	const token = localStorage.getItem("token");
			// Fetch patient data with token
	    	const patientData = await getPatientData(token);
			// Show booking overlay UI with doctor and patient info
	    	showBookingOverlay(e, doctor, patientData);
	  });
	  // Add button to actions container
	  actionsDiv.appendChild(bookNow);
	}
      
 	// Append doctor info and action buttons to the card
	card.appendChild(infoDiv);
	card.appendChild(actionsDiv);
	
 	// Return the complete doctor card element
	return card;
}