// util.js
  export function setRole(role) {
    localStorage.setItem("userRole", role);
  }
  
  export function getRole() {
    return localStorage.getItem("userRole");
  }
  
  export function clearRole() {
    localStorage.removeItem("userRole");
  }
  
