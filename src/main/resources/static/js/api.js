// Base helper for all API calls.
// Every page includes this file before its own page-specific JS.

const API_BASE = "/api";

async function apiRequest(endpoint, method = "GET", body = null) {
    const options = {
        method: method,
        headers: { "Content-Type": "application/json" },
        credentials: "include"
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(API_BASE + endpoint, options);

    if (!response.ok) {
        let errorMessage = "Something went wrong.";
        try {
            const errorData = await response.json();

            if (errorData.error) {
                // Standard single-error responses, e.g. {"error": "..."}
                errorMessage = errorData.error;
            } else if (errorData.message) {
                errorMessage = errorData.message;
            } else if (typeof errorData === "object") {
                // Validation error responses: a flat map of fieldName -> message
                const fieldMessages = Object.values(errorData);
                if (fieldMessages.length > 0) {
                    errorMessage = fieldMessages.join(" | ");
                }
            }

        } catch (e) {
            // response wasn't JSON, keep default message
        }
        throw new Error(errorMessage);
    }

    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return response.json();
    }
    return null;
}