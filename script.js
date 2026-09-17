async function getWeather() {
    const city = document.getElementById('cityInput').value;
    if (!city) {
        alert("Please enter a city name!");
        return;
    }

    try {
        const api = axios.create({
            baseURL: window.location.origin.replace(
                /(\.codechef-apps\.com)/,
                '-backend$1'
            ),
            headers: {
                'Content-Type': 'application/json'
            },
        });

        // Use Axios for API request
        const response = await api.post('/weather', { city: city });
        const data = response.data;

        if (data.error) {
            document.getElementById('weatherResult').innerHTML =
                `<p style="color: red;">${data.error}</p>`;
        } else {
            document.getElementById('weatherResult').innerHTML = `
                <h2>Weather in ${data.City}</h2>
                <p>Temperature: ${data.Temperature}</p>
                <p>Description: ${data.Description}</p>
                <p>Humidity: ${data.Humidity}</p>
            `;
        }
    } catch (error) {
        console.error("Error:", error);
    }
}async function getWeather() {
    const city = document.getElementById('cityInput').value;
    if (!city) {
        alert("Please enter a city name!");
        return;
    }

    try {
        const api = axios.create({
            baseURL: window.location.origin.replace(
                /(\.codechef-apps\.com)/,
                '-backend$1'
            ),
            headers: {
                'Content-Type': 'application/json'
            },
        });

        // Use Axios for API request
        const response = await api.post('/weather', { city: city });
        const data = response.data;

        if (data.error) {
            document.getElementById('weatherResult').innerHTML =
                `<p style="color: red;">${data.error}</p>`;
        } else {
            document.getElementById('weatherResult').innerHTML = `
                <h2>Weather in ${data.City}</h2>
                <p>Temperature: ${data.Temperature}°C</p>
                <p>Description: ${data.Description}</p>
                <p>Humidity: ${data.Humidity}%</p>
            `;
        }
    } catch (error) {
        console.error("Error:", error);
    }
}