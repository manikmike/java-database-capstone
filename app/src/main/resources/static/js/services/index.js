// index.js
/*
  Import the openModal function to handle showing login popups/modals
  Import the base API URL from the config file
  Define constants for the admin and doctor login API endpoints using the base URL
*/
import { openModal } from '../components/modals.js';
import { API_BASE_URL } from '../config/config.js';
import { selectRole } from '../render.js';

const ADMIN_API = API_BASE_URL + '/admin';
const DOCTOR_API = API_BASE_URL + '/doctor/login'

/*
  Use the window.onload event to ensure DOM elements are available after page load
  Inside this function:
    - Select the "adminLogin" and "doctorLogin" buttons using getElementById
    - If the admin login button exists:
        - Add a click event listener that calls openModal('adminLogin') to show the admin login modal
    - If the doctor login button exists:
        - Add a click event listener that calls openModal('doctorLogin') to show the doctor login modal
*/
window.onload = function () {
	const adminBtn = document.getElementById('adminLogin');
	if (adminBtn) {
	  adminBtn.addEventListener('click', () => {
	    openModal('adminLogin');
	  });
	}
	const doctorBtn = document.getElementById('doctorLogin');
	if (doctorBtn) {
	  doctorBtn.addEventListener('click', () => {
	    openModal('doctorLogin');
	  });
	}
}     

/*
  Define a function named adminLoginHandler on the global window object
  This function will be triggered when the admin submits their login credentials
*/
export async function adminLoginHandler() {

/*
  Step 1: Get the entered username and password from the input fields
*/
  const username = document.getElementById("username");
  const password = document.getElementById("password"); 
  
 /*
 
  Step 2: Create an admin object with these credentials
*/
  const admin = { "username": username.value, "password": password.value };

/*
  Step 3: Use fetch() to send a POST request to the ADMIN_API endpoint
    - Set method to POST
    - Add headers with 'Content-Type: application/json'
    - Convert the admin object to JSON and send in the body
*/
    try {
        var response = await fetch(ADMIN_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(admin)
        });


        /*
          Step 4: If the response is successful:
            - Parse the JSON response to get the token
            - Store the token in localStorage
            - Call selectRole('admin') to proceed with admin-specific behavior
        */
        if (response.ok) {
            const data = await response.json();
            const token = data.token;
            localStorage.setItem("token", token);
            selectRole('admin');
        }

        /*
          Step 5: If login fails or credentials are invalid:
            - Show an alert with an error message
        */
        else {
            alert("Error logging in as admin.");
        }

        /*
          Step 6: Wrap everything in a try-catch to handle network or server errors
            - Show a generic error message if something goes wrong
        */
    }
    catch (error) {
        alert("Network Error. Please tray again later.");
    }
}

/*
  Define a function named doctorLoginHandler on the global window object
  This function will be triggered when a doctor submits their login credentials
*/
export async function doctorLoginHandler() {

/*
  Step 1: Get the entered email and password from the input fields
*/
  const email = document.getElementById("email");
  const password = document.getElementById("password");

/*

  Step 2: Create a doctor object with these credentials
*/
  const login = {"email": email.value, "password": password.value};

/*
  Step 3: Use fetch() to send a POST request to the DOCTOR_API endpoint
    - Include headers and request body similar to admin login
*/
 try {
  const response = await fetch(DOCTOR_API, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(login)
  }); 

/*

  Step 4: If login is successful:
    - Parse the JSON response to get the token
    - Store the token in localStorage
    - Call selectRole('doctor') to proceed with doctor-specific behavior
*/
  if (response.ok) {
    const data = await response.json();
    const token = data.token;
    localStorage.setItem("token", token);
    selectRole('doctor');
  }

/*
  Step 5: If login fails:
    - Show an alert for invalid credentials
*/
  else {
	alert("Error logging in as doctor.");
  }

/*

  Step 6: Wrap in a try-catch block to handle errors gracefully
    - Log the error to the console
    - Show a generic error message
*/
}
 catch(error) {
  alert("Network Error. Please try again later.");
 }
}