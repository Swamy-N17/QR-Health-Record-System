let currentPatient = null;
loadPatientDashboard();

async function loadPatientDashboard() {
    await loadProfile();
    await loadHistory();
}

async function loadProfile() {
    try {
        const patient = await apiRequest("/patient/profile");
        currentPatient = patient;
        renderProfile(patient);
        loadHealthCard();
    } catch (error) {
        console.error("Failed to load profile:", error);
    }
}

function renderProfile(patient) {
    const firstLetter = patient.fullName ? patient.fullName.charAt(0).toUpperCase() : "P";
    document.getElementById("profileAvatar").textContent = firstLetter;
    document.getElementById("profileName").textContent = patient.fullName || "Patient";
    document.getElementById("profileCode").textContent = patient.patientCode || "";

    setProfileValue("profileFullName", patient.fullName);
    setProfileValue("profilePatientCode", patient.patientCode);
    setProfileValue("profileEmail", patient.email);
    setProfileValue("profilePhone", patient.phoneNumber);
    setProfileValue("profileAge", patient.age !== null && patient.age !== undefined ? `${patient.age} years` : null);
    setProfileValue("profileDob", patient.dateOfBirth);
    setProfileValue("profileGender", patient.gender);
    setProfileValue("profileBloodGroup", patient.bloodGroup);
    setProfileValue("profileAddress", patient.address);
    setProfileValue("profileEmergency", patient.emergencyContact);

    document.getElementById("newFullName").value = patient.fullName || "";
    document.getElementById("newEmail").value = patient.email || "";
    document.getElementById("newPhone").value = patient.phoneNumber || "";
    document.getElementById("newAge").value = patient.age ?? "";
    document.getElementById("newDob").value = patient.dateOfBirth || "";
    document.getElementById("newGender").value = patient.gender || "Male";
    document.getElementById("newAddress").value = patient.address || "";
    document.getElementById("newEmergencyContact").value = patient.emergencyContact || "";
    document.getElementById("profileEditBloodGroup").value = patient.bloodGroup || "Not provided";
}

function setProfileValue(id, value) {
    document.getElementById(id).textContent = value || "Not provided";
}

function loadHealthCard() {
    const image = document.getElementById("healthCardImage");
    const error = document.getElementById("healthCardError");
    image.src = `/api/patient/health-card?_=${Date.now()}`;
    image.onload = () => { image.style.display = "block"; error.style.display = "none"; };
    image.onerror = () => { image.style.display = "none"; error.style.display = "block"; };
}

