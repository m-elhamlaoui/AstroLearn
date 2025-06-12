"use client"

import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';

// Define paths that do not require authentication
const PUBLIC_PATHS = ['/', '/auth', '/articles'];
// Admin-only paths - include all possible admin paths
const ADMIN_PATHS = ['/admin'];

export function useAuthRedirect() {
  const router = useRouter();
  const pathname = usePathname();
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkAuth = () => {
      // Ensure code runs only on the client side
      if (typeof window !== 'undefined') {
        const token = localStorage.getItem('authToken');
        const userRolesStr = localStorage.getItem('userRoles');
        let userRoles = [];
        
        try {
          if (userRolesStr) {
            userRoles = JSON.parse(userRolesStr);
          }
        } catch (error) {
          console.error('[AuthRedirect] Error parsing user roles:', error);
        }

        // Check if user has admin role
        const isAdmin = userRoles.includes('ROLE_ADMIN');
        console.log('[AuthRedirect] User is admin:', isAdmin);
        
        // Check if the current path is one of the public paths
        const isPublicPath = PUBLIC_PATHS.some(publicPath => {
          return pathname === publicPath || pathname.startsWith(publicPath + '/');
        });
        console.log('[AuthRedirect] Current path is public:', isPublicPath, 'Path:', pathname);

        // Check if path is admin-only
        const isAdminPath = pathname.startsWith('/admin');
        console.log('[AuthRedirect] Current path is admin path:', isAdminPath);

        // Handle redirects based on authentication and role
        if (!token && !isPublicPath) {
          // If no token and the current path is not public, redirect to the homepage
          console.log(`[AuthRedirect] No token found, redirecting from ${pathname} to /`);
          router.replace('/');
        } else if (token && isAdminPath && !isAdmin) {
          // If user is not an admin but trying to access admin paths
          console.log(`[AuthRedirect] Non-admin user attempting to access admin path ${pathname}, redirecting to articles`);
          router.replace('/articles');
        }

        setIsLoading(false);
      }
    };

    // Run the check immediately
    checkAuth();

    // Set up an event listener for storage changes (in case token changes in another tab)
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'authToken' || e.key === 'userRoles') {
        checkAuth();
      }
    };

    window.addEventListener('storage', handleStorageChange);
    
    // Clean up the event listener
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, [pathname, router]); // Re-run effect if pathname or router changes

  return { isLoading };
}
