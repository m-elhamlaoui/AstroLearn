"use client"

import { useState } from "react"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { ArrowUp, ArrowDown } from "lucide-react"
import Link from "next/link"
import { formatDistanceToNow } from "date-fns"

interface Comment {
  id: number
  author: {
    id: number
    name: string
    profileImage: string
  }
  content: string
  publishDate: string
  votes: number
}

interface ArticleCommentProps {
  comment: Comment
  onVote: (commentId: number, voteType: "up" | "down") => void
}

export function ArticleComment({ comment, onVote }: ArticleCommentProps) {
  const [userVote, setUserVote] = useState<"up" | "down" | null>(null)
  const [votes, setVotes] = useState(comment.votes)

  // Format the date to "X days/hours/minutes ago"
  const formattedDate = formatDistanceToNow(new Date(comment.publishDate), { addSuffix: true })

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

    // Call the parent component's vote handler
    onVote(comment.id, voteType)
  }

  return (
    <div className="flex gap-4">
      <Avatar className="h-10 w-10">
        <AvatarImage src={comment.author.profileImage || "/placeholder.svg"} alt={comment.author.name} />
        <AvatarFallback>{comment.author.name.charAt(0)}</AvatarFallback>
      </Avatar>

      <div className="flex-1">
        <div className="bg-gray-800 rounded-lg p-4">
          <div className="flex justify-between items-center mb-2">
            <Link href={`/profile/${comment.author.id}`} className="font-medium hover:underline">
              {comment.author.name}
            </Link>
            <span className="text-xs text-gray-400">{formattedDate}</span>
          </div>

          <p className="text-gray-300 whitespace-pre-wrap">{comment.content}</p>

          <div className="flex items-center gap-1 mt-3">
            <button
              onClick={() => handleVote("up")}
              className={`p-1 rounded hover:bg-gray-700 transition-colors ${userVote === "up" ? "text-green-500" : "text-gray-400"}`}
              aria-label="Upvote"
            >
              <ArrowUp className="h-4 w-4" />
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
              <ArrowDown className="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
