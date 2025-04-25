"use client"

import { useState } from "react"
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
}

interface ArticleCardProps {
  article: Article
}

export function ArticleCard({ article }: ArticleCardProps) {
  const [votes, setVotes] = useState(article.votes)
  const [userVote, setUserVote] = useState<"up" | "down" | null>(null)

  // Format the date to "X days/hours/minutes ago"
  const formattedDate = formatDistanceToNow(new Date(article.publishDate), { addSuffix: true })

  // Handle voting
  const handleVote = (voteType: "up" | "down") => {
    // In production, this would call the backend API
    // For now, we'll just update the local state

    if (userVote === voteType) {
      // User is removing their vote
      setVotes(voteType === "up" ? votes - 1 : votes + 1)
      setUserVote(null)
    } else if (userVote === null) {
      // User is adding a new vote
      setVotes(voteType === "up" ? votes + 1 : votes - 1)
      setUserVote(voteType)
    } else {
      // User is changing their vote (e.g., from up to down)
      setVotes(voteType === "up" ? votes + 2 : votes - 2)
      setUserVote(voteType)
    }

    /* 
      In production, we would call the backend:
      
      const voteOnArticle = async () => {
        try {
          const response = await axios.post(`http://your-spring-boot-api/api/articles/${article.id}/vote`, {
            voteType: voteType
          });
          
          if (response.data.success) {
            setVotes(response.data.newVoteCount);
            setUserVote(voteType);
          }
        } catch (error) {
          console.error('Error voting on article:', error);
        }
      };
      
      voteOnArticle();
    */
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
              <AvatarImage src={article.author.profileImage || "/placeholder.svg"} alt={article.author.name} />
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
            className={`p-1 rounded hover:bg-gray-700 transition-colors ${userVote === "up" ? "text-green-500" : "text-gray-400"}`}
            aria-label="Upvote"
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
            className={`p-1 rounded hover:bg-gray-700 transition-colors ${userVote === "down" ? "text-red-500" : "text-gray-400"}`}
            aria-label="Downvote"
          >
            <ArrowDown className="h-5 w-5" />
          </button>
        </div>
      </div>
    </Card>
  )
}
