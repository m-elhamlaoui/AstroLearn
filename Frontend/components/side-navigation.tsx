"use client"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { FileText, Calendar, MessageSquare, BookOpen, User, Settings, LogOut } from "lucide-react"

export function SideNavigation() {
  const pathname = usePathname()

  const navItems = [
    { name: "Articles", href: "/articles", icon: FileText },
    { name: "Missions", href: "/missions", icon: Calendar },
    { name: "Chatbot", href: "/chatbot", icon: MessageSquare },
    { name: "Courses", href: "/courses", icon: BookOpen },
    { name: "Profile", href: "/profile", icon: User },
    { name: "Settings", href: "/settings", icon: Settings },
  ]

  return (
    <aside className="fixed left-0 top-0 h-full w-64 bg-gray-900 border-r border-gray-800 p-5 flex flex-col">
      {/* Logo */}
      <div className="mb-8">
        <Link href="/" className="flex items-center">
          <h2 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-blue-400 to-teal-400">
            AstroLearn
          </h2>
        </Link>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1">
        <ul className="space-y-2">
          {navItems.map((item) => {
            const isActive = pathname === item.href

            return (
              <li key={item.name}>
                <Link
                  href={item.href}
                  className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                    isActive ? "bg-indigo-900/50 text-indigo-400" : "text-gray-400 hover:bg-gray-800 hover:text-white"
                  }`}
                >
                  <item.icon className="h-5 w-5" />
                  <span>{item.name}</span>
                </Link>
              </li>
            )
          })}
        </ul>
      </nav>

      {/* Logout Button */}
      <div className="mt-auto pt-5 border-t border-gray-800">
        <button className="flex items-center gap-3 px-4 py-3 w-full text-left rounded-lg text-gray-400 hover:bg-gray-800 hover:text-white transition-colors">
          <LogOut className="h-5 w-5" />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  )
}
