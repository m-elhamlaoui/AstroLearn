"use client"

import { useState, useEffect } from "react"
import axiosInstance from "../../lib/axiosInstance" // Assuming axiosInstance is in lib
import { ArticleCard } from "@/components/article-card"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { ArticleSearchBar } from "@/components/article-search-bar"
import { Search, PlusCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import Link from "next/link"
import { BloomingStars } from "@/components/blooming-stars"

// Define interfaces for Article and Author based on DTO and component needs
interface Author {
  id: number; 
  name: string;
  profileImage: string;
}

interface Article {
  id: number; 
  title: string;
  summary: string;
  image: string; // From imageUrls[0]
  author: Author;
  publishDate: string; // From createdAt
  votes: number; // From score
  tags: string[];
}

// Backend DTO structure (for reference during transformation)
interface ArticleDTO {
  id: number;
  title: string;
  summary: string;
  content: string; // Not directly used in ArticleCard but part of DTO
  imageUrls: string[];
  createdAt: string; // Assuming ISO string from backend
  authorId: number;
  authorUsername: string;
  score: number;
  commentCount: number; // Not directly used in ArticleCard
  tags: string[];
  currentUserVote?: number | null; // Add the new field (optional for safety)
}

// Update frontend Article interface
interface Article {
  id: number; 
  title: string;
  summary: string;
  image: string; // From imageUrls[0]
  author: Author;
  publishDate: string; // From createdAt
  votes: number; // From score
  tags: string[];
  currentUserVote?: number | null; // Add the new field
}


export default function ArticlesPage() {
  const [isSearchOpen, setIsSearchOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState("")
  const [allArticles, setAllArticles] = useState<Article[]>([])
  const [filteredArticles, setFilteredArticles] = useState<Article[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchArticles = async () => {
      try {
        setLoading(true)
        setError(null)
        // Fetch articles from the backend
        // The backend returns Page<ArticleDTO>, so response.data might be { content: ArticleDTO[], ... }
        const response = await axiosInstance.get<{ content: ArticleDTO[] }>("/articles")
        
        const fetchedArticlesDTO = response.data.content || [] // Ensure it's an array

        // Transform DTOs to the Article interface expected by the frontend
        const transformedArticles: Article[] = fetchedArticlesDTO.map((dto: ArticleDTO) => ({
          id: dto.id,
          title: dto.title,
          summary: dto.summary,
          image: dto.imageUrls && dto.imageUrls.length > 0 ? dto.imageUrls[0] : "/placeholder.svg?height=300&width=500", // Use first image or placeholder
          author: {
            id: dto.authorId,
            name: dto.authorUsername,
            profileImage: "/placeholder.svg?height=50&width=50", // Placeholder for profile image
          },
          publishDate: dto.createdAt, // Assuming createdAt is a string like "2023-11-15T14:30:00Z"
          votes: dto.score,
          tags: dto.tags || [],
          currentUserVote: dto.currentUserVote, // Map the new field
        }))

        setAllArticles(transformedArticles)
        setFilteredArticles(transformedArticles)
      } catch (err: any) {
        console.error("Failed to fetch articles:", err)
        setError(err.message || "Failed to load articles. Please try again later.")
      } finally {
        setLoading(false)
      }
    }

    fetchArticles()
  }, [])

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query)

    if (!query.trim()) {
      setFilteredArticles(allArticles) // Reset to all fetched articles
      return
    }

    const filtered = allArticles.filter(
      (article) =>
        article.title.toLowerCase().includes(query.toLowerCase()) ||
        article.summary.toLowerCase().includes(query.toLowerCase()) ||
        article.author.name.toLowerCase().includes(query.toLowerCase()) ||
        (article.tags && article.tags.some((tag) => tag.toLowerCase().includes(query.toLowerCase()))),
    )

    setFilteredArticles(filtered)
  }

  if (loading) {
    return (
      <div className="flex min-h-screen bg-black text-white items-center justify-center">
        <MinimalNavigation />
        <p className="text-xl">Loading articles...</p>
        {/* Optionally, add a spinner component here */}
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-screen bg-black text-white items-center justify-center p-6">
        <MinimalNavigation />
        <div className="text-center">
          <p className="text-xl text-red-500">Error: {error}</p>
          <Button
            variant="outline"
            onClick={() => window.location.reload()} // Simple reload, or could re-trigger fetchArticles
            className="mt-4 border-gray-700 text-gray-300 hover:bg-gray-800"
          >
            Try Again
          </Button>
        </div>
      </div>
    )
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
              <p className="text-gray-400 text-lg">
                {searchQuery ? "No articles found matching your search criteria." : "No articles available at the moment."}
              </p>
              {searchQuery && (
                 <Button
                    variant="outline"
                    onClick={() => handleSearch("")} // Clear search
                    className="mt-4 border-gray-700 text-gray-300 hover:bg-gray-800"
                  >
                  Clear Search
                </Button>
              )}
            </div>
          )}
        </div>
      </main>

      {/* Search Overlay */}
      {isSearchOpen && (
        <ArticleSearchBar onClose={() => setIsSearchOpen(false)} onSearch={handleSearch} articles={allArticles} />
      )}
    </div>
  )
}
