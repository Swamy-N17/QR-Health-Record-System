loadDashboardStats();
loadAdminsList();


// Dashboard statistics
async function loadDashboardStats() {
    try {
        const stats = await apiRequest("/super-admin/dashboard");

        document.getElementById("totalAdmins").textContent = stats.totalAdmins;
        document.getElementById("totalDoctors").textContent = stats.totalDoctors;
        document.getElementById("totalPatients").textContent = stats.totalPatients;

    } catch (error) {
        console.error("Failed to load dashboard stats:", error);
    }
}


// Admin list
async function loadAdminsList() {
    try {
        const admins = await apiRequest("/super-admin/admins");
        const list = document.getElementById("adminsTableBody");

        list.innerHTML = "";
        document.getElementById("adminCount").textContent = admins.length;

        admins.forEach(admin => {

            const item = document.createElement("div");
            item.className = "admin-item";

            const initial = admin.fullName
                ? admin.fullName.charAt(0).toUpperCase()
                : "A";

            item.innerHTML = `
                <div class="admin-avatar">${initial}</div>

                <div class="admin-info">
                    <strong>${admin.fullName}</strong>
                    <span>${admin.email}</span>
                </div>

                <span class="admin-id">#${admin.id}</span>
            `;

            list.appendChild(item);
        });

    } catch (error) {
        console.error("Failed to load admins list:", error);
    }
}


// Password visibility
document.getElementById("passwordToggle").addEventListener("click", function () {

    const password = document.getElementById("password");

    password.type =
        password.type === "password" ? "text" : "password";

    this.textContent =
        password.type === "password" ? "◉" : "⊘";
});


// Create admin
document.getElementById("createAdminForm").addEventListener("submit", async function (e) {

    e.preventDefault();

    const errorBox = document.getElementById("createAdminError");
    const successBox = document.getElementById("createAdminSuccess");
    const button = document.getElementById("createAdminBtn");

    errorBox.style.display = "none";
    successBox.style.display = "none";

    const fullName = document.getElementById("fullName").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    button.classList.add("loading");

    try {

        await apiRequest(
            "/super-admin/admins",
            "POST",
            { fullName, email, password }
        );

        successBox.textContent = "Admin created successfully!";
        successBox.style.display = "block";

        this.reset();

        await Promise.all([
            loadDashboardStats(),
            loadAdminsList()
        ]);

    } catch (error) {

        errorBox.textContent = error.message;
        errorBox.style.display = "block";

    } finally {
        button.classList.remove("loading");
    }
});


// Logout
document.getElementById("logoutBtn").addEventListener("click", async function () {

    try {
        await apiRequest("/auth/logout", "POST");
    } catch (error) {
        console.error("Logout failed:", error);
    }

    window.location.href = "/login.html";
});