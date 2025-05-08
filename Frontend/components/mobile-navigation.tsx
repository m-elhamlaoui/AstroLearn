"use client"

import { usePathname } from "next/navigation"
import Link from "next/link"
import { Home, BookOpen, Calendar, MessageSquare, User, GraduationCap } from "lucide-react"
import { useState, useEffect } from "react"; // Import hooks
import { cn } from "@/lib/utils" // Assuming you have a utility for class names

// Define nav items structure
interface NavItem {
  href: string;
  icon: React.ElementType; // Use React.ElementType for component types
  label: string;
}

export function MobileNavigation() {
  const pathname = usePathname();
  const [userId, setUserId] = useState<string | null>(null);

  useEffect(() => {
    // Fetch userId from localStorage on the client side
    const storedUserId = localStorage.getItem("userId"); // Assuming 'userId' is the key
    if (storedUserId) {
      setUserId(storedUserId);
    }
  }, []);

  // Define nav items dynamically based on userId
  const navItems: NavItem[] = [
    { href: "/articles", icon: BookOpen, label: "Articles" },
    { href: "/missions", icon: Calendar, label: "Missions" },
    { href: "/courses", icon: GraduationCap, label: "Courses" },
    { href: "/chatbot", icon: MessageSquare, label: "Chatbot" },
    { href: userId ? `/profile/${userId}` : "/profile/me", icon: User, label: "Profile" }, // Dynamic profile link
  ];

  // Determine if the current path matches a nav item
  // More specific check for profile to handle /profile/[id] or /profile/me
  const isActive = (path: string, isProfileLink: boolean = false) => {
    if (isProfileLink) {
      // Check if current path starts with /profile/ and is either /me or matches the dynamic userId
      return pathname.startsWith("/profile/") && (pathname === `/profile/${userId}` || pathname === "/profile/me");
    }
    return pathname === path || pathname.startsWith(`${path}/`);
  };

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 border-t border-gray-800 bg-gray-900/90 backdrop-blur-md md:hidden">
      <ul className="flex justify-around items-center h-16 px-2">
        {navItems.map((item) => {
          const active = isActive(item.href, item.label === "Profile");
          return (
            <li key={item.label}> {/* Use label or a more stable key if href changes */}
              <Link
                href={item.href}
                className={cn(
                  "flex flex-col items-center justify-center p-2 rounded-md transition-colors duration-200 w-16",
                  active
                    ? "text-indigo-400"
                    : "text-gray-400 hover:text-white hover:bg-gray-800"
                )}
              >
                <item.icon size={24} strokeWidth={active ? 2.5 : 2} />
                <span className="text-xs mt-1">{item.label}</span>
              </Link>
            </li>
         );
        })}
      </ul>
      {/* Removed extraneous closing tags below */}
    </nav>
  );
}
