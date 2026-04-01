## MySQL Database Design

### Table: admin
- id: BIGINT, Not Null, Primary Key, Auto Increment
- username: VARCHAR(255), Not Null
- password: VARCHAR(255), Not Null

### Table: appointment
- id: BIGINT, Not Null, Primary Key, Auto Increment
- appointment_time: DATETIME(6), Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled), Not Null
- doctor_id: BIGINT, Not Null, Foreign Key → doctor(id)
- patient_id: BIGINT, Not Null, Foreign Key → patient(id)

### Table: doctor
- id: INT, Primary Key, Auto Increment
- email: VARCHAR(255), Not Null
- name: VARCHAR(100), Not Null
- password: VARCHAR(255), Not Null
- phone: VARCHAR(255), Not Null
- specialty: VARCHAR(50), Not Null

### Table: doctor_available_times
- doctor_id: BIGINT, Not Null, Foreign Key → doctor(id)
- available_times: VARCHAR(255)

### Table: patient
- id: BIGINT, Not Null, Primary Key, Auto Increment
- address: VARCHAR(255), Not Null
- email: VARCHAR(255), Not Null
- name: VARCHAR(100), Not Null
- password: VARCHAR(255), Not Null
- phone: VARCHAR(255), Not Null


## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "_class": "com.project.back_end.models.Prescription"
}
