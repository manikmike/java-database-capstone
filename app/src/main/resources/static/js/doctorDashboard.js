 // doctorDashboard.js
/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment
*/
import { getAllAppointments } from './services/appointmentRecordService.js';
import { createPatientRow } from './components/patientRows.js';
import { renderContent } from './render.js';

// Get the table body where patient rows will be added
const tableBody = document.getElementById("patientTableBody");
  
// Initialize selectedDate with today's date in 'YYYY-MM-DD' format
const selectedDate = new Date().setHours(0, 0, 0, 0);

// Get the saved token from localStorage (used for authenticated API calls)
const token = localStorage.getItem("token");

// Initialize patientName to null (used for filtering by name)
const patientName = null;

/*
  Add an 'input' event listener to the search bar
  On each keystroke:
    - Trim and check the input value
    - If not empty, use it as the patientName for filtering
    - Else, reset patientName to "null" (as expected by backend)
    - Reload the appointments list with the updated filter
*/
document.getElementById("searchBar").addEventListener("input", () => {
	const searchBar = document.getElementById("searchBar").value.trim();  
    patientName = searchBar.length > 0 ? searchBar : null;
	loadAppointments();
});

/*
  Add a click listener to the "Today" button
  When clicked:
    - Set selectedDate to today's date
    - Update the date picker UI to match
    - Reload the appointments for today
*/
document.getElementById("todayButton").addEventListener("click", () => {
	selectedDate = Date.now(); 
    const datePicker = document.getElementById("datePicker");
	datePicker.value = selectedDate;
	loadAppointments();
});

/*
  Add a change event listener to the date picker
  When the date changes:
    - Update selectedDate with the new value
    - Reload the appointments for that specific date
*/
document.getElementById("datePicker").addEventListener("change", () => {
	selectedDate = document.getElementById("datePicker").value; 
	loadAppointments();
});

/*
  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name
*/
async function loadAppointments() {
    try {
        // Step 1: Call getAllAppointments with selectedDate, patientName, and token
        const appointments = await getAllAppointments(selectedDate, patientName, token);

        // Step 2: Clear the table body content before rendering new rows
        tableBody.innerHTML = "";

        // Step 3: If no appointments are returned:
        //   - Display a message row: "No Appointments found for today."
        if (appointments.length === 0) {
            tableBody.innerHTML = `<tr><td class="noPatientRecord" colspan='5'>No appointments found for today.</td></tr>`;
            return;
        }

        // Step 4: If appointments exist:
        //  - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
        //  - Call createPatientRow to generate a table row for the appointment
        //  - Append each row to the table body
        appointments.forEach(appointment => {
            const patient = appointment.patient;
            const appointmentId = appointment.appointmentId;
            const doctorId = appointment.doctorId;
            const row = createPatientRow(patient, appointmentId, doctorId);
            tableBody.appendChild(row);
        });
        /*
            Step 5: Catch and handle any errors during fetch:
            - Show a message row: "Error loading appointments. Try again later."
        */
    }
    catch (error) {
        tableBody.innerHTML = `<tr><td class="noPatientRecord" colspan='5'>No appointments found for today.</td></tr>`;
    }
}

/*
  When the page is fully loaded (DOMContentLoaded):
    - Call renderContent() (assumes it sets up the UI layout)
    - Call loadAppointments() to display today's appointments by default
*/
document.addEventListener("DOMContentLoaded", () => {
  renderContent();
  loadAppointments();
});
