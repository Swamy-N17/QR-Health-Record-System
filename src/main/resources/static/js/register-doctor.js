const form = document.getElementById("registerDoctorForm");
const errorBox = document.getElementById("doctorError");
const successBox = document.getElementById("doctorSuccess");
const submitBtn = document.getElementById("submitBtn");


// Password show / hide
document.getElementById("toggleDoctorPassword")
    .addEventListener("click", function () {

        const input = document.getElementById("doctorPassword");
        const isHidden = input.type === "password";

        input.type = isHidden ? "text" : "password";

        this.textContent = isHidden ? "⊘" : "◉";

        this.setAttribute(
            "aria-label",
            isHidden ? "Hide password" : "Show password"
        );
    });


// Clear field errors
function clearFieldErrors() {

    document.querySelectorAll(
        "#registerDoctorForm .field-error"
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


// Validate form
function validateDoctorForm(data) {

    let isValid = true;

    if (!data.fullName.trim()) {
        showFieldError(
            "doctorFullName",
            "Full name is required."
        );
        isValid = false;
    }

    const emailPattern =
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailPattern.test(data.email)) {
        showFieldError(
            "doctorEmail",
            "Enter a valid email address."
        );
        isValid = false;
    }

    if (data.password.length < 6) {
        showFieldError(
            "doctorPassword",
            "Password must be at least 6 characters."
        );
        isValid = false;
    }

    if (!/^[0-9]{10}$/.test(data.phoneNumber)) {
        showFieldError(
            "doctorPhone",
            "Phone number must be exactly 10 digits."
        );
        isValid = false;
    }

    if (!data.dateOfBirth) {

        showFieldError(
            "doctorDob",
            "Date of birth is required."
        );

        isValid = false;

    } else if (new Date(data.dateOfBirth) > new Date()) {

        showFieldError(
            "doctorDob",
            "Date of birth cannot be in the future."
        );

        isValid = false;
    }

    if (!data.gender) {
        showFieldError(
            "doctorGender",
            "Please select a gender."
        );
        isValid = false;
    }

    if (!data.specialization.trim()) {
        showFieldError(
            "doctorSpecialization",
            "Specialization is required."
        );
        isValid = false;
    }

    if (!data.address.trim()) {
        showFieldError(
            "doctorAddress",
            "Address is required."
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


    const data = {
        fullName:
            document.getElementById("doctorFullName").value,

        email:
            document.getElementById("doctorEmail").value,

        password:
            document.getElementById("doctorPassword").value,

        phoneNumber:
            document.getElementById("doctorPhone").value,

        dateOfBirth:
            document.getElementById("doctorDob").value,

        gender:
            document.getElementById("doctorGender").value,

        specialization:
            document.getElementById("doctorSpecialization").value,

        address:
            document.getElementById("doctorAddress").value
    };


    if (!validateDoctorForm(data)) {

        errorBox.textContent =
            "Please fix the highlighted fields before submitting.";

        errorBox.style.display = "block";

        return;
    }


    submitBtn.disabled = true;
    submitBtn.textContent = "Registering...";


    try {

        await apiRequest(
            "/admin/doctors",
            "POST",
            data
        );

        successBox.textContent =
            "Doctor registered successfully!";

        successBox.style.display = "block";

        form.reset();

    } catch (error) {

        errorBox.textContent = error.message;
        errorBox.style.display = "block";

    } finally {

        submitBtn.disabled = false;
        submitBtn.textContent = "Register Doctor";
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