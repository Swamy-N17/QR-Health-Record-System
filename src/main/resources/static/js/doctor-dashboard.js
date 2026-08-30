loadDashboardStats();


// =========================================================
// DASHBOARD STATS
// =========================================================

async function loadDashboardStats() {

    try {

        const stats = await apiRequest("/doctor/dashboard");

        document.getElementById("todaysPatients").textContent =
            stats.todaysPatients;

    } catch (error) {

        console.error(
            "Failed to load dashboard stats:",
            error
        );
    }
}


// =========================================================
// SEARCH PATIENT BY CODE
// =========================================================

document
    .getElementById("searchByCodeBtn")
    .addEventListener("click", async function () {

        const code = document
            .getElementById("manualCode")
            .value
            .trim();

        if (!code) {

            const errorBox =
                document.getElementById("findPatientError");

            errorBox.textContent =
                "Please enter a patient code.";

            errorBox.style.display = "block";

            return;
        }

        await findPatientByCode(code);
    });


// Allow pressing Enter inside patient code field

document
    .getElementById("manualCode")
    .addEventListener("keydown", function (e) {

        if (e.key === "Enter") {

            e.preventDefault();

            document
                .getElementById("searchByCodeBtn")
                .click();
        }
    });


// =========================================================
// FIND PATIENT
// =========================================================

async function findPatientByCode(code) {

    const errorBox =
        document.getElementById("findPatientError");

    errorBox.style.display = "none";

    try {

        const patient = await apiRequest(
            `/doctor/patients/scan/${encodeURIComponent(code)}`
        );

        showPatientDetails(patient);

    } catch (error) {

        errorBox.textContent =
            "Patient not found: " + error.message;

        errorBox.style.display = "block";

        document
            .getElementById("patientDetailsSection")
            .style.display = "none";
    }
}


// =========================================================
// QR CODE SCANNER
// =========================================================

let html5QrScanner = null;

const scannerBox =
    document.getElementById("qrScannerBox");

const scannerButton =
    document.getElementById("startScanBtn");

const reader =
    document.getElementById("qrReader");


// Start / Stop scanner

scannerButton.addEventListener("click", async function () {

    // -----------------------------------------
    // STOP SCANNER
    // -----------------------------------------

    if (html5QrScanner) {

        await stopScanner();

        return;
    }


    // -----------------------------------------
    // START SCANNER
    // -----------------------------------------

    try {

        scannerBox.classList.add("active");

        scannerButton.classList.add("scanning");

        scannerButton.textContent =
            "Stop Scanner";


        html5QrScanner =
            new Html5Qrcode("qrReader");


        await html5QrScanner.start(

            {
                facingMode: "environment"
            },

            {
                fps: 10,
                qrbox: {
                    width: 190,
                    height: 190
                }
            },

            async function (decodedText) {

                // QR successfully detected

                await findPatientByCode(decodedText);


                // Stop camera after successful scan

                await stopScanner();
            },

            function () {

                // No QR detected in this frame.
                // Nothing needs to be done.
            }

        );

    } catch (error) {

        console.error(
            "Failed to start QR scanner:",
            error
        );

        scannerBox.classList.remove("active");

        scannerButton.classList.remove("scanning");

        scannerButton.textContent =
            "Scan QR Code";

        html5QrScanner = null;

        const errorBox =
            document.getElementById("findPatientError");

        errorBox.textContent =
            "Unable to access the camera. Please allow camera permission and try again.";

        errorBox.style.display = "block";
    }

});


// =========================================================
// STOP QR SCANNER
// =========================================================

async function stopScanner() {

    if (!html5QrScanner) {
        return;
    }

    try {

        await html5QrScanner.stop();

    } catch (error) {

        console.error(
            "Error stopping scanner:",
            error
        );
    }


    html5QrScanner = null;

    reader.innerHTML = "";

    scannerBox.classList.remove("active");

    scannerButton.classList.remove("scanning");

    scannerButton.textContent =
        "Scan QR Code";
}


