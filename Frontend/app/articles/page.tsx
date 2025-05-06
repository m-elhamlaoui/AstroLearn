"use client"

import { useEffect, useState } from "react"
import axios from "axios"
import { ArticleCard } from "@/components/article-card"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { ArticleSearchBar } from "@/components/article-search-bar"
import { Search } from "lucide-react"
import { Button } from "@/components/ui/button"

const API_URL = "http://localhost:8088"

export const fetchArticles = async () => {
  const response = await axios.get(`${API_URL}/articles`)
  return response.data.content // because it's a Spring Page
}

export const searchArticles = async (query: string) => {
  const response = await axios.get(`${API_URL}/articles/search`, {
    params: { query }
  })
  return response.data
}

export default function ArticlesPage() {
  const [isSearchOpen, setIsSearchOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState("")
  const [filteredArticles, setFilteredArticles] = useState([])

  useEffect(() => {
    fetchArticles().then(setFilteredArticles)
  }, [])

  const handleSearch = async (query: string) => {
    setSearchQuery(query)
    if (!query.trim()) {
      const data = await fetchArticles()
      setFilteredArticles(data)
    } else {
      const data = await searchArticles(query)
      setFilteredArticles(data)
    }
  }

  return (
      <div className="flex min-h-screen bg-black text-white">
        <MinimalNavigation />
        <main className="flex-1 p-6 ml-12 transition-all duration-300">
          <div className="container mx-auto">
            <div className="flex justify-between items-center mb-8">
              <h1 className="text-3xl font-bold">Explore Articles</h1>
              <Button
                  onClick={() => setIsSearchOpen(true)}
                  variant="outline"
                  className="border-gray-700 text-gray-300 hover:bg-gray-800"
              >
                <Search className="h-5 w-5 mr-2" />
                Search Articles
              </Button>
            </div>

            {filteredArticles.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filteredArticles.map((article: any) => (
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

        {isSearchOpen && (
            <ArticleSearchBar
                onClose={() => setIsSearchOpen(false)}
                onSearch={handleSearch}
                articles={filteredArticles}
            />
        )}
      </div>
  )
}