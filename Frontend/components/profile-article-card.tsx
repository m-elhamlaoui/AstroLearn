"use client"

import { useState } from "react"
import { Card } from "@/components/ui/card"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { ArrowUp, ArrowDown, MoreVertical, Trash2 } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import { formatDistanceToNow } from "date-fns"

interface Article {
  id: number
  title: string
  summary: string
  image: string
  publishDate: string
  votes: number
  tags: string[]
}

export interface ArticleCardProps { // Added export
  article: Article;
  showEditDelete?: boolean; // Renamed from isOwner and made optional
  onDelete?: (id: number) => void;
  interactionType?: "upvoted" | "downvoted"; // Added optional prop
}

export function ArticleCard({ article, showEditDelete = false, onDelete, interactionType }: ArticleCardProps) {
  const [votes, setVotes] = useState(article.votes);
  // Initialize userVote based on interactionType if provided
  const [userVote, setUserVote] = useState<"up" | "down" | null>(interactionType === "upvoted" ? "up" : interactionType === "downvoted" ? "down" : null);

  // Format the date to "X days/hours/minutes ago"
  const formattedDate = formatDistanceToNow(new Date(article.publishDate), { addSuffix: true })

  // Handle voting
  const handleVote = (voteType: "up" | "down") => {
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
  }

  // Handle article deletion
  const handleDelete = () => {
    if (onDelete) {
      onDelete(article.id)
    }
  }

  return (
    <Card className="bg-gray-900 border-gray-800 overflow-hidden rounded-xl hover:shadow-lg hover:shadow-white/5 transition-all duration-300 group">
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
        {/* Article Header with Title and Actions */}
        <div className="flex justify-between items-start mb-3">
          <Link href={`/articles/${article.id}`} className="block group-hover:text-white transition-colors">
            <h3 className="text-xl font-bold">{article.title}</h3>
          </Link>

          {/* Three dots menu for article owner */}
          {showEditDelete && (
            <DropdownMenu>
              <DropdownMenuTrigger className="p-1 rounded-full hover:bg-gray-800 transition-colors">
                <MoreVertical className="h-5 w-5 text-gray-400" />
              </DropdownMenuTrigger>
              <DropdownMenuContent className="bg-gray-800 border-gray-700 text-white">
                <DropdownMenuItem className="text-red-500 hover:text-red-400 cursor-pointer" onClick={handleDelete}>
                  <Trash2 className="h-4 w-4 mr-2" />
                  Delete Article
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>

        {/* Date */}
        <div className="text-xs text-gray-400 mb-3">{formattedDate}</div>

        {/* Article Summary */}
        <p className="text-gray-400 text-sm mb-4 line-clamp-3">{article.summary}</p>

        {/* Tags */}
        <div className="flex flex-wrap gap-2 mb-4">
          {article.tags.map((tag) => (
            <span key={tag} className="text-xs px-2 py-1 bg-gray-800 text-gray-300 rounded-full">
              {tag}
            </span>
          ))}
        </div>

        {/* Voting */}
        <div className="flex items-center gap-1">
          <button
            onClick={() => handleVote("up")}
            className={`p-1 rounded hover:bg-gray-800 transition-colors ${userVote === "up" ? "text-green-500" : "text-gray-400"}`}
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
            className={`p-1 rounded hover:bg-gray-800 transition-colors ${userVote === "down" ? "text-red-500" : "text-gray-400"}`}
            aria-label="Downvote"
          >
            <ArrowDown className="h-5 w-5" />
          </button>
        </div>
      </div>
    </Card>
  )
}