// =========================================================
// SHOW PATIENT DETAILS
// =========================================================

function showPatientDetails(patient) {

    const section =
        document.getElementById(
            "patientDetailsSection"
        );


    section.style.display = "block";


    // Patient name

    document.getElementById("pName")
        .textContent =
        patient.fullName ?? "-";


    // Patient code

    document.getElementById("pCode")
        .textContent =
        patient.patientCode ?? "-";


    // Age is stored directly because some patients may not know their exact DOB.
    document.getElementById("pAge")
        .textContent = patient.age !== null && patient.age !== undefined
            ? patient.age + " yrs"
            : "-";

    document.getElementById("pDob")
        .textContent = patient.dateOfBirth ?? "-";


    // Gender

    document.getElementById("pGender")
        .textContent =
        patient.gender ?? "-";


    // Blood group

    document.getElementById("pBloodGroup")
        .textContent =
        patient.bloodGroup ?? "-";


    // Phone

    document.getElementById("pPhone")
        .textContent =
        patient.phoneNumber ?? "-";


    // Address

    document.getElementById("pAddress")
        .textContent =
        patient.address ?? "-";


    // Emergency contact

    document.getElementById("pEmergencyContact")
        .textContent =
        patient.emergencyContact ?? "-";


    // Hidden patient ID

    document.getElementById("pId")
        .value =
        patient.id;


    // Reset medical record form

    document
        .getElementById("addRecordForm")
        .reset();


    document
        .getElementById("addRecordError")
        .style.display = "none";


    document
        .getElementById("addRecordSuccess")
        .style.display = "none";


    // Load patient's history

    loadPatientHistory(patient.id);


    // Scroll to patient

    section.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}


// =========================================================
// SHORTEN TEXT FOR THE COLLAPSED CARD PREVIEW
// =========================================================

function shortenText(value, maxLength) {

    if (!value) {
        return "-";
    }

    if (value.length <= maxLength) {
        return value;
    }

    return value.substring(0, maxLength).trim() + "...";
}


// =========================================================
// LOAD MEDICAL HISTORY
// Renders each visit as a collapsed card. Clicking a card
// (or its "View Details" button) expands/collapses it.
// Newest visit first — the backend already returns records
// in that order (findByPatientIdOrderByVisitDateDesc).
// =========================================================

async function loadPatientHistory(patientId) {

    try {

        const history =
            await apiRequest(
                `/doctor/patients/${patientId}/history`
            );


        const container =
            document.getElementById(
                "historyTableBody"
            );

        const emptyState =
            document.getElementById("historyEmpty");


        container.innerHTML = "";


        // No records

        if (!history || history.length === 0) {

            emptyState.style.display = "block";
            return;
        }

        emptyState.style.display = "none";


        const totalVisits = history.length;

        // Newest is first in the array (index 0) — give it the
        // highest visit number so "Visit 1" is always the very first
        // consultation this patient ever had, in the order they happened.
        history.forEach((record, index) => {

            const visitNumber = totalVisits - index;

            const visitDate =
                new Date(
                    record.visitDate
                ).toLocaleString();

            const diagnosis = record.diagnosis ?? "-";
            const medicineName = record.medicineName ?? "-";
            const dosage = record.dosage ?? "-";
            const frequency = record.frequency ?? "-";
            const duration = record.duration ?? "-";
            const instructions = record.instructions ?? "-";
            const visitNotes = record.visitNotes ?? "-";

            const card =
                document.createElement("div");

            card.className = "medical-record-card";

            card.innerHTML = `

                <div class="record-card-header">

                    <div class="record-card-header-text">
                        <div class="record-date">
                            ${visitDate}
                        </div>

                        <div class="record-visit-number">
                            Visit ${visitNumber}
                        </div>

                        <div class="record-preview">
                            <span class="record-preview-label">Diagnosis:</span>
                            ${escapeHtml(shortenText(diagnosis, 45))}
                            &nbsp;•&nbsp;
                            <span class="record-preview-label">Prescription:</span>
                            ${escapeHtml(shortenText(medicineName, 45))}
                        </div>
                    </div>

                    <button type="button" class="record-toggle-btn">
                        View Details
                    </button>

                </div>


                <div class="record-body">

                    <div class="record-field">
                        <span>Diagnosis</span>
                        <strong>${escapeHtml(diagnosis)}</strong>
                    </div>

                    <div class="record-field prescription-record-field">
                        <span>Prescription</span>
                        <div class="prescription-details">
                            <div><small>Medicine</small><strong>${escapeHtml(medicineName)}</strong></div>
                            <div><small>Dosage</small><strong>${escapeHtml(dosage)}</strong></div>
                            <div><small>Frequency</small><strong>${escapeHtml(frequency)}</strong></div>
                            <div><small>Duration</small><strong>${escapeHtml(duration)}</strong></div>
                            <div class="prescription-instructions"><small>When / How to Take</small><strong>${escapeHtml(instructions)}</strong></div>
                        </div>
                    </div>

                    <div class="record-field">
                        <span>Visit Notes</span>
                        <strong>${escapeHtml(visitNotes)}</strong>
                    </div>

                </div>

            `;


            // Toggle expand/collapse — clicking anywhere on the
            // header (or the button inside it) toggles the card.
            const header = card.querySelector(".record-card-header");
            const toggleBtn = card.querySelector(".record-toggle-btn");

            header.addEventListener("click", function () {

                const isExpanded = card.classList.toggle("expanded");

                toggleBtn.textContent =
                    isExpanded ? "Hide Details" : "View Details";
            });


            container.appendChild(card);
        });


    } catch (error) {

        console.error(
            "Failed to load patient history:",
            error
        );
    }
}


