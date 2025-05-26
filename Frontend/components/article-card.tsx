"use client"

import { useState } from "react"
import axiosInstance from "@/lib/axiosInstance" // Re-add axiosInstance import
import { Card } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { ArrowUp, ArrowDown } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import { formatDistanceToNow } from "date-fns"

interface Author {
  id: number
  name: string
  profileImage: string
}

interface Article {
  id: number
  title: string
  summary: string
  image: string
  author: Author
  publishDate: string
  votes: number
  tags: string[]
  currentUserVote?: number | null; // Add the new field here too
}

interface ArticleCardProps {
  article: Article
}

// Remove duplicate interface definition below if present
// interface ArticleCardProps {
// } // Removed stray 'article: Article' line

export function ArticleCard({ article }: ArticleCardProps) {
  const [votes, setVotes] = useState(article.votes)
  // Initialize userVote based on currentUserVote from props
  const [userVote, setUserVote] = useState<"up" | "down" | null>(
    article.currentUserVote === 1 ? "up" : article.currentUserVote === -1 ? "down" : null
  );
  const [isVoting, setIsVoting] = useState(false); // Add isVoting state

  // Format the date to "X days/hours/minutes ago"
  const formattedDate = formatDistanceToNow(new Date(article.publishDate), { addSuffix: true })

  // Handle voting with backend integration and optimistic updates
  const handleVote = async (newVoteType: "up" | "down") => {
    if (isVoting) return;
    setIsVoting(true)

    const previousVotes = votes // Use existing 'votes' state
    const previousUserVoteStatus = userVote // Use existing 'userVote' state
    let optimisticApiVoteType = newVoteType.toUpperCase() // "UP" or "DOWN"

    // Optimistic UI Update
    if (userVote === newVoteType) { // Clicking the same button (attempt to unvote)
      setVotes(previousVotes - (newVoteType === "up" ? 1 : -1)) // Update 'votes' state
      setUserVote(null) // Update 'userVote' state
    } else if (userVote !== null) { // Changing vote (e.g., from up to down)
      setVotes(previousVotes + (newVoteType === "up" ? 2 : -2)) // Update 'votes' state
      setUserVote(newVoteType) // Update 'userVote' state
    } else { // New vote
      setVotes(previousVotes + (newVoteType === "up" ? 1 : -1)) // Update 'votes' state
      setUserVote(newVoteType) // Update 'userVote' state
    }

    // Placeholder userId, replace with actual authenticated user ID
    const userId = 1 

     try {
       console.log(`[ArticleCard Vote] Attempting POST to /articles/${article.id}/vote/user/${userId} with type: ${optimisticApiVoteType}`); // Log before API call
       // Backend endpoint: POST /articles/{id}/vote/user/{userId}
       // Assumes backend handles toggling/unvoting correctly if the same voteType is sent again
       const response = await axiosInstance.post(`/articles/${article.id}/vote/user/${userId}`, { 
         voteType: optimisticApiVoteType 
       })
      
      // Use the score and vote status returned from the backend for consistency
      // Assumes response.data is the updated ArticleDTO
      if (response.data && typeof response.data.score === 'number') {
        setVotes(response.data.score) // Correct with backend score
        // Update userVote based on response.data.currentUserVote
        setUserVote(response.data.currentUserVote === 1 ? "up" : response.data.currentUserVote === -1 ? "down" : null);
      } else {
        console.warn("Vote API response did not contain expected score/vote format.", response.data)
        // Revert optimistic update if data is not as expected
        setVotes(previousVotes)
         setUserVote(previousUserVoteStatus)
       }
     } catch (error) {
       console.error(`[ArticleCard Vote] Error voting on article ${article.id}:`, error); // Enhanced error log
       // Revert optimistic UI updates on error
       setVotes(previousVotes)
      setUserVote(previousUserVoteStatus)
      // Optionally, show an error message to the user
    } finally {
      setIsVoting(false)
    }
  }


  return (
    <Card className="bg-gray-800 border-gray-700 overflow-hidden rounded-xl hover:shadow-lg hover:shadow-indigo-900/20 transition-all duration-300 group">
      {/* Article Image */}
      <div className="relative h-48 overflow-hidden">
        <Link href={`/articles/${article.id}`} className="block h-full">
          <div className="absolute inset-0 bg-gray-900 group-hover:opacity-80 transition-opacity duration-300"></div>
          <Image
            src={article.image || "/placeholder.svg"}
            alt={article.title}
            fill
            className="object-cover group-hover:scale-105 transition-transform duration-500"
          />
        </Link>
      </div>

      {/* Article Content */}
      <div className="p-5">
        {/* Author Info and Date */}
        <div className="flex justify-between items-center mb-3">
          <Link
            href={`/profile/${article.author.id}`}
            className="flex items-center gap-2 hover:opacity-80 transition-opacity"
          >
            <Avatar className="h-8 w-8 border-2 border-indigo-500">
              <AvatarImage 
                src={article.author.profileImage} 
                alt={article.author.name} 
                onError={(e) => {
                  e.currentTarget.onerror = null; // Prevent infinite loop
                  e.currentTarget.src = "/placeholder.svg?height=50&width=50";
                }}
              />
              <AvatarFallback>{article.author.name.charAt(0)}</AvatarFallback>
            </Avatar>
            <span className="text-sm text-gray-300">{article.author.name}</span>
          </Link>
          <span className="text-xs text-gray-400">{formattedDate}</span>
        </div>

        {/* Article Title and Summary */}
        <Link href={`/articles/${article.id}`} className="block group-hover:text-indigo-400 transition-colors">
          <h3 className="text-xl font-bold mb-2">{article.title}</h3>
          <p className="text-gray-400 text-sm mb-4 line-clamp-3">{article.summary}</p>
        </Link>

        {/* Tags */}
        <div className="flex flex-wrap gap-2 mb-4">
          {article.tags.map((tag) => (
            <span key={tag} className="text-xs px-2 py-1 bg-gray-700 text-gray-300 rounded-full">
              {tag}
            </span>
          ))}
        </div>

        {/* Voting */}
        <div className="flex items-center gap-1">
          <button
            onClick={() => handleVote("up")}
            className={`p-1 rounded hover:bg-gray-700 transition-colors ${userVote === "up" ? "text-green-500" : "text-gray-400 hover:text-green-400"}`}
            aria-label="Upvote"
            disabled={isVoting} // Disable button while voting
          >
            <ArrowUp className="h-5 w-5" />
          </button>

          <span
            className={`text-sm font-medium ${
              votes > 0 ? "text-green-500" : votes < 0 ? "text-red-500" : "text-gray-400"
            }`}
          >
            {votes} 
          </span>

          <button
            onClick={() => handleVote("down")}
            className={`p-1 rounded hover:bg-gray-700 transition-colors ${userVote === "down" ? "text-red-500" : "text-gray-400 hover:text-red-400"}`}
            aria-label="Downvote"
            disabled={isVoting} // Disable button while voting
          >
            <ArrowDown className="h-5 w-5" />
          </button>
        </div>
      </div>
    </Card>
  )
}
