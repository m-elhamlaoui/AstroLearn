"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { usePathname } from "next/navigation"
import { useRouter } from "next/navigation"
import { Home, BookOpen, Calendar, MessageSquare, User, ChevronRight, GraduationCap } from "lucide-react"
import Link from "next/link"

export function MinimalNavigation() {
  const [expanded, setExpanded] = useState(false);
  const pathname = usePathname();

  // Determine if the current path matches a nav item
  const isActive = (path: string) => {
    return pathname === path || pathname.startsWith(`${path}/`)
  }

  // Handle hover events
  const handleMouseEnter = () => {
    setExpanded(true)
  }

  const handleMouseLeave = () => {
    setExpanded(false)
  }

  return (
    <nav
      className={`hidden md:block fixed left-0 top-1/2 -translate-y-1/2 bg-gray-800/80 backdrop-blur-md transition-all duration-300 z-[9999] ${ // Added hidden md:block
        expanded ? "w-48 rounded-r-xl" : "w-2 rounded-r-md"
      }`}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <div className="py-4 flex flex-col items-center">
        <div
          className={`absolute -right-3 top-1/2 -translate-y-1/2 transition-all duration-300 ${
            expanded ? "opacity-0 translate-x-2" : "opacity-100"
          }`}
        >
          <div className="bg-gray-700 rounded-full p-1 shadow-md">
            <ChevronRight size={14} className="text-gray-300" />
          </div>
        </div>

        <ul className={`flex flex-col space-y-4 w-full ${expanded ? "opacity-100" : "opacity-0"}`}>
          <li>
            <NavItem
              href="/articles"
              icon={<BookOpen size={20} />}
              label="Articles"
              active={isActive("/articles")}
              expanded={expanded}
            />
          </li>
          <li>
            <NavItem
              href="/missions"
              icon={<Calendar size={20} />}
              label="Missions"
              active={isActive("/missions")}
              expanded={expanded}
            />
          </li>
          <li>
            <NavItem
              href="/courses"
              icon={<GraduationCap size={20} />}
              label="Courses"
              active={isActive("/courses")}
              expanded={expanded}
            />
          </li>
          <li>
            <NavItem
              href="/chatbot"
              icon={<MessageSquare size={20} />}
              label="Chatbot"
              active={isActive("/chatbot")}
              expanded={expanded}
            />
          </li>
          <li>
            <NavItem
              href="/profile/1"
              icon={<User size={20} />}
              label="Profile"
              active={isActive("/profile")}
              expanded={expanded}
            />
          </li>
        </ul>
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
    <Link href={href} className="w-full block">
      <div
        className={`flex items-center px-3 py-2 rounded-full transition-all duration-300 ${
          active ? "bg-gradient-to-r from-purple-500 to-blue-500 text-white" : "text-gray-400 hover:text-white"
        }`}
      >
        <div className="flex items-center justify-center w-6">{icon}</div>
        <span
          className={`ml-3 whitespace-nowrap transition-opacity duration-300 ${expanded ? "opacity-100" : "opacity-0"}`}
        >
          {label}
        </span>
      </div>
    </Link>
  );
}
