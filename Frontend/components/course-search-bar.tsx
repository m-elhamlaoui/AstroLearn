"use client"

import { useState, useEffect, useRef } from "react"
import { X, Search } from "lucide-react"
import { Input } from "@/components/ui/input"
import Link from "next/link"
import { motion, AnimatePresence } from "framer-motion"

interface Course {
  id: number
  title: string
  instructor: string
  level: string
  category: string
}

interface CourseSearchBarProps {
  onClose: () => void
  onSearch: (query: string) => void
  courses: Course[]
}

export function CourseSearchBar({ onClose, onSearch, courses }: CourseSearchBarProps) {
  const [searchQuery, setSearchQuery] = useState("")
  const [searchResults, setSearchResults] = useState<Course[]>([])
  const [suggestions] = useState<string[]>([
    "Astronomy basics",
    "Rocket science",
    "Planetary exploration",
    "Astrophysics",
    "Space engineering",
  ])
  const inputRef = useRef<HTMLInputElement>(null)

  // Focus the input when the overlay opens
  useEffect(() => {
    if (inputRef.current) {
      inputRef.current.focus()
    }

    // Prevent scrolling of the body when overlay is open
    document.body.style.overflow = "hidden"

    return () => {
      document.body.style.overflow = "auto"
    }
  }, [])

  // Handle search
  useEffect(() => {
    if (searchQuery.trim() === "") {
      setSearchResults([])
      return
    }

    // Simple client-side search for demo purposes
    const results = courses.filter(
      (course) =>
        course.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        course.instructor.toLowerCase().includes(searchQuery.toLowerCase()) ||
        course.category.toLowerCase().includes(searchQuery.toLowerCase()) ||
        course.level.toLowerCase().includes(searchQuery.toLowerCase()),
    )

    setSearchResults(results)
  }, [searchQuery, courses])

  // Handle escape key to close overlay
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose()
      }
    }

    window.addEventListener("keydown", handleEscape)

    return () => {
      window.removeEventListener("keydown", handleEscape)
    }
  }, [onClose])

  // Handle search submission
  const handleSubmit = () => {
    if (searchQuery.trim()) {
      onSearch(searchQuery)
      onClose()
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-20">
      {/* Backdrop with blur effect */}
      <div className="absolute inset-0 bg-black/80 backdrop-blur-sm" onClick={onClose}></div>

      {/* Search container */}
      <div className="relative z-10 w-full max-w-2xl px-4">
        {/* Search input */}
        <div className="bg-black border border-white/20 rounded-lg shadow-xl overflow-hidden">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-white/70" />
            <Input
              ref={inputRef}
              type="text"
              placeholder="Search courses..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  handleSubmit()
                }
              }}
              className="pl-12 pr-12 py-6 bg-transparent border-none text-white text-lg focus-visible:ring-0 focus-visible:ring-offset-0"
            />
            <button
              onClick={onClose}
              className="absolute right-4 top-1/2 transform -translate-y-1/2 text-white/70 hover:text-white transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Search results or suggestions */}
          <div className="max-h-[70vh] overflow-y-auto">
            {searchQuery === "" ? (
              // Show suggestions when search is empty
              <div className="px-4 pb-4">
                <h3 className="text-sm font-medium text-white/70 mb-3">Suggested searches</h3>
                <div className="space-y-3">
                  {suggestions.map((suggestion, index) => (
                    <div
                      key={suggestion}
                      className={index < suggestions.length - 1 ? "pb-3 border-b border-white/10" : ""}
                    >
                      <button
                        onClick={() => {
                          setSearchQuery(suggestion)
                          onSearch(suggestion)
                          onClose()
                        }}
                        className="w-full text-left text-white hover:text-gray-300 transition-colors"
                      >
                        {suggestion}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              // Show search results
              <AnimatePresence>
                <div className="px-4 pb-4">
                  <h3 className="text-sm font-medium text-white/70 mb-3">{searchResults.length} results found</h3>
                  <div className="space-y-0">
                    {searchResults.map((result, index) => (
                      <motion.div
                        key={result.id}
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.2, delay: index * 0.05 }}
                        className={`py-3 ${index < searchResults.length - 1 ? "border-b border-white/10" : ""}`}
                      >
                        <Link
                          href={`/courses/${result.id}`}
                          onClick={onClose}
                          className="flex items-center justify-between hover:text-gray-300 transition-colors"
                        >
                          <div>
                            <span className="text-white block">{result.title}</span>
                            <span className="text-white/60 text-sm">
                              {result.instructor} • {result.level} • {result.category}
                            </span>
                          </div>
                        </Link>
                      </motion.div>
                    ))}
                  </div>
                </div>
              </AnimatePresence>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
