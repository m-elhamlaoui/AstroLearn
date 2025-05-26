import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

/**
 * A hook to handle authentication redirection
 * Redirects to login page if user is not authenticated
 * @returns Object containing isAuthenticated state
 */
export function useAuthRedirect() {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  
  useEffect(() => {
    // Check if we're in the browser
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('authToken');
      
      if (!token) {
        console.log('[Auth] No authentication token found, redirecting to login');
        router.push('/login');
        setIsAuthenticated(false);
      } else {
        setIsAuthenticated(true);
      }
    }
  }, [router]);
  
  return { isAuthenticated };
}
