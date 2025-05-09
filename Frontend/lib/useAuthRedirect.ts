"use client"

import { useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';

// Define paths that do not require authentication
const PUBLIC_PATHS = ['/', '/auth/signin', '/auth/signup']; 
// Add any other public paths, e.g., '/about', '/contact'

export function useAuthRedirect() {
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    // Ensure code runs only on the client side
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('authToken');
      
      // Check if the current path is one of the public paths
      // A more robust solution might involve regex or more complex matching if paths are dynamic public paths
      const isPublicPath = PUBLIC_PATHS.some(publicPath => {
        if (publicPath === '/') {
          return pathname === publicPath; // Exact match for homepage
        }
        return pathname.startsWith(publicPath); // Allows for sub-routes of public paths like /auth/reset-password
      });

      if (!token && !isPublicPath) {
        // If no token and the current path is not public, redirect to the homepage
        console.log(`[AuthRedirect] No token found, redirecting from ${pathname} to /`);
        router.replace('/'); // Use replace to avoid adding to history stack
      }
    }
  }, [pathname, router]); // Re-run effect if pathname or router changes
}