// =========================================================
// ADD MEDICAL RECORD
// =========================================================

document
    .getElementById("addRecordForm")
    .addEventListener("submit", async function (e) {

        e.preventDefault();


        const errorBox =
            document.getElementById(
                "addRecordError"
            );


        const successBox =
            document.getElementById(
                "addRecordSuccess"
            );


        errorBox.style.display = "none";

        successBox.style.display = "none";


        const patientId =
            document.getElementById("pId").value;


        const diagnosis =
            document
                .getElementById("diagnosis")
                .value
                .trim();


        const medicineName = document.getElementById("medicineName").value.trim();
        const dosage = document.getElementById("dosage").value.trim();
        const frequency = document.getElementById("frequency").value;
        const duration = document.getElementById("duration").value.trim();
        const instructions = document.getElementById("instructions").value.trim();


        const visitNotes =
            document
                .getElementById("visitNotes")
                .value
                .trim();


        if (!diagnosis) {

            errorBox.textContent =
                "Diagnosis is required.";

            errorBox.style.display =
                "block";

            return;
        }


        if (!medicineName || !dosage || !frequency || !duration) {
            errorBox.textContent = "Medicine, dosage, frequency and duration are required.";
            errorBox.style.display = "block";
            return;
        }


        try {

            await apiRequest(
                `/doctor/patients/${patientId}/records`,
                "POST",
                {
                    diagnosis,
                    medicineName,
                    dosage,
                    frequency,
                    duration,
                    instructions,
                    visitNotes
                }
            );


            successBox.textContent =
                "Medical record added successfully!";

            successBox.style.display =
                "block";


            this.reset();


            // Refresh history

            await loadPatientHistory(
                patientId
            );


            // Refresh today's patient count

            loadDashboardStats();


        } catch (error) {

            errorBox.textContent =
                error.message;

            errorBox.style.display =
                "block";
        }

    });


// =========================================================
// SIMPLE HTML ESCAPE
// Prevents patient-entered text from becoming HTML
// =========================================================

function escapeHtml(value) {

    const div =
        document.createElement("div");

    div.textContent =
        value;

    return div.innerHTML;
}


// =========================================================
// DOCTOR PROFILE
// =========================================================

