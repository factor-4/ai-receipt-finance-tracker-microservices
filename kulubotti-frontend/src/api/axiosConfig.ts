import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

// Interceptor to attach the JWT token automatically
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});


api.interceptors.response.use(
  (response) => {
    // If the request was successful, just pass the data through
    return response;
  },
  (error) => {
    // If the backend rejects the request because the token is dead
    if (error.response && error.response.status === 401) {
      console.warn("Session expired. Automatically logging out.");
      
      // Clear the dead token from the closet
      localStorage.removeItem('token');
      
      // Force the browser to navigate back to the login screen.
      // (Using window.location is the safest way to redirect outside of a React component)
      window.location.href = '/login'; 
    }
    
    // Reject the promise so the component's try/catch block still behaves correctly
    return Promise.reject(error);
  }
);
export default api;