async function loadHistory() {
    try {
        const history = await apiRequest("/patient/history");
        const container = document.getElementById("historyCards");
        container.innerHTML = "";
        if (!history.length) {
            container.innerHTML = `<div class="empty-history"><strong>No medical history yet</strong><span>Your recorded visits will appear here.</span></div>`;
            return;
        }
        history.forEach((record, index) => {
            const card = document.createElement("article");
            card.className = "medical-record-card";
            const visitDate = record.visitDate ? new Date(record.visitDate).toLocaleString() : "Date not available";
            card.innerHTML = `
                <button type="button" class="medical-record-header" aria-expanded="${index === 0}">
                    <span><strong>Visit on ${escapeHtml(visitDate)}</strong><small>Dr. ${escapeHtml(record.doctorName || "Not available")}</small></span>
                    <span class="record-chevron">${index === 0 ? "−" : "+"}</span>
                </button>
                <div class="medical-record-body" ${index === 0 ? "" : "hidden"}>
                    <div class="record-field"><span>Diagnosis</span><p>${escapeHtml(record.diagnosis || "Not provided")}</p></div>
                    <div class="record-field prescription-record-field">
                        <span>Prescription</span>
                        <div class="prescription-details">
                            <div><small>Medicine</small><strong>${escapeHtml(record.medicineName || "Not provided")}</strong></div>
                            <div><small>Dosage</small><strong>${escapeHtml(record.dosage || "Not provided")}</strong></div>
                            <div><small>Frequency</small><strong>${escapeHtml(record.frequency || "Not provided")}</strong></div>
                            <div><small>Duration</small><strong>${escapeHtml(record.duration || "Not provided")}</strong></div>
                            <div class="prescription-instructions"><small>When / How to Take</small><strong>${escapeHtml(record.instructions || "Not provided")}</strong></div>
                        </div>
                    </div>
                    <div class="record-field"><span>Visit Notes</span><p>${escapeHtml(record.visitNotes || "No notes")}</p></div>
                </div>`;
            const header = card.querySelector(".medical-record-header");
            const body = card.querySelector(".medical-record-body");
            const chevron = card.querySelector(".record-chevron");
            header.addEventListener("click", () => {
                const isOpen = !body.hidden;
                body.hidden = isOpen;
                header.setAttribute("aria-expanded", String(!isOpen));
                chevron.textContent = isOpen ? "+" : "−";
            });
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Failed to load medical history:", error);
    }
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

const profileBtn = document.getElementById("profileBtn");
const profilePanel = document.getElementById("profilePanel");
const profileOverlay = document.getElementById("profileOverlay");
function openProfile() { profilePanel.classList.add("open"); profileOverlay.classList.add("open"); profilePanel.setAttribute("aria-hidden", "false"); }
function closeProfile() { profilePanel.classList.remove("open"); profileOverlay.classList.remove("open"); profilePanel.setAttribute("aria-hidden", "true"); }
profileBtn.addEventListener("click", openProfile);
document.getElementById("closeProfileBtn").addEventListener("click", closeProfile);
profileOverlay.addEventListener("click", closeProfile);

const editProfileOverlay = document.getElementById("editProfileOverlay");
function openEditProfile() {
    closeProfile();
    document.getElementById("contactError").style.display = "none";
    document.getElementById("contactSuccess").style.display = "none";
    editProfileOverlay.classList.add("open");
}
function closeEditProfile() { editProfileOverlay.classList.remove("open"); }
document.getElementById("editProfileBtn").addEventListener("click", openEditProfile);
document.getElementById("cancelEditBtn").addEventListener("click", closeEditProfile);
document.getElementById("cancelEditBtnBottom").addEventListener("click", closeEditProfile);
editProfileOverlay.addEventListener("click", event => { if (event.target === editProfileOverlay) closeEditProfile(); });

document.getElementById("editContactForm").addEventListener("submit", async function (event) {
    event.preventDefault();
    const errorBox = document.getElementById("contactError");
    const successBox = document.getElementById("contactSuccess");
    const button = document.getElementById("updateContactBtn");
    errorBox.style.display = "none";
    successBox.style.display = "none";

    const data = {
        fullName: document.getElementById("newFullName").value.trim(),
        email: document.getElementById("newEmail").value.trim(),
        phoneNumber: document.getElementById("newPhone").value.trim(),
        age: Number(document.getElementById("newAge").value),
        dateOfBirth: document.getElementById("newDob").value || null,
        gender: document.getElementById("newGender").value,
        address: document.getElementById("newAddress").value.trim(),
        emergencyContact: document.getElementById("newEmergencyContact").value.trim() || null
    };

    if (!data.fullName) { showInlineError(errorBox, "Full name is required."); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) { showInlineError(errorBox, "Enter a valid email address."); return; }
    if (!/^[0-9]{10}$/.test(data.phoneNumber)) { showInlineError(errorBox, "Phone number must be exactly 10 digits."); return; }
    if (!Number.isInteger(data.age) || data.age < 0 || data.age > 130) { showInlineError(errorBox, "Enter a valid age."); return; }
    if (data.dateOfBirth && new Date(data.dateOfBirth) > new Date()) { showInlineError(errorBox, "Date of birth cannot be in the future."); return; }
    if (!data.address) { showInlineError(errorBox, "Address is required."); return; }
    if (data.emergencyContact && !/^[0-9]{10}$/.test(data.emergencyContact)) { showInlineError(errorBox, "Emergency contact must be exactly 10 digits."); return; }

    button.disabled = true;
    button.textContent = "Saving...";
    try {
        await apiRequest("/patient/profile", "PUT", data);
        await loadProfile();
        successBox.textContent = "Profile updated successfully.";
        successBox.style.display = "block";
        setTimeout(closeEditProfile, 700);
    } catch (error) {
        showInlineError(errorBox, error.message);
    } finally {
        button.disabled = false;
        button.textContent = "Save Changes";
    }
});

function showInlineError(box, message) { box.textContent = message; box.style.display = "block"; }

document.querySelectorAll(".dashboard-password-toggle").forEach(toggle => {
    toggle.addEventListener("click", () => {
        const input = document.getElementById(toggle.dataset.target);
        const showing = input.type === "password";
        input.type = showing ? "text" : "password";
        toggle.classList.toggle("showing", showing);
        toggle.setAttribute("aria-label", showing ? "Hide password" : "Show password");
    });
});

const passwordOverlay = document.getElementById("passwordOverlay");
function openPassword() {
    closeProfile();
    document.getElementById("passwordError").style.display = "none";
    document.getElementById("passwordSuccess").style.display = "none";
    document.getElementById("changePasswordForm").reset();
    passwordOverlay.classList.add("open");
}
function closePassword() { passwordOverlay.classList.remove("open"); }
document.getElementById("changePasswordBtn").addEventListener("click", openPassword);
document.getElementById("cancelPasswordBtn").addEventListener("click", closePassword);
document.getElementById("cancelPasswordBtnBottom").addEventListener("click", closePassword);
passwordOverlay.addEventListener("click", event => { if (event.target === passwordOverlay) closePassword(); });

document.getElementById("changePasswordForm").addEventListener("submit", async function (event) {
    event.preventDefault();
    const errorBox = document.getElementById("passwordError");
    const successBox = document.getElementById("passwordSuccess");
    const button = document.getElementById("savePasswordBtn");
    errorBox.style.display = "none";
    successBox.style.display = "none";
    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;
    if (newPassword.length < 6) { showInlineError(errorBox, "New password must be at least 6 characters."); return; }
    button.disabled = true;
    button.textContent = "Changing...";
    try {
        await apiRequest("/auth/change-password", "POST", { currentPassword, newPassword });
        successBox.textContent = "Password changed successfully.";
        successBox.style.display = "block";
        this.reset();
        setTimeout(closePassword, 900);
    } catch (error) {
        showInlineError(errorBox, error.message);
    } finally {
        button.disabled = false;
        button.textContent = "Change Password";
    }
});

async function logout() {
    try { await apiRequest("/auth/logout", "POST"); }
    finally { window.location.href = "/login.html"; }
}
document.getElementById("profileLogoutBtn").addEventListener("click", logout);
