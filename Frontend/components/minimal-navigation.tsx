"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { usePathname, useRouter } from "next/navigation" // Combined useRouter import
import { Home, BookOpen, Calendar, MessageSquare, User, ChevronRight, GraduationCap, LogOut } from "lucide-react" // Added LogOut
import Link from "next/link"
import { clearAuthToken } from "@/lib/axiosInstance"; // Import clearAuthToken

export function MinimalNavigation() {
  const [expanded, setExpanded] = useState(false);
  const [userId, setUserId] = useState<string | null>(null);
  const pathname = usePathname();
  const router = useRouter(); // Initialize router

  useEffect(() => {
    const storedUserId = localStorage.getItem("userId"); 
    if (storedUserId) {
      setUserId(storedUserId);
    }
  }, []);

  const isActive = (path: string) => {
    return pathname === path || pathname.startsWith(`${path}/`)
  }

  const handleMouseEnter = () => {
    setExpanded(true)
  }

  const handleMouseLeave = () => {
    setExpanded(false)
  }

  const handleLogout = () => {
    clearAuthToken();
    localStorage.removeItem("userId"); 
    localStorage.removeItem("userRole"); // Also clear userRole if it's stored
    setUserId(null); 
    router.push("/"); // Redirect to homepage, or /auth/signin
    // Optionally, could do a full page reload to ensure all states are reset:
    // window.location.href = '/'; 
  };

  return (
    <nav
      className={`hidden md:flex fixed left-0 top-1/2 -translate-y-1/2 bg-gray-800/80 backdrop-blur-md transition-all duration-300 z-[9999] flex-col justify-between rounded-r-xl ${
        expanded ? "w-48 py-6" : "w-12 py-4 items-center" // Adjusted padding and width for collapsed state
      }`}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      style={{ height: 'auto', minHeight: '200px' }} // Ensure it has some height
    >
      {/* Expansion Handle - only visible when collapsed */}
      {!expanded && (
        <div
          className="absolute -right-2.5 top-1/2 -translate-y-1/2 bg-gray-700 rounded-full p-0.5 shadow-md cursor-pointer"
          onClick={handleMouseEnter} // Allow click to expand as well
        >
          <ChevronRight size={14} className="text-gray-300" />
        </div>
      )}
      
      <ul className={`flex flex-col space-y-2 w-full ${expanded ? "px-3" : "px-0 items-center"}`}> {/* Adjusted padding */}
        {/* Navigation Items */}
        <NavItem
          href="/articles"
          icon={<BookOpen size={20} />}
          label="Articles"
          active={isActive("/articles")}
          expanded={expanded}
        />
        <NavItem
          href="/missions"
          icon={<Calendar size={20} />}
          label="Missions"
          active={isActive("/missions")}
          expanded={expanded}
        />
        <NavItem
          href="/courses"
          icon={<GraduationCap size={20} />}
          label="Courses"
          active={isActive("/courses")}
          expanded={expanded}
        />
        <NavItem
          href="/chatbot"
          icon={<MessageSquare size={20} />}
          label="Chatbot"
          active={isActive("/chatbot")}
          expanded={expanded}
        />
        <NavItem
          href={userId ? `/profile/${userId}` : "/profile/me"}
          icon={<User size={20} />}
          label="Profile"
          active={isActive("/profile")}
          expanded={expanded}
        />
      </ul>

      {/* Logout Button - always at the bottom */}
      <div className={`w-full ${expanded ? "px-3" : "px-0 flex justify-center"} mt-4 pt-2 border-t border-gray-700/50`}>
        <button
          onClick={handleLogout}
          className="w-full block"
          title="Logout"
        >
          <div
            className={`flex items-center py-2 rounded-md transition-all duration-300 text-gray-400 hover:text-red-400 hover:bg-red-900/40 ${expanded ? "px-3" : "justify-center"}`}
          >
            <LogOut size={20} />
            <span
              className={`ml-3 whitespace-nowrap transition-opacity duration-300 ${expanded ? "opacity-100" : "opacity-0"}`}
            >
              Logout
            </span>
          </div>
        </button>
      </div>
    </nav>
  )
}

interface NavItemProps {
  href: string
  icon: React.ReactNode
  label: string
  active: boolean
  expanded: boolean
}

function NavItem({ href, icon, label, active, expanded }: NavItemProps) {
  return (
    <Link href={href} className="w-full block" title={label}>
      <div
        className={`flex items-center py-2 rounded-md transition-all duration-300 ${
          active ? "bg-gradient-to-r from-purple-600 to-blue-600 text-white shadow-lg" : "text-gray-400 hover:text-white hover:bg-gray-700/50"
        } ${expanded ? "px-3" : "justify-center w-10 h-10"}`} // Centered icon when collapsed
      >
        {icon}
        <span
          className={`ml-3 whitespace-nowrap transition-opacity duration-300 ${expanded ? "opacity-100" : "opacity-0 sr-only"}`} // sr-only for accessibility when collapsed
        >
          {label}
        </span>
      </div>
    </Link>
  );
}
