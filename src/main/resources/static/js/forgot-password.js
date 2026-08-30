document.getElementById("forgotPasswordForm")
    .addEventListener("submit", async function (e) {

    e.preventDefault();

    const email =
        document.getElementById("email").value.trim();

    const messageBox =
        document.getElementById("messageBox");

    try {

        const result = await apiRequest(
            "/auth/forgot-password",
            "POST",
            { email }
        );

        messageBox.style.background = "#f0fdf4";
        messageBox.style.color = "#15803d";

        messageBox.textContent = result.message;
        messageBox.style.display = "block";

        this.reset();

    } catch (error) {

        messageBox.style.background = "#fef2f2";
        messageBox.style.color = "#b91c1c";

        messageBox.textContent = error.message;
        messageBox.style.display = "block";
    }
});