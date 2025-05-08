"use client"

import { useState } from "react"
import { ArticleCard } from "@/components/article-card"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { ArticleSearchBar } from "@/components/article-search-bar"
import { Search, PlusCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import Link from "next/link"
import { BloomingStars } from "@/components/blooming-stars"

// Sample data - would be fetched from backend in production
const sampleArticles = [
  {
    id: 1,
    title: "The Future of Mars Colonization",
    summary: "Exploring the challenges and possibilities of establishing human settlements on the Red Planet.",
    image: "/placeholder.svg?height=300&width=500",
    author: {
      id: 101,
      name: "Elena Rodriguez",
      profileImage: "/placeholder.svg?height=50&width=50",
    },
    publishDate: "2023-11-15T14:30:00Z",
    votes: 128,
    tags: ["Mars", "Colonization", "Space Travel"],
  },
  {
    id: 2,
    title: "James Webb's Latest Discoveries",
    summary: "A deep dive into the groundbreaking observations from NASA's most powerful space telescope.",
    image: "/placeholder.svg?height=300&width=500",
    author: {
      id: 102,
      name: "Marcus Chen",
      profileImage: "/placeholder.svg?height=50&width=50",
    },
    publishDate: "2023-11-10T09:15:00Z",
    votes: 245,
    tags: ["James Webb", "Telescope", "Astronomy"],
  },
  {
    id: 3,
    title: "Understanding Black Holes",
    summary: "A comprehensive guide to one of the universe's most mysterious phenomena.",
    image: "/placeholder.svg?height=300&width=500",
    author: {
      id: 103,
      name: "Sophia Williams",
      profileImage: "/placeholder.svg?height=50&width=50",
    },
    publishDate: "2023-11-05T16:45:00Z",
    votes: 189,
    tags: ["Black Holes", "Astrophysics", "Space"],
  },
  {
    id: 4,
    title: "The Search for Exoplanets",
    summary: "How astronomers are discovering and analyzing planets outside our solar system.",
    image: "/placeholder.svg?height=300&width=500",
    author: {
      id: 104,
      name: "David Kim",
      profileImage: "/placeholder.svg?height=50&width=50",
    },
    publishDate: "2023-10-28T11:20:00Z",
    votes: -12,
    tags: ["Exoplanets", "Astronomy", "Space Exploration"],
  },
  {
    id: 5,
    title: "SpaceX Starship Development",
    summary: "The latest updates on SpaceX's revolutionary spacecraft designed for Mars missions.",
    image: "/placeholder.svg?height=300&width=500",
    author: {
      id: 105,
      name: "Alex Johnson",
      profileImage: "/placeholder.svg?height=50&width=50",
    },
    publishDate: "2023-10-22T13:10:00Z",
    votes: 302,
    tags: ["SpaceX", "Starship", "Mars"],
  },
  {
    id: 6,
    title: "The Artemis Program: Return to the Moon",
    summary: "NASA's plan to land the first woman and next man on the lunar surface by 2025.",
    image: "/placeholder.svg?height=300&width=500",
    author: {
      id: 106,
      name: "Olivia Martinez",
      profileImage: "/placeholder.svg?height=50&width=50",
    },
    publishDate: "2023-10-15T10:05:00Z",
    votes: 178,
    tags: ["Artemis", "Moon", "NASA"],
  },
]

export default function ArticlesPage() {
  const [isSearchOpen, setIsSearchOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState("")
  const [filteredArticles, setFilteredArticles] = useState(sampleArticles)

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query)

    if (!query.trim()) {
      setFilteredArticles(sampleArticles)
      return
    }

    const filtered = sampleArticles.filter(
      (article) =>
        article.title.toLowerCase().includes(query.toLowerCase()) ||
        article.summary.toLowerCase().includes(query.toLowerCase()) ||
        article.author.name.toLowerCase().includes(query.toLowerCase()) ||
        article.tags.some((tag) => tag.toLowerCase().includes(query.toLowerCase())),
    )

    setFilteredArticles(filtered)
  }

  return (
    <div className="flex min-h-screen bg-black text-white relative">
      {/* Blooming Stars Animation */}
      <BloomingStars />
      
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300 relative z-10">
        <div className="container mx-auto">
          {/* Header with Icon Buttons */}
          <div className="flex justify-between items-center mb-8">
            <h1 className="text-3xl font-bold">Explore Articles</h1>
            <div className="flex space-x-3">
              <Link href="/article-edit">
                <Button
                  variant="outline"
                  size="icon"
                  className="h-10 w-10 rounded-full bg-gray-800/50 border-gray-700 hover:bg-indigo-900/50 hover:border-indigo-600 text-gray-300 hover:text-indigo-400 backdrop-blur-sm"
                >
                  <PlusCircle className="h-5 w-5" />
                  <span className="sr-only">Create Article</span>
                </Button>
              </Link>
              <Button
                onClick={() => setIsSearchOpen(true)}
                variant="outline"
                size="icon"
                className="h-10 w-10 rounded-full bg-gray-800/50 border-gray-700 hover:bg-indigo-900/50 hover:border-indigo-600 text-gray-300 hover:text-indigo-400 backdrop-blur-sm"
              >
                <Search className="h-5 w-5" />
                <span className="sr-only">Search Articles</span>
              </Button>
            </div>
          </div>

          {/* Articles Grid */}
          {filteredArticles.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredArticles.map((article) => (
                <ArticleCard key={article.id} article={article} />
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <p className="text-gray-400 text-lg">No articles found matching your search criteria.</p>
              <Button
                variant="outline"
                onClick={() => handleSearch("")}
                className="mt-4 border-gray-700 text-gray-300 hover:bg-gray-800"
              >
                Clear Search
              </Button>
            </div>
          )}
        </div>
      </main>

      {/* Search Overlay */}
      {isSearchOpen && (
        <ArticleSearchBar onClose={() => setIsSearchOpen(false)} onSearch={handleSearch} articles={sampleArticles} />
      )}
    </div>
  )
}
