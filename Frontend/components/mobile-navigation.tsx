"use client"

import { usePathname } from "next/navigation"
import Link from "next/link"
import { Home, BookOpen, Calendar, MessageSquare, User, GraduationCap } from "lucide-react"
import { cn } from "@/lib/utils" // Assuming you have a utility for class names

const navItems = [
  { href: "/articles", icon: BookOpen, label: "Articles" },
  { href: "/missions", icon: Calendar, label: "Missions" },
  { href: "/courses", icon: GraduationCap, label: "Courses" },
  { href: "/chatbot", icon: MessageSquare, label: "Chatbot" },
  { href: "/profile/1", icon: User, label: "Profile" }, // Assuming profile/1 is the default/user's profile link
];

export function MobileNavigation() {
  const pathname = usePathname();

  // Determine if the current path matches a nav item
  // More specific check for profile to handle /profile/[id]
  const isActive = (path: string) => {
    if (path === "/profile/1") {
      return pathname.startsWith("/profile");
    }
    return pathname === path || pathname.startsWith(`${path}/`);
  };

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 border-t border-gray-800 bg-gray-900/90 backdrop-blur-md md:hidden">
      <ul className="flex justify-around items-center h-16 px-2">
        {navItems.map((item) => (
          <li key={item.href}>
            <Link
              href={item.href}
              className={cn(
                "flex flex-col items-center justify-center p-2 rounded-md transition-colors duration-200 w-16",
                isActive(item.href)
                  ? "text-indigo-400"
                  : "text-gray-400 hover:text-white hover:bg-gray-800"
              )}
            >
              <item.icon size={24} strokeWidth={isActive(item.href) ? 2.5 : 2} />
              <span className="text-xs mt-1">{item.label}</span>
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
