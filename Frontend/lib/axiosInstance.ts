import axios, { InternalAxiosRequestConfig, AxiosError } from 'axios'; // Use InternalAxiosRequestConfig

const axiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8090', // Default to localhost:8090
});

axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => { // Use InternalAxiosRequestConfig
    // console.log(`[Axios Interceptor] Requesting URL: ${config.url}`); 
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('authToken');
      // console.log(`[Axios Interceptor] Token from localStorage ('authToken'): ${token ? 'Found' : 'Not Found'}`); 
      if (token) {
        // InternalAxiosRequestConfig guarantees headers object exists
        config.headers.Authorization = `Bearer ${token}`; 
        // console.log(`[Axios Interceptor] Set Authorization header: Bearer ${token.substring(0, 10)}...`); 
      } else {
         console.warn(`[Axios Interceptor] No authToken found in localStorage for URL: ${config.url}`); 
      }
    } else {
       console.warn("[Axios Interceptor] window is undefined, cannot access localStorage.");
    }
    // console.log("[Axios Interceptor] Final Headers:", config.headers); // Log final headers
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

export const clearAuthToken = () => {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('authToken');
    // Optionally, you could also clear the header on the instance if it was set directly,
    // but the interceptor handles this by not finding the token.
    // delete axiosInstance.defaults.headers.common['Authorization']; // Not strictly necessary with the current interceptor
    console.log("[Auth] authToken cleared from localStorage.");
  }
};

export default axiosInstance;
