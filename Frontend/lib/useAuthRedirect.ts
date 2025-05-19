"use client"

import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';

// Define paths that do not require authentication - per new requirements, only the root path is public
const PUBLIC_PATHS = ['/'];
// Admin-only paths
const ADMIN_PATHS = ['/admin', '/admin/dashboard', '/admin/verification-requests', '/admin/courses'];

export function useAuthRedirect() {
  const router = useRouter();
  const pathname = usePathname();
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
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
      const isAdmin = userRoles.some((role: any) => role.authority === 'ROLE_ADMIN');
      
      // Check if the current path is one of the public paths
      const isPublicPath = PUBLIC_PATHS.some(publicPath => {
        if (publicPath === '/') {
          return pathname === publicPath; // Exact match for homepage
        }
        return pathname.startsWith(publicPath); // Allows for sub-routes of public paths
      });

      // Check if path is admin-only
      const isAdminPath = ADMIN_PATHS.some(adminPath => pathname.startsWith(adminPath));

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
  }, [pathname, router]); // Re-run effect if pathname or router changes

  return { isLoading };
}