let currentDoctor = null;
const doctorProfileBtn = document.getElementById("doctorProfileBtn");
const doctorProfilePanel = document.getElementById("doctorProfilePanel");
const doctorProfileOverlay = document.getElementById("doctorProfileOverlay");
const doctorEditOverlay = document.getElementById("doctorEditOverlay");
const doctorPasswordOverlay = document.getElementById("doctorPasswordOverlay");

async function loadDoctorProfile() {
    try {
        currentDoctor = await apiRequest("/doctor/profile");
        renderDoctorProfile(currentDoctor);
    } catch (error) {
        console.error("Failed to load doctor profile:", error);
    }
}

function renderDoctorProfile(doctor) {
    const initial = doctor.fullName ? doctor.fullName.charAt(0).toUpperCase() : "D";
    document.getElementById("doctorProfileAvatar").textContent = initial;
    document.getElementById("doctorProfileName").textContent = doctor.fullName || "Doctor";
    document.getElementById("doctorProfileCode").textContent = doctor.doctorCode || "";

    setDoctorValue("doctorProfileFullName", doctor.fullName);
    setDoctorValue("doctorProfileDoctorCode", doctor.doctorCode);
    setDoctorValue("doctorProfileEmail", doctor.email);
    setDoctorValue("doctorProfilePhone", doctor.phoneNumber);
    setDoctorValue("doctorProfileDob", doctor.dateOfBirth);
    setDoctorValue("doctorProfileGender", doctor.gender);
    setDoctorValue("doctorProfileSpecialization", doctor.specialization);
    setDoctorValue("doctorProfileAddress", doctor.address);

    document.getElementById("doctorEditName").value = doctor.fullName || "";
    document.getElementById("doctorEditEmail").value = doctor.email || "";
    document.getElementById("doctorEditPhone").value = doctor.phoneNumber || "";
    document.getElementById("doctorEditDob").value = doctor.dateOfBirth || "";
    document.getElementById("doctorEditGender").value = doctor.gender || "Male";
    document.getElementById("doctorEditAddress").value = doctor.address || "";
    document.getElementById("doctorEditSpecialization").value = doctor.specialization || "Not provided";
}

function setDoctorValue(id, value) {
    document.getElementById(id).textContent = value || "Not provided";
}

loadDoctorProfile();

doctorProfileBtn.addEventListener("click", () => {
    doctorProfilePanel.classList.add("open");
    doctorProfileOverlay.classList.add("open");
    doctorProfilePanel.setAttribute("aria-hidden", "false");
});

function closeDoctorProfile() {
    doctorProfilePanel.classList.remove("open");
    doctorProfileOverlay.classList.remove("open");
    doctorProfilePanel.setAttribute("aria-hidden", "true");
}

document.getElementById("doctorCloseProfileBtn").addEventListener("click", closeDoctorProfile);
doctorProfileOverlay.addEventListener("click", closeDoctorProfile);

function openDoctorEdit() {
    closeDoctorProfile();
    document.getElementById("doctorProfileError").style.display = "none";
    document.getElementById("doctorProfileSuccess").style.display = "none";
    doctorEditOverlay.classList.add("open");
}
function closeDoctorEdit() { doctorEditOverlay.classList.remove("open"); }
document.getElementById("doctorEditProfileBtn").addEventListener("click", openDoctorEdit);
document.getElementById("doctorCancelEditBtn").addEventListener("click", closeDoctorEdit);
document.getElementById("doctorCancelEditBottom").addEventListener("click", closeDoctorEdit);
doctorEditOverlay.addEventListener("click", event => { if (event.target === doctorEditOverlay) closeDoctorEdit(); });

