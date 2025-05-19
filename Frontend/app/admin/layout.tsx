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
    <div className="min-h-screen bg-gray-900 text-white relative">
      {/* Admin Navigation Bar */}
      <header className="bg-gray-800 border-b border-gray-700">
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center space-x-2">
              <span className="text-xl font-bold text-indigo-400">AstroLearn</span>
              <span className="bg-indigo-600 text-white px-2 py-1 rounded-md text-xs">Admin</span>
            </div>
            <nav className="hidden md:flex space-x-6">
              {navItems.map((item) => (
                <Link 
                  key={item.path} 
                  href={item.path}
                  className={`px-3 py-2 rounded-md text-sm font-medium ${
                    pathname === item.path
                      ? "bg-indigo-700 text-white"
                      : "text-gray-300 hover:bg-gray-700 hover:text-white"
                  }`}
                >
                  {item.label}
                </Link>
              ))}
              <Link
                href="/"
                className="px-3 py-2 rounded-md text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
              >
                Logout
              </Link>
            </nav>
          </div>
        </div>
      </header>

      {/* Mobile Navigation */}
      <div className="md:hidden fixed bottom-0 inset-x-0 bg-gray-800 border-t border-gray-700 z-10">
        <div className="flex justify-between px-4">
          {navItems.map((item) => (
            <Link
              key={item.path}
              href={item.path}
              className={`flex flex-col items-center p-2 ${
                pathname === item.path
                  ? "text-indigo-400"
                  : "text-gray-400 hover:text-white"
              }`}
            >
              <span className="text-xs mt-1">{item.label}</span>
            </Link>
          ))}
          <Link
            href="/"
            className="flex flex-col items-center p-2 text-gray-400 hover:text-white"
          >
            <span className="text-xs mt-1">Logout</span>
          </Link>
        </div>
      </div>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        {children}
      </main>

      {/* Blooming Stars Animation */}
      <BloomingStars />
    </div>
  )
}
