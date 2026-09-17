async function getWeather() {

    // Get city name from input
    const city = document.getElementById('cityInput').value.trim();

    // Validate input
    if (!city) {
        alert("Please enter a city name!");
        return;
    }

    try {

        // Create Axios instance
        const api = axios.create({
            baseURL: 'http://localhost:8080',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        // Send POST request to Java backend
        const response = await api.post('/weather', {
            city: city
        });

        // Get response data
        const data = response.data;

        console.log("Weather data:", data);

        // Check for error returned by backend
        if (data.error) {

            document.getElementById('weatherResult').innerHTML = `
                <p style="color: red;">
                    ${data.error}
                </p>
            `;

        } else {

            // Display weather information
            document.getElementById('weatherResult').innerHTML = `
                <h2>Weather in ${data.City}</h2>

                <p>
                    <strong>Temperature:</strong>
                    ${data.Temperature}
                </p>

                <p>
                    <strong>Description:</strong>
                    ${data.Description}
                </p>

                <p>
                    <strong>Humidity:</strong>
                    ${data.Humidity}
                </p>
            `;
        }

    } catch (error) {

        console.error("Error:", error);

        // Handle different types of errors
        if (error.response) {

            console.error("Backend response:", error.response.data);

            document.getElementById('weatherResult').innerHTML = `
                <p style="color: red;">
                    Server Error: ${error.response.status}
                </p>
            `;

        } else if (error.request) {

            document.getElementById('weatherResult').innerHTML = `
                <p style="color: red;">
                    Cannot connect to the backend server.
                    Make sure WeatherServer is running on port 8080.
                </p>
            `;

        } else {

            document.getElementById('weatherResult').innerHTML = `
                <p style="color: red;">
                    Something went wrong. Please try again.
                </p>
            `;
        }
    }
}