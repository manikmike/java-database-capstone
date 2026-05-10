// adminDashboard.js
/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form
*/
import { openModal } from './components/modals.js';
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';

/*
  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')
*/
document.getElementById('addDocBtn').addEventListener('click', () => {
  openModal('addDoctor');
});

/*
  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors
*/
window.onload = function() {
	document.getElementById('modal').style.display = 'none';
	loadDoctorCards();
}

/*
  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards
*/
function loadDoctorCards() {
    // Call getDoctors() from the service layer
    // Clear the current content area
    getDoctors()
        .then(doctors => {
            const contentDiv = document.getElementById("content");
            contentDiv.innerHTML = "";

            // For each doctor returned:
            // - Create a doctor card using createDoctorCard()
            // - Append it to the content div
            doctors.forEach(doctor => {
                const card = createDoctorCard(doctor);
                contentDiv.appendChild(card);
            });
        })

        // Handle any fetch errors by logging them
        .catch(error => {
            console.error("Failed to load doctors:", error);
        });
}

/*
  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()
*/
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);

/*
  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty
*/
function filterDoctorsOnChange() {

    // Read values from the search bar and filters
	const searchBar = document.getElementById("searchBar").value.trim();
	const filterTime = document.getElementById("filterTime").value;
	const filterSpecialty = document.getElementById("filterSpecialty").value;
	
    // Normalize empty values to null
	const name = searchBar.length > 0 ? searchBar : null;
	const time = filterTime.length > 0 ? filterTime : null;
	const specialty = filterSpecialty.length > 0 ? filterSpecialty : null;
	
    // Call filterDoctors(name, time, specialty) from the service
	filterDoctors(name, time, specialty)
	  .then(response => {
	    const doctors = response.doctors;
	    const contentDiv = document.getElementById("content");
	    contentDiv.innerHTML = "";
		
    // If doctors are found:
    // - Render them using createDoctorCard()
    // If no doctors match the filter:
    // - Show a message: "No doctors found with the given filters."
	  if (doctors.length > 0) {
	    renderDoctorCards(doctors);
	  } else {
	    contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
	    console.log("Nothing");
	  }
	})

    // Catch and display any errors with an alert
	.catch(error => {
	  console.error("Failed to filter doctors:", error);
	  alert("❌ An error occurred while filtering doctors.");
	});
}

/*
  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it
*/
function renderDoctorCards(doctors) {
    // Clear the content area
	const contentDiv = document.getElementById("content");
	contentDiv.innerHTML = "";
	
    // Loop through the doctors and append each card to the content area
	console.log(doctors);
	doctors.forEach(doctor => {
	  const card = createDoctorCard(doctor);
	  contentDiv.appendChild(card);
	});
}

/*
  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system
*/
window.adminAddDoctor = async function() {
    try {
        // Collect input values from the modal form
        // - Includes name, email, phone, password, specialty, and available times
        const name = document.getElementById("doctorName").value;
        const specialty = document.getElementById("specialization").value;
        const email = document.getElementById("doctorEmail").value;
        const password = document.getElementById("doctorPassword").value;
        const phone = document.getElementById("doctorPhone").value;
        const availabileTimes = document.getElementById("availability").value;

        // Retrieve the authentication token from localStorage
        // - If no token is found, show an alert and stop execution
        const token = localStorage.getItem("token");
        if (!token) throw new Error("No toekn found");

        // Build a doctor object with the form values
        const doctor = { name, specialty, email, password, phone, availabileTimes };

        // Call saveDoctor(doctor, token) from the service
        const { success, message } = await saveDoctor(doctor, token);

        // If save is successful:
        // - Show a success message
        // - Close the modal and reload the page
        if (success) {
            alert(message);
            document.getElementById("modal").style.display = "none";
            window.location.reload();
        }

        // If saving fails, show an error message
        else alert(message);
    } catch (error) {
        console.error("Signup failed:", error);
        alert("❌ An error occurred while saving doctor.");
    }
}