# 🏥 QR Code Based Health Record System

A secure, role-based healthcare web application for managing digital patient health records with QR-based patient identification.

## 🌐 Live Demo

https://qr-health-record-system.onrender.com/login.html

## ✨ Features

- 🔐 Role-based authentication and authorization
- 👑 Super Admin management
- 🧑‍💼 Admin management of doctors and patients
- 👨‍⚕️ Doctor patient search and medical record management
- 🧑 Patient access to personal health records
- 📱 Dynamic QR code generation
- 📷 QR code scanning using webcam
- 🪪 Digital Patient Health Card
- 🩺 Diagnosis and prescription management
- 📋 Medical history
- 👤 Doctor and patient profile management
- 🔑 Change password
- 📧 Email-based forgot/reset password
- 🛡️ Role-based endpoint authorization
- 🚫 Custom Access Denied page
- 📱 Responsive user interface

## 👥 User Roles

### 👑 Super Admin
- Create and manage administrators
- View system statistics

### 🧑‍💼 Admin
- Register doctors and patients
- Manage doctor and patient accounts
- Activate/deactivate accounts
- Edit doctor and patient information
- View dashboard statistics

### 👨‍⚕️ Doctor
- Search patients using Patient Code
- Scan patient QR codes using webcam
- View patient information
- View medical history
- Add diagnosis, prescription and visit notes
- Manage doctor profile
- Change password

### 🧑 Patient
- View personal health information
- View digital Patient Health Card
- View and download QR code
- View medical history
- Edit profile information
- Change password

## 🛠️ Technology Stack

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?logo=springsecurity&logoColor=white)
![JPA Hibernate](https://img.shields.io/badge/JPA%2FHibernate-ORM-59666C?logo=hibernate&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)
![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white)

### Additional Technologies

- 🔳 ZXing — QR code generation
- 📧 Spring Mail — email-based password recovery
- 🔒 BCrypt — password hashing
- 🌐 REST APIs — frontend and backend communication

## 🔄 QR Code Workflow

```text
Admin registers Patient
        ↓
Unique Patient Code generated
        ↓
Dynamic QR Code generated
        ↓
Patient Health Card displays QR
        ↓
Doctor scans QR using webcam
        ↓
Patient identified
        ↓
Doctor views patient information
        ↓
Doctor manages medical records

## 🩺 Medical Records

Doctors can manage:

- Diagnosis
- Prescription
- Visit Notes
- Visit Date and Time
- Complete Medical History

Patients can securely view their previous medical records from their dashboard.
## 🔐 Security

The application uses Spring Security for:

- Authentication
- Role-based authorization
- Protected REST endpoints
- BCrypt password hashing
- Secure password recovery
- Time-limited password reset tokens
- Role-based dashboard access
- Custom 403 Access Denied handling

Each user role can access only the functionality permitted for that role.

## 📧 Password Recovery

The system provides:

1. Forgot password request
2. Email-based password reset link
3. Secure reset token
4. New password creation
5. BCrypt password hashing
6. Login using the new password

## 🗂️ Project Structure

src/
└── main/
    ├── java/
    │   └── com/clinic/qrhealthrecord/
    │       ├── config/
    │       ├── controller/
    │       ├── dto/
    │       ├── entity/
    │       ├── exception/
    │       ├── repository/
    │       ├── security/
    │       ├── service/
    │       └── util/
    │
    └── resources/
        ├── static/
        │   ├── css/
        │   ├── js/
        │   ├── login.html
        │   ├── admin-dashboard.html
        │   ├── doctor-dashboard.html
        │   ├── patient-dashboard.html
        │   ├── super-admin-dashboard.html
        │   └── ...
        │
        └── application.properties

pom.xml

## 🗄️ Database

The application uses **MySQL** for persistent data storage.

Main data models include:

- Super Admin
- Admin
- Doctor
- Patient
- Medical Record
- Password Reset Token
- Doctor-Patient Access

## 📌 Project Highlights

- Full-stack healthcare web application
- Four user roles with role-based access control
- QR-based patient identification
- Dynamic digital Patient Health Card
- Medical record management
- RESTful backend
- MySQL database integration
- Secure password management
- Email-based password recovery
- Responsive frontend
- Cloud deployment

## 👨‍💻 Author

**Swamy N.**

Bachelor of Engineering in Electronics and Communication Engineering
