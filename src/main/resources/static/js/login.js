const loginForm = document.getElementById("loginForm");
const passwordInput = document.getElementById("password");
const passwordToggle = document.getElementById("passwordToggle");
const loginButton = document.getElementById("loginButton");
const errorBox = document.getElementById("errorBox");

const roleSelect = document.getElementById("role");
const roleSelectWrap = document.getElementById("roleSelect");
const roleSelectButton = document.getElementById("roleSelectButton");
const roleSelectedText = document.getElementById("roleSelectedText");
const roleSelectedIcon = document.getElementById("roleSelectedIcon");
const roleOptions = [...document.querySelectorAll(".role-option")];

function setRole(value, label, iconMarkup) {
    roleSelect.value = value;
    roleSelectedText.textContent = label;
    roleSelectedText.classList.add("selected");
    roleSelectedIcon.innerHTML = iconMarkup;
    roleOptions.forEach(option => {
        const selected = option.dataset.value === value;
        option.classList.toggle("selected", selected);
        option.setAttribute("aria-selected", selected ? "true" : "false");
    });
}

roleSelectButton.addEventListener("click", () => {
    const open = roleSelectWrap.classList.toggle("open");
    roleSelectButton.setAttribute("aria-expanded", String(open));
});

roleOptions.forEach(option => {
    option.addEventListener("click", () => {
        const icon = option.querySelector(".role-option-icon").innerHTML;
        setRole(option.dataset.value, option.querySelector("span:last-child").textContent, icon);
        roleSelectWrap.classList.remove("open");
        roleSelectButton.setAttribute("aria-expanded", "false");
    });
});

document.addEventListener("click", event => {
    if (!roleSelectWrap.contains(event.target)) {
        roleSelectWrap.classList.remove("open");
        roleSelectButton.setAttribute("aria-expanded", "false");
    }
});

roleSelectButton.addEventListener("keydown", event => {
    if (event.key === "ArrowDown" || event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        roleSelectWrap.classList.add("open");
        roleSelectButton.setAttribute("aria-expanded", "true");
    }
    if (event.key === "Escape") {
        roleSelectWrap.classList.remove("open");
        roleSelectButton.setAttribute("aria-expanded", "false");
    }
});

passwordToggle.addEventListener("click", () => {
    const showPassword = passwordInput.type === "password";
    passwordInput.type = showPassword ? "text" : "password";
    passwordToggle.classList.toggle("showing", showPassword);
    passwordToggle.setAttribute("aria-label", showPassword ? "Hide password" : "Show password");
});

loginForm.addEventListener("submit", async function (e) {
    e.preventDefault();

    const role = document.getElementById("role").value;
    const email = document.getElementById("email").value.trim();
    const password = passwordInput.value;

    errorBox.style.display = "none";
    loginButton.classList.add("loading");

    try {
        const result = await apiRequest("/auth/login", "POST", { email, password, role });

        switch (result.role) {
            case "ROLE_SUPER_ADMIN": window.location.href = "/super-admin-dashboard.html"; break;
            case "ROLE_ADMIN": window.location.href = "/admin-dashboard.html"; break;
            case "ROLE_DOCTOR": window.location.href = "/doctor-dashboard.html"; break;
            case "ROLE_PATIENT": window.location.href = "/patient-dashboard.html"; break;
            default: showError("Unknown role. Please contact the administrator.");
        }
    } catch (error) {
        showError(error.message || "Invalid login details.");
    }
});

function showError(message) {
    errorBox.textContent = message;
    errorBox.style.display = "block";
    loginButton.classList.remove("loading");
}