document.getElementById("doctorEditForm").addEventListener("submit", async function(event) {
    event.preventDefault();
    const errorBox = document.getElementById("doctorProfileError");
    const successBox = document.getElementById("doctorProfileSuccess");
    const button = document.getElementById("doctorSaveProfileBtn");
    errorBox.style.display = "none";
    successBox.style.display = "none";

    const data = {
        fullName: document.getElementById("doctorEditName").value.trim(),
        email: document.getElementById("doctorEditEmail").value.trim(),
        phoneNumber: document.getElementById("doctorEditPhone").value.trim(),
        dateOfBirth: document.getElementById("doctorEditDob").value || null,
        gender: document.getElementById("doctorEditGender").value,
        address: document.getElementById("doctorEditAddress").value.trim()
    };

    if (!data.fullName) { showDoctorError(errorBox, "Full name is required."); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) { showDoctorError(errorBox, "Enter a valid email address."); return; }
    if (!/^[0-9]{10}$/.test(data.phoneNumber)) { showDoctorError(errorBox, "Phone number must be exactly 10 digits."); return; }
    if (data.dateOfBirth && new Date(data.dateOfBirth) > new Date()) { showDoctorError(errorBox, "Date of birth cannot be in the future."); return; }
    if (!data.address) { showDoctorError(errorBox, "Address is required."); return; }

    button.disabled = true;
    button.textContent = "Saving...";
    try {
        await apiRequest("/doctor/profile", "PUT", data);
        await loadDoctorProfile();
        successBox.textContent = "Profile updated successfully.";
        successBox.style.display = "block";
        setTimeout(closeDoctorEdit, 700);
    } catch (error) {
        showDoctorError(errorBox, error.message);
    } finally {
        button.disabled = false;
        button.textContent = "Save Changes";
    }
});

function showDoctorError(box, message) {
    box.textContent = message;
    box.style.display = "block";
}

// Doctor change password
document.querySelectorAll(".dashboard-password-toggle").forEach(toggle => {
    toggle.addEventListener("click", () => {
        const input = document.getElementById(toggle.dataset.target);
        const showing = input.type === "password";
        input.type = showing ? "text" : "password";
        toggle.classList.toggle("showing", showing);
        toggle.setAttribute("aria-label", showing ? "Hide password" : "Show password");
    });
});

function openDoctorPassword() {
    closeDoctorProfile();
    document.getElementById("doctorPasswordError").style.display = "none";
    document.getElementById("doctorPasswordSuccess").style.display = "none";
    document.getElementById("doctorPasswordForm").reset();
    doctorPasswordOverlay.classList.add("open");
}
function closeDoctorPassword() { doctorPasswordOverlay.classList.remove("open"); }
document.getElementById("doctorChangePasswordBtn").addEventListener("click", openDoctorPassword);
document.getElementById("doctorCancelPasswordBtn").addEventListener("click", closeDoctorPassword);
document.getElementById("doctorCancelPasswordBottom").addEventListener("click", closeDoctorPassword);
doctorPasswordOverlay.addEventListener("click", event => { if (event.target === doctorPasswordOverlay) closeDoctorPassword(); });

document.getElementById("doctorPasswordForm").addEventListener("submit", async function(event) {
    event.preventDefault();
    const errorBox = document.getElementById("doctorPasswordError");
    const successBox = document.getElementById("doctorPasswordSuccess");
    const button = document.getElementById("doctorSavePasswordBtn");
    errorBox.style.display = "none";
    successBox.style.display = "none";
    const currentPassword = document.getElementById("doctorCurrentPassword").value;
    const newPassword = document.getElementById("doctorNewPassword").value;
    if (newPassword.length < 6) { showDoctorError(errorBox, "New password must be at least 6 characters."); return; }

    button.disabled = true;
    button.textContent = "Changing...";
    try {
        await apiRequest("/auth/change-password", "POST", { currentPassword, newPassword });
        successBox.textContent = "Password changed successfully.";
        successBox.style.display = "block";
        this.reset();
        setTimeout(closeDoctorPassword, 900);
    } catch (error) {
        showDoctorError(errorBox, error.message);
    } finally {
        button.disabled = false;
        button.textContent = "Change Password";
    }
});

// =========================================================
// LOGOUT
// =========================================================

document.getElementById("doctorLogoutBtn").addEventListener("click", async function () {
    try {
        await apiRequest("/auth/logout", "POST");
    } finally {
        window.location.href = "/login.html";
    }
});
