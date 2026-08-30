let currentPage = 0;
let currentSearch = "";
let patientsCache = {};

loadPatients();


// Load patients
async function loadPatients() {
    try {
        const query =
            `/admin/patients?page=${currentPage}&size=10&search=${encodeURIComponent(currentSearch)}`;

        const result = await apiRequest(query);
        const body = document.getElementById("patientsBody");

        body.innerHTML = "";
        patientsCache = {};

        result.content.forEach(p => {

            patientsCache[p.id] = p;

            const isActive = p.active !== false;

            const statusBadge = isActive
                ? `<span class="status-badge active">Active</span>`
                : `<span class="status-badge inactive">Inactive</span>`;

            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${p.patientCode ?? "-"}</td>
                <td>${p.fullName ?? "-"}</td>
                <td>${p.email ?? "-"}</td>
                <td>${p.phoneNumber ?? "-"}</td>
                <td>${p.bloodGroup ?? "-"}</td>
                <td>${statusBadge}</td>
                <td>
                    <div class="action-group">

                        <button
                            class="btn-small"
                            onclick="openEditModal(${p.id})"
                        >
                            Edit
                        </button>

                        <button
                            class="btn-small status-action"
                            onclick="toggleStatus(${p.id}, ${!isActive})"
                        >
                            ${isActive ? "Deactivate" : "Activate"}
                        </button>

                    </div>
                </td>
            `;

            body.appendChild(row);
        });

        renderPagination(result.currentPage, result.totalPages);

    } catch (error) {
        console.error("Failed to load patients:", error);
    }
}


// Pagination
function renderPagination(current, totalPages) {

    const container = document.getElementById("pagination");

    container.innerHTML = `
        <button id="prevBtn" ${current === 0 ? "disabled" : ""}>
            Previous
        </button>

        <span>
            Page ${current + 1} of ${Math.max(totalPages, 1)}
        </span>

        <button
            id="nextBtn"
            ${current + 1 >= totalPages ? "disabled" : ""}
        >
            Next
        </button>
    `;

    document.getElementById("prevBtn")
        ?.addEventListener("click", () => {
            currentPage--;
            loadPatients();
        });

    document.getElementById("nextBtn")
        ?.addEventListener("click", () => {
            currentPage++;
            loadPatients();
        });
}


// Search
document.getElementById("searchBtn")
    .addEventListener("click", () => {

        currentSearch =
            document.getElementById("searchInput").value.trim();

        currentPage = 0;

        loadPatients();
    });


// Edit modal
function openEditModal(id) {

    const p = patientsCache[id];

    if (!p) return;

    document.getElementById("editError").style.display = "none";

    document.getElementById("editFullName").value =
        p.fullName ?? "";

    document.getElementById("editEmail").value =
        p.email ?? "";

    document.getElementById("editPhone").value =
        p.phoneNumber ?? "";

    document.getElementById("editDob").value =
        p.dateOfBirth ?? "";

    document.getElementById("editBloodGroup").value =
        p.bloodGroup ?? "";

    document.getElementById("editAddress").value =
        p.address ?? "";

    document.getElementById("editEmergencyContact").value =
        p.emergencyContact ?? "";

    document.getElementById("editPatientForm")
        .dataset.editingId = id;

    document.getElementById("editModal")
        .classList.add("open");
}


// Close modal
function closeEditModal() {
    document.getElementById("editModal")
        .classList.remove("open");
}

document.getElementById("closeModalBtn")
    .addEventListener("click", closeEditModal);

document.getElementById("cancelModalBtn")
    .addEventListener("click", closeEditModal);


// Close by clicking overlay
document.getElementById("editModal")
    .addEventListener("click", function (e) {

        if (e.target === this) {
            closeEditModal();
        }
    });


// Save patient
document.getElementById("editPatientForm")
    .addEventListener("submit", async function (e) {

        e.preventDefault();

        const id = this.dataset.editingId;
        const errorBox = document.getElementById("editError");

        errorBox.style.display = "none";

        const phoneNumber =
            document.getElementById("editPhone").value.trim();

        const emergencyContact =
            document.getElementById("editEmergencyContact")
                .value.trim();

        if (!/^[0-9]{10}$/.test(phoneNumber)) {

            errorBox.textContent =
                "Phone number must be exactly 10 digits.";

            errorBox.style.display = "block";
            return;
        }

        if (!/^[0-9]{10}$/.test(emergencyContact)) {

            errorBox.textContent =
                "Emergency contact must be exactly 10 digits.";

            errorBox.style.display = "block";
            return;
        }

        try {

            await apiRequest(
                `/admin/patients/${id}`,
                "PUT",
                {
                    fullName:
                        document.getElementById("editFullName").value,

                    email:
                        document.getElementById("editEmail").value,

                    phoneNumber,

                    dateOfBirth:
                        document.getElementById("editDob").value,

                    bloodGroup:
                        document.getElementById("editBloodGroup").value,

                    address:
                        document.getElementById("editAddress").value,

                    emergencyContact
                }
            );

            closeEditModal();

            await loadPatients();

            showToast("Patient updated successfully.");

        } catch (error) {

            errorBox.textContent = error.message;
            errorBox.style.display = "block";
        }
    });


// Toast
function showToast(message) {

    const toast = document.createElement("div");

    toast.className = "toast";
    toast.textContent = message;

    document.body.appendChild(toast);

    setTimeout(() => toast.remove(), 2500);
}


// Activate / deactivate
async function toggleStatus(id, makeActive) {

    const action = makeActive
        ? "activate"
        : "deactivate";

    if (!confirm(
        `Are you sure you want to ${action} this patient?`
    )) {
        return;
    }

    try {

        await apiRequest(
            `/admin/patients/${id}/status`,
            "PUT",
            { active: makeActive }
        );

        await loadPatients();

    } catch (error) {
        alert(error.message);
    }
}


// Logout
document.getElementById("logoutBtn")
    .addEventListener("click", async function () {

        try {
            await apiRequest("/auth/logout", "POST");
        } finally {
            window.location.href = "/login.html";
        }
    });