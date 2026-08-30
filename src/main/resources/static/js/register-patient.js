const form = document.getElementById("registerPatientForm");
const errorBox = document.getElementById("patientError");
const successBox = document.getElementById("patientSuccess");
const submitBtn = document.getElementById("submitBtn");


// Password show / hide
document.getElementById("togglePatientPassword")
    .addEventListener("click", function () {

        const input = document.getElementById("patientPassword");
        const isHidden = input.type === "password";

        input.type = isHidden ? "text" : "password";

        this.textContent = isHidden ? "⊘" : "◉";

        this.setAttribute(
            "aria-label",
            isHidden ? "Hide password" : "Show password"
        );
    });


// Clear errors
function clearFieldErrors() {

    document.querySelectorAll(
        "#registerPatientForm .field-error"
    ).forEach(el => {

        el.style.display = "none";
        el.textContent = "";
    });
}


// Show field error
function showFieldError(fieldId, message) {

    const errorEl =
        document.getElementById("err-" + fieldId);

    if (errorEl) {
        errorEl.textContent = message;
        errorEl.style.display = "block";
    }
}


// Validate patient
// NOTE: dateOfBirth, bloodGroup, emergencyContact are OPTIONAL per Phase 1 —
// they are only validated for correct format IF the admin chose to fill them in.
function validatePatientForm(data) {

    let isValid = true;

    if (!data.fullName.trim()) {
        showFieldError(
            "patientFullName",
            "Full name is required."
        );
        isValid = false;
    }

    const emailPattern =
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailPattern.test(data.email)) {
        showFieldError(
            "patientEmail",
            "Enter a valid email address."
        );
        isValid = false;
    }

    if (data.password.length < 6) {
        showFieldError(
            "patientPassword",
            "Password must be at least 6 characters."
        );
        isValid = false;
    }

    if (!/^[0-9]{10}$/.test(data.phoneNumber)) {
        showFieldError(
            "patientPhone",
            "Phone number must be exactly 10 digits."
        );
        isValid = false;
    }

    if (data.age === null || data.age === "" || isNaN(data.age)) {

        showFieldError(
            "patientAge",
            "Age is required."
        );

        isValid = false;

    } else if (data.age < 0 || data.age > 130) {

        showFieldError(
            "patientAge",
            "Enter a valid age."
        );

        isValid = false;
    }

    if (!data.gender) {
        showFieldError(
            "patientGender",
            "Please select a gender."
        );
        isValid = false;
    }

    // Optional: only validated if the admin actually entered something
    if (data.dateOfBirth && new Date(data.dateOfBirth) > new Date()) {

        showFieldError(
            "patientDob",
            "Date of birth cannot be in the future."
        );

        isValid = false;
    }

    if (!data.address.trim()) {
        showFieldError(
            "patientAddress",
            "Address is required."
        );
        isValid = false;
    }

    // Optional: only validated if the admin actually entered something
    if (data.emergencyContact && !/^[0-9]{10}$/.test(data.emergencyContact)) {

        showFieldError(
            "patientEmergencyContact",
            "Emergency contact must be exactly 10 digits."
        );

        isValid = false;
    }

    return isValid;
}


// Submit
form.addEventListener("submit", async function (e) {

    e.preventDefault();

    errorBox.style.display = "none";
    successBox.style.display = "none";

    clearFieldErrors();


    const dobValue = document.getElementById("patientDob").value;
    const bloodGroupValue = document.getElementById("patientBloodGroup").value;
    const emergencyContactValue = document.getElementById("patientEmergencyContact").value.trim();
    const ageValue = document.getElementById("patientAge").value;

    const data = {

        fullName:
            document.getElementById("patientFullName").value,

        email:
            document.getElementById("patientEmail").value,

        password:
            document.getElementById("patientPassword").value,

        phoneNumber:
            document.getElementById("patientPhone").value,

        age:
            ageValue === "" ? null : Number(ageValue),

        gender:
            document.getElementById("patientGender").value,

        address:
            document.getElementById("patientAddress").value,

        // Optional fields — sent as null when left blank, never as
        // empty strings, so the backend stores them as genuinely absent.
        dateOfBirth:
            dobValue === "" ? null : dobValue,

        bloodGroup:
            bloodGroupValue === "" ? null : bloodGroupValue,

        emergencyContact:
            emergencyContactValue === "" ? null : emergencyContactValue
    };


    if (!validatePatientForm(data)) {

        errorBox.textContent =
            "Please fix the highlighted fields before submitting.";

        errorBox.style.display = "block";

        return;
    }


    submitBtn.disabled = true;
    submitBtn.textContent = "Registering...";


    try {

        const patient = await apiRequest(
            "/admin/patients",
            "POST",
            data
        );

        successBox.textContent =
            "Patient registered successfully. The Health Card is ready.";
        successBox.style.display = "block";

        form.reset();
        showHealthCard(patient.id);

    } catch (error) {

        errorBox.textContent = error.message;
        errorBox.style.display = "block";

    } finally {

        submitBtn.disabled = false;
        submitBtn.textContent = "Register Patient";
    }
});


// Logout
document.getElementById("logoutBtn")
    .addEventListener("click", async function () {

        try {
            await apiRequest("/auth/logout", "POST");
        } finally {
            window.location.href = "/login.html";
        }
    });

// Show the generated Health Card immediately after registration.
function showHealthCard(patientId) {
    const result = document.getElementById("healthCardResult");
    const image = document.getElementById("registeredHealthCard");

    image.src = `/api/admin/patients/${patientId}/health-card?_=${Date.now()}`;
    result.style.display = "block";
    result.scrollIntoView({ behavior: "smooth", block: "start" });
}

document.getElementById("registerAnotherBtn")?.addEventListener("click", function () {
    document.getElementById("healthCardResult").style.display = "none";
    document.getElementById("patientSuccess").style.display = "none";
    document.getElementById("patientError").style.display = "none";
    document.getElementById("registerPatientForm").scrollIntoView({ behavior: "smooth", block: "start" });
});
