let currentPage = 0;
let currentSearch = "";
let doctorsCache = {};

loadDoctors();


// Load doctors
async function loadDoctors() {
    try {
        const query =
            `/admin/doctors?page=${currentPage}&size=10&search=${encodeURIComponent(currentSearch)}`;

        const result = await apiRequest(query);
        const body = document.getElementById("doctorsBody");

        body.innerHTML = "";
        doctorsCache = {};

        result.content.forEach(d => {

            doctorsCache[d.id] = d;

            const isActive = d.active !== false;

            const statusBadge = isActive
                ? `<span class="status-badge active">Active</span>`
                : `<span class="status-badge inactive">Inactive</span>`;

            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${d.doctorCode ?? "-"}</td>
                <td>${d.fullName ?? "-"}</td>
                <td>${d.email ?? "-"}</td>
                <td>${d.phoneNumber ?? "-"}</td>
                <td>${d.specialization ?? "-"}</td>
                <td>${statusBadge}</td>
                <td>
                    <div class="action-group">
                        <button
                            class="btn-small"
                            onclick="openEditModal(${d.id})"
                        >
                            Edit
                        </button>

                        <button
                            class="btn-small status-action"
                            onclick="toggleStatus(${d.id}, ${!isActive})"
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
        console.error("Failed to load doctors:", error);
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

        <button id="nextBtn" ${current + 1 >= totalPages ? "disabled" : ""}>
            Next
        </button>
    `;

    document.getElementById("prevBtn")
        ?.addEventListener("click", () => {
            currentPage--;
            loadDoctors();
        });

    document.getElementById("nextBtn")
        ?.addEventListener("click", () => {
            currentPage++;
            loadDoctors();
        });
}


// Search
document.getElementById("searchBtn").addEventListener("click", () => {

    currentSearch =
        document.getElementById("searchInput").value.trim();

    currentPage = 0;

    loadDoctors();
});


// Edit modal
function openEditModal(id) {

    const d = doctorsCache[id];

    if (!d) return;

    document.getElementById("editError").style.display = "none";

    document.getElementById("editFullName").value = d.fullName ?? "";
    document.getElementById("editEmail").value = d.email ?? "";
    document.getElementById("editPhone").value = d.phoneNumber ?? "";
    document.getElementById("editAddress").value = d.address ?? "";
    document.getElementById("editSpecialization").value =
        d.specialization ?? "";

    document.getElementById("editDoctorForm").dataset.editingId = id;

    document.getElementById("editModal").classList.add("open");
}


// Close modal
function closeEditModal() {
    document.getElementById("editModal").classList.remove("open");
}

document.getElementById("closeModalBtn")
    .addEventListener("click", closeEditModal);

document.getElementById("cancelModalBtn")
    .addEventListener("click", closeEditModal);


// Close when clicking overlay
document.getElementById("editModal")
    .addEventListener("click", function (e) {
        if (e.target === this) {
            closeEditModal();
        }
    });


// Save doctor
document.getElementById("editDoctorForm")
    .addEventListener("submit", async function (e) {

        e.preventDefault();

        const id = this.dataset.editingId;
        const errorBox = document.getElementById("editError");

        errorBox.style.display = "none";

        const phoneNumber =
            document.getElementById("editPhone").value.trim();

        if (!/^[0-9]{10}$/.test(phoneNumber)) {
            errorBox.textContent =
                "Phone number must be exactly 10 digits.";

            errorBox.style.display = "block";
            return;
        }

        try {

            await apiRequest(
                `/admin/doctors/${id}`,
                "PUT",
                {
                    fullName:
                        document.getElementById("editFullName").value,

                    email:
                        document.getElementById("editEmail").value,

                    phoneNumber,

                    address:
                        document.getElementById("editAddress").value,

                    specialization:
                        document.getElementById("editSpecialization").value
                }
            );

            closeEditModal();

            await loadDoctors();

            showToast("Doctor updated successfully.");

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

    const action = makeActive ? "activate" : "deactivate";

    if (!confirm(`Are you sure you want to ${action} this doctor?`)) {
        return;
    }

    try {

        await apiRequest(
            `/admin/doctors/${id}/status`,
            "PUT",
            { active: makeActive }
        );

        await loadDoctors();

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