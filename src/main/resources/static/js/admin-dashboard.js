loadDashboardStats();

async function loadDashboardStats() {
    try {

        const stats =
            await apiRequest("/admin/dashboard");

        document.getElementById("totalDoctors")
            .textContent = stats.totalDoctors;

        document.getElementById("totalPatients")
            .textContent = stats.totalPatients;


        const doctorsBody =
            document.getElementById("recentDoctorsBody");

        doctorsBody.innerHTML = "";

        stats.recentDoctors.forEach(d => {

            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${d.doctorCode ?? "-"}</td>
                <td>${d.fullName}</td>
                <td>${d.specialization}</td>
            `;

            doctorsBody.appendChild(row);
        });


        const patientsBody =
            document.getElementById("recentPatientsBody");

        patientsBody.innerHTML = "";

        stats.recentPatients.forEach(p => {

            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${p.patientCode ?? "-"}</td>
                <td>${p.fullName}</td>
                <td>${p.phoneNumber}</td>
            `;

            patientsBody.appendChild(row);
        });

    } catch (error) {

        console.error(
            "Failed to load dashboard stats:",
            error
        );
    }
}


document.getElementById("logoutBtn")
    .addEventListener("click", async function () {

        try {
            await apiRequest("/auth/logout", "POST");
        } finally {
            window.location.href = "/login.html";
        }
    });