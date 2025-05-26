import axios, { InternalAxiosRequestConfig, AxiosError } from 'axios'; // Use InternalAxiosRequestConfig

// Access runtime config from window object
const getRuntimeConfig = () => {
  if (typeof window !== 'undefined') {
    console.log('[getRuntimeConfig] Checking window.RUNTIME_CONFIG:', (window as any).RUNTIME_CONFIG);
    if ((window as any).RUNTIME_CONFIG) {
      console.log('[getRuntimeConfig] Using window.RUNTIME_CONFIG');
      return (window as any).RUNTIME_CONFIG;
    } else {
      console.warn('[getRuntimeConfig] window.RUNTIME_CONFIG is not defined or empty.');
    }
  } else {
    console.log('[getRuntimeConfig] window is undefined (server-side or build time).');
  }
  
  // Fallback
  const buildTimeApiUrl = process.env.NEXT_PUBLIC_API_URL;
  console.log(`[getRuntimeConfig] Falling back. Build-time NEXT_PUBLIC_API_URL: ${buildTimeApiUrl}`);
  
  // In Kubernetes, use the internal service name
  if (process.env.NODE_ENV === 'production') {
    return { API_URL: 'http://localhost:8088' };
  }
  
  // For local development
  return { API_URL: buildTimeApiUrl || 'http://localhost:8088' };
};

const currentApiUrl = getRuntimeConfig().API_URL;

// Log the API URL being used and check backend accessibility
if (typeof window !== 'undefined') { // Ensure this only runs on the client
  console.log(`[Frontend Configuration] Using API URL: ${currentApiUrl}`);

  // Function to check backend accessibility
  const checkBackendAccessibility = async (url: string) => {
    try {
      const response = await fetch(url, { method: 'HEAD', mode: 'cors' });
      if (response.ok || response.type === 'opaque' || response.status === 404 || response.status === 403 || response.status === 401) {
        console.log(`[Frontend Configuration] Backend DNS (${new URL(url).hostname}) seems accessible. Status: ${response.status}`);
      } else {
        console.warn(`[Frontend Configuration] Backend DNS (${new URL(url).hostname}) might be inaccessible or misconfigured. Status: ${response.status}`);
      }
    } catch (error) {
      console.error(`[Frontend Configuration] Error checking backend accessibility for ${new URL(url).hostname}:`, error);
    }
  };

  checkBackendAccessibility(currentApiUrl);
}

const axiosInstance = axios.create({
  baseURL: currentApiUrl,
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
