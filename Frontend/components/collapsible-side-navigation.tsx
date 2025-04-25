"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { FileText, Calendar, MessageSquare, BookOpen, User, Settings, LogOut, Menu } from "lucide-react"
import { motion, AnimatePresence } from "framer-motion"

export function CollapsibleSideNavigation() {
  const pathname = usePathname()
  const [isExpanded, setIsExpanded] = useState(false)
  const [isHovered, setIsHovered] = useState(false)

  const navItems = [
    { name: "Articles", href: "/articles", icon: FileText },
    { name: "Missions", href: "/missions", icon: Calendar },
    { name: "Chatbot", href: "/chatbot", icon: MessageSquare },
    { name: "Courses", href: "/courses", icon: BookOpen },
    { name: "Profile", href: "/profile", icon: User },
    { name: "Settings", href: "/settings", icon: Settings },
  ]

  // Reset expanded state when navigating
  useEffect(() => {
    setIsExpanded(false)
  }, [pathname])

  return (
    <motion.aside
      className="fixed left-0 top-0 h-full bg-black border-r border-gray-800 z-40"
      initial={{ width: 64 }}
      animate={{ width: isExpanded || isHovered ? 256 : 64 }}
      transition={{ duration: 0.3, ease: "easeInOut" }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="h-full flex flex-col p-4">
        {/* Header with Logo and Toggle */}
        <div className="flex items-center justify-between mb-8">
          <AnimatePresence>
            {(isExpanded || isHovered) && (
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.2 }}
                className="flex-1"
              >
                <Link href="/" className="flex items-center">
                  <h2 className="text-xl font-bold text-white">AstroLearn</h2>
                </Link>
              </motion.div>
            )}
          </AnimatePresence>

          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="p-2 rounded-lg text-gray-400 hover:text-white hover:bg-gray-800 transition-colors"
          >
            <Menu className="h-5 w-5" />
          </button>
        </div>

        {/* Navigation Links */}
        <nav className="flex-1">
          <ul className="space-y-2">
            {navItems.map((item) => {
              const isActive = pathname.startsWith(item.href)

              return (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    className={`flex items-center gap-3 px-3 py-3 rounded-lg transition-colors ${
                      isActive ? "bg-gray-800 text-white" : "text-gray-400 hover:bg-gray-800 hover:text-white"
                    }`}
                  >
                    <item.icon className="h-5 w-5 flex-shrink-0" />
                    <AnimatePresence>
                      {(isExpanded || isHovered) && (
                        <motion.span
                          initial={{ opacity: 0, width: 0 }}
                          animate={{ opacity: 1, width: "auto" }}
                          exit={{ opacity: 0, width: 0 }}
                          transition={{ duration: 0.2 }}
                          className="whitespace-nowrap"
                        >
                          {item.name}
                        </motion.span>
                      )}
                    </AnimatePresence>
                  </Link>
                </li>
              )
            })}
          </ul>
        </nav>

        {/* Logout Button */}
        <div className="mt-auto pt-5 border-t border-gray-800">
          <button className="flex items-center gap-3 px-3 py-3 w-full text-left rounded-lg text-gray-400 hover:bg-gray-800 hover:text-white transition-colors">
            <LogOut className="h-5 w-5 flex-shrink-0" />
            <AnimatePresence>
              {(isExpanded || isHovered) && (
                <motion.span
                  initial={{ opacity: 0, width: 0 }}
                  animate={{ opacity: 1, width: "auto" }}
                  exit={{ opacity: 0, width: 0 }}
                  transition={{ duration: 0.2 }}
                  className="whitespace-nowrap"
                >
                  Logout
                </motion.span>
              )}
            </AnimatePresence>
          </button>
        </div>
      </div>
    </motion.aside>
  )
}
