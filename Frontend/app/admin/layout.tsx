"use client"

import { useEffect } from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { useAuthRedirect } from "@/lib/useAuthRedirect"
import { BloomingStars } from "@/components/blooming-stars"

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const { isLoading } = useAuthRedirect()
  const pathname = usePathname()

  // Nav items with paths and labels
  const navItems = [
    { path: "/admin/dashboard", label: "Dashboard" },
    { path: "/admin/verification-requests", label: "Verification Requests" },
    { path: "/admin/courses", label: "Posting Courses" },
  ]

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-900">
        <div className="w-16 h-16 border-4 border-t-indigo-500 border-r-transparent border-b-indigo-500 border-l-transparent rounded-full animate-spin"></div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white relative flex">
      {/* Admin Side Navigation (desktop) */}
      <aside className="hidden md:flex flex-col fixed left-0 top-0 h-full w-64 bg-gray-900 border-r border-gray-800 p-5">
        {/* Logo */}
        <div className="mb-8 flex items-center gap-2">
          <h2 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-blue-400 to-teal-400">
            AstroLearn
          </h2>
          <span className="bg-indigo-600 text-white px-2 py-1 rounded-md text-xs">Admin</span>
        </div>

        {/* Navigation Links */}
        <nav className="flex-1">
          <ul className="space-y-2">
            {navItems.map((item) => {
              const isActive = pathname === item.path || pathname.startsWith(`${item.path}/`);
              
              return (
                <li key={item.path}>
                  <Link
                    href={item.path}
                    className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                      isActive ? "bg-indigo-900/50 text-indigo-400" : "text-gray-400 hover:bg-gray-800 hover:text-white"
                    }`}
                  >
                    <span>{item.label}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        {/* Logout Button */}
        <div className="mt-auto pt-5 border-t border-gray-800">
          <Link 
            href="/"
            className="flex items-center gap-3 px-4 py-3 w-full text-left rounded-lg text-gray-400 hover:bg-gray-800 hover:text-white transition-colors"
          >
            <span>Logout</span>
          </Link>
        </div>
      </aside>

      {/* Mobile Navigation */}
      <div className="md:hidden fixed bottom-0 inset-x-0 bg-gray-800 border-t border-gray-700 z-10">
        <div className="flex justify-between px-4">
          {navItems.map((item) => {
            const isActive = pathname === item.path || pathname.startsWith(`${item.path}/`);
            
            return (
              <Link
                key={item.path}
                href={item.path}
                className={`flex flex-col items-center p-2 ${
                  isActive ? "text-indigo-400" : "text-gray-400 hover:text-white"
                }`}
              >
                <span className="text-xs mt-1">{item.label}</span>
              </Link>
            );
          })}
          <Link
            href="/"
            className="flex flex-col items-center p-2 text-gray-400 hover:text-white"
          >
            <span className="text-xs mt-1">Logout</span>
          </Link>
        </div>
      </div>
      
      {/* Mobile header */}
      <div className="md:hidden fixed top-0 inset-x-0 bg-gray-900 border-b border-gray-800 z-10 h-16 flex items-center px-4">
        <div className="flex items-center gap-2">
          <h2 className="text-xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-blue-400 to-teal-400">
            AstroLearn
          </h2>
          <span className="bg-indigo-600 text-white px-2 py-1 rounded-md text-xs">Admin</span>
        </div>
      </div>

      {/* Main Content */}
      <main className="md:ml-64 flex-1 px-4 py-8 mt-16 md:mt-0">
        <div className="container mx-auto max-w-6xl">
          {children}
        </div>
      </main>

      {/* Blooming Stars Animation */}
      <BloomingStars />
    </div>
  )
}
