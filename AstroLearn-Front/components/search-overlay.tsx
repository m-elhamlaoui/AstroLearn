"use client"

import { useState, useEffect, useRef } from "react"
import { X, Search } from "lucide-react"
import { Input } from "@/components/ui/input"
import Link from "next/link"
import { motion, AnimatePresence } from "framer-motion"

interface Article {
  id: number
  title: string
  votes: number
  tags: string[]
}

interface SearchOverlayProps {
  onClose: () => void
  articles: Article[]
}

export function SearchOverlay({ onClose, articles }: SearchOverlayProps) {
  const [searchQuery, setSearchQuery] = useState("")
  const [searchResults, setSearchResults] = useState<Article[]>([])
  const [suggestions, setSuggestions] = useState<string[]>([
    "Black holes",
    "Mars colonization",
    "James Webb telescope",
    "SpaceX",
    "Exoplanets",
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
    // In production, this would call the backend API
    const results = articles.filter(
      (article) =>
        article.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        article.tags.some((tag) => tag.toLowerCase().includes(searchQuery.toLowerCase())),
    )

    setSearchResults(results)

    /* 
      In production, we would call the backend:
      
      const searchArticles = async () => {
        try {
          const response = await axios.get(`http://your-spring-boot-api/api/articles/search?q=${searchQuery}`);
          setSearchResults(response.data);
        } catch (error) {
          console.error('Error searching articles:', error);
          setSearchResults([]);
        }
      };
      
      searchArticles();
    */
  }, [searchQuery, articles])

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

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-20">
      {/* Backdrop with blur effect */}
      <div className="absolute inset-0 bg-black/80 backdrop-blur-sm" onClick={onClose}></div>

      {/* Search container */}
      <div className="relative z-10 w-full max-w-2xl px-4">
        {/* Search input */}
        <div className="bg-gray-800 rounded-lg shadow-xl overflow-hidden">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <Input
              ref={inputRef}
              type="text"
              placeholder="Search articles..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-12 pr-12 py-6 bg-transparent border-none text-white text-lg focus-visible:ring-0 focus-visible:ring-offset-0"
            />
            <button
              onClick={onClose}
              className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-white transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Search results or suggestions */}
          <div className="max-h-[70vh] overflow-y-auto p-4">
            {searchQuery === "" ? (
              // Show suggestions when search is empty
              <div>
                <h3 className="text-sm font-medium text-gray-400 mb-3">Suggested searches</h3>
                <div className="flex flex-wrap gap-2">
                  {suggestions.map((suggestion) => (
                    <button
                      key={suggestion}
                      onClick={() => setSearchQuery(suggestion)}
                      className="px-3 py-1 bg-gray-700 hover:bg-gray-600 rounded-full text-sm text-gray-300 transition-colors"
                    >
                      {suggestion}
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              // Show search results
              <AnimatePresence>
                <div>
                  <h3 className="text-sm font-medium text-gray-400 mb-3">{searchResults.length} results found</h3>
                  <ul className="space-y-2">
                    {searchResults.map((result, index) => (
                      <motion.li
                        key={result.id}
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.2, delay: index * 0.05 }}
                      >
                        <Link
                          href={`/articles/${result.id}`}
                          onClick={onClose}
                          className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-700 transition-colors"
                        >
                          <span className="text-white">{result.title}</span>
                          <span
                            className={`text-sm ${
                              result.votes > 0 ? "text-green-500" : result.votes < 0 ? "text-red-500" : "text-gray-400"
                            }`}
                          >
                            {result.votes}
                          </span>
                        </Link>
                      </motion.li>
                    ))}
                  </ul>
                </div>
              </AnimatePresence>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
