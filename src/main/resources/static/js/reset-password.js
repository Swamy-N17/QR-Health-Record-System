// Get token from URL
const urlParams =
    new URLSearchParams(window.location.search);

const token = urlParams.get("token");

const form =
    document.getElementById("resetPasswordForm");

const messageBox =
    document.getElementById("messageBox");


// Check token
if (!token) {

    messageBox.style.background = "#fef2f2";
    messageBox.style.color = "#b91c1c";

    messageBox.textContent =
        "Invalid reset link. Please request a new one.";

    messageBox.style.display = "block";

    form.style.display = "none";
}


// Show / hide new password
document.getElementById("toggleNewPassword")
    .addEventListener("click", function () {

        const input =
            document.getElementById("newPassword");

        const hidden =
            input.type === "password";

        input.type = hidden ? "text" : "password";

        this.textContent =
            hidden ? "⊘" : "◉";

        this.setAttribute(
            "aria-label",
            hidden ? "Hide password" : "Show password"
        );
    });


// Show / hide confirm password
document.getElementById("toggleConfirmPassword")
    .addEventListener("click", function () {

        const input =
            document.getElementById("confirmPassword");

        const hidden =
            input.type === "password";

        input.type = hidden ? "text" : "password";

        this.textContent =
            hidden ? "⊘" : "◉";

        this.setAttribute(
            "aria-label",
            hidden ? "Hide password" : "Show password"
        );
    });


// Reset password
form.addEventListener("submit", async function (e) {

    e.preventDefault();

    const newPassword =
        document.getElementById("newPassword").value;

    const confirmPassword =
        document.getElementById("confirmPassword").value;


    messageBox.style.display = "none";


    // Check password match
    if (newPassword !== confirmPassword) {

        messageBox.style.background = "#fef2f2";
        messageBox.style.color = "#b91c1c";

        messageBox.textContent =
            "Passwords do not match.";

        messageBox.style.display = "block";

        return;
    }


    // Check minimum length
    if (newPassword.length < 6) {

        messageBox.style.background = "#fef2f2";
        messageBox.style.color = "#b91c1c";

        messageBox.textContent =
            "Password must be at least 6 characters.";

        messageBox.style.display = "block";

        return;
    }


    try {

        const result = await apiRequest(
            "/auth/reset-password",
            "POST",
            {
                token,
                newPassword
            }
        );


        messageBox.style.background = "#f0fdf4";
        messageBox.style.color = "#15803d";

        messageBox.textContent =
            result.message +
            " Redirecting to login...";

        messageBox.style.display = "block";


        form.reset();


        setTimeout(() => {
            window.location.href = "/login.html";
        }, 2000);


    } catch (error) {

        messageBox.style.background = "#fef2f2";
        messageBox.style.color = "#b91c1c";

        messageBox.textContent =
            error.message;

        messageBox.style.display = "block";
    }
});