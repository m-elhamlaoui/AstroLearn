"use client"

import { useState } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { ArrowUp, ArrowDown, Calendar, Clock, Tag, ArrowLeft, Send } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import { formatDistanceToNow, format } from "date-fns"
import { ArticleComment } from "@/components/article-comment"

// This would be fetched from the backend in production
const getArticleById = (id: string) => {
  // Sample article data
  return {
    id: Number.parseInt(id),
    title: "The Future of Mars Colonization",
    content: `
      <p>Mars has captivated human imagination for centuries, but only in recent decades has technology advanced enough to make colonization seem possible. SpaceX, NASA, and other space agencies around the world are developing plans to send humans to the Red Planet, with the ultimate goal of establishing permanent settlements.</p>
      
      <h2>Challenges of Mars Colonization</h2>
      
      <p>The journey to Mars presents numerous challenges:</p>
      
      <ul>
        <li><strong>Radiation Exposure:</strong> Without Earth's protective magnetic field, Mars colonists would be exposed to harmful cosmic radiation.</li>
        <li><strong>Low Gravity:</strong> Mars has about 38% of Earth's gravity, which could lead to muscle atrophy and bone density loss over time.</li>
        <li><strong>Limited Resources:</strong> Colonists would need to develop systems for producing food, water, and oxygen using limited Martian resources.</li>
        <li><strong>Psychological Isolation:</strong> The psychological impact of living millions of miles away from Earth, with limited communication and no possibility of quick return, poses significant mental health challenges.</li>
      </ul>
      
      <h2>Technological Solutions</h2>
      
      <p>Despite these challenges, technological innovations are paving the way for potential colonization:</p>
      
      <ul>
        <li><strong>In-Situ Resource Utilization (ISRU):</strong> Technologies that can extract water from Martian soil and produce oxygen from the CO2-rich atmosphere.</li>
        <li><strong>3D Printing:</strong> Structures could be built using Martian regolith, reducing the need to transport building materials from Earth.</li>
        <li><strong>Advanced Life Support Systems:</strong> Closed-loop systems that recycle water, air, and waste to sustain human life indefinitely.</li>
      </ul>
      
      <h2>Timeline for Mars Colonization</h2>
      
      <p>Most experts agree on a phased approach to Mars colonization:</p>
      
      <ol>
        <li><strong>Robotic Precursors (Present-2030):</strong> Continued exploration with rovers and the first sample return missions.</li>
        <li><strong>First Human Landing (2030s):</strong> Short-duration missions focused on exploration and testing technologies.</li>
        <li><strong>Research Outpost (2040s):</strong> Establishment of a permanent research base with rotating crews.</li>
        <li><strong>Self-Sustaining Colony (2050s and beyond):</strong> Growth into a settlement that could survive without regular resupply from Earth.</li>
      </ol>
      
      <p>The colonization of Mars represents one of humanity's greatest potential achievements. While the challenges are immense, the technological progress we've made in recent years suggests that humans may indeed become a multi-planetary species within this century.</p>
    `,
    image: "/placeholder.svg?height=500&width=1000",
    author: {
      id: 101,
      name: "Elena Rodriguez",
      profileImage: "/placeholder.svg?height=50&width=50",
      bio: "Astrophysicist and space exploration enthusiast with a focus on Mars habitability studies.",
    },
    publishDate: "2023-11-15T14:30:00Z",
    readTime: 8, // minutes
    votes: 128,
    tags: ["Mars", "Colonization", "Space Travel", "SpaceX", "NASA"],
    comments: [
      {
        id: 1,
        author: {
          id: 102,
          name: "Marcus Chen",
          profileImage: "/placeholder.svg?height=50&width=50",
        },
        content: "Great article! I wonder how we'll solve the radiation problem for long-term habitation.",
        publishDate: "2023-11-16T10:15:00Z",
        votes: 24,
      },
      {
        id: 2,
        author: {
          id: 103,
          name: "Sophia Williams",
          profileImage: "/placeholder.svg?height=50&width=50",
        },
        content:
          "The psychological aspects of Mars colonization are often overlooked. I'd love to see more research on how humans adapt to such isolated environments over years.",
        publishDate: "2023-11-17T08:45:00Z",
        votes: 18,
      },
      {
        id: 3,
        author: {
          id: 104,
          name: "David Kim",
          profileImage: "/placeholder.svg?height=50&width=50",
        },
        content:
          "I think the timeline is too optimistic. We're still struggling with many basic technologies needed for Mars habitation.",
        publishDate: "2023-11-18T14:20:00Z",
        votes: -3,
      },
    ],
  }
}

export default function ArticlePage({ params }: { params: { id: string } }) {
  // In a real app, this would be a server component fetching data from the backend
  const article = getArticleById(params.id)
  const [votes, setVotes] = useState(article.votes)
  const [userVote, setUserVote] = useState<"up" | "down" | null>(null)
  const [comments, setComments] = useState(article.comments)
  const [newComment, setNewComment] = useState("")

  // Format dates
  const formattedDate = format(new Date(article.publishDate), "MMMM d, yyyy")
  const timeAgo = formatDistanceToNow(new Date(article.publishDate), { addSuffix: true })

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

  // Handle comment submission
  const handleSubmitComment = () => {
    if (!newComment.trim()) return

    // In a real app, this would call an API to add the comment
    const newCommentObj = {
      id: comments.length + 1,
      author: {
        id: 999, // Current user ID
        name: "Current User", // Current user name
        profileImage: "/placeholder.svg?height=50&width=50", // Current user profile image
      },
      content: newComment,
      publishDate: new Date().toISOString(),
      votes: 0,
    }

    setComments([...comments, newCommentObj])
    setNewComment("")
  }

  // Handle comment voting
  const handleCommentVote = (commentId: number, voteType: "up" | "down") => {
    setComments(
      comments.map((comment) => {
        if (comment.id === commentId) {
          // Simple implementation - in a real app, you'd track user's previous votes
          if (voteType === "up") {
            return { ...comment, votes: comment.votes + 1 }
          } else {
            return { ...comment, votes: comment.votes - 1 }
          }
        }
        return comment
      }),
    )
  }

  return (
    <div className="flex min-h-screen bg-black text-white">
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300">
        <div className="container mx-auto max-w-4xl">
          {/* Back Button */}
          <Link href="/articles" className="inline-flex items-center gap-2 text-gray-400 hover:text-white mb-6">
            <ArrowLeft className="h-4 w-4" />
            Back to Articles
          </Link>

          {/* Article Header */}
          <header className="mb-8">
            <h1 className="text-4xl font-bold mb-4">{article.title}</h1>

            {/* Author and Meta Information */}
            <div className="flex flex-wrap items-center gap-6 mb-6">
              <Link
                href={`/profile/${article.author.id}`}
                className="flex items-center gap-3 hover:opacity-80 transition-opacity"
              >
                <Avatar className="h-10 w-10 border-2 border-white">
                  <AvatarImage src={article.author.profileImage || "/placeholder.svg"} alt={article.author.name} />
                  <AvatarFallback>{article.author.name.charAt(0)}</AvatarFallback>
                </Avatar>
                <div>
                  <div className="text-sm font-medium text-white">{article.author.name}</div>
                  <div className="text-xs text-gray-400">{timeAgo}</div>
                </div>
              </Link>

              <div className="flex items-center gap-2 text-gray-400">
                <Calendar className="h-4 w-4" />
                <span className="text-xs">{formattedDate}</span>
              </div>

              <div className="flex items-center gap-2 text-gray-400">
                <Clock className="h-4 w-4" />
                <span className="text-xs">{article.readTime} min read</span>
              </div>
            </div>

            {/* Tags */}
            <div className="flex flex-wrap gap-2 mb-6">
              {article.tags.map((tag) => (
                <Link
                  key={tag}
                  href={`/articles/tags/${tag.toLowerCase()}`}
                  className="text-xs px-3 py-1 bg-gray-800 text-white rounded-full hover:bg-gray-700 transition-colors"
                >
                  <span className="flex items-center gap-1">
                    <Tag className="h-3 w-3" />
                    {tag}
                  </span>
                </Link>
              ))}
            </div>
          </header>

          {/* Featured Image */}
          <div className="relative h-80 md:h-96 mb-8 rounded-xl overflow-hidden">
            <Image src={article.image || "/placeholder.svg"} alt={article.title} fill className="object-cover" />
          </div>

          {/* Article Content */}
          <div className="mb-8">
            <div className="prose prose-invert max-w-none" dangerouslySetInnerHTML={{ __html: article.content }} />
          </div>

          {/* Voting Section */}
          <div className="flex items-center gap-4 border-t border-gray-800 pt-6">
            <div className="flex flex-col items-center">
              <button
                className={`p-2 rounded-full bg-gray-800 hover:bg-gray-700 transition-colors ${userVote === "up" ? "text-green-500" : ""}`}
                aria-label="Upvote"
                onClick={() => handleVote("up")}
              >
                <ArrowUp className="h-6 w-6" />
              </button>

              <span
                className={`text-lg font-medium my-1 ${
                  votes > 0 ? "text-green-500" : votes < 0 ? "text-red-500" : "text-gray-400"
                }`}
              >
                {votes}
              </span>

              <button
                className={`p-2 rounded-full bg-gray-800 hover:bg-gray-700 transition-colors ${userVote === "down" ? "text-red-500" : ""}`}
                aria-label="Downvote"
                onClick={() => handleVote("down")}
              >
                <ArrowDown className="h-6 w-6" />
              </button>
            </div>

            <div className="ml-4">
              <h3 className="text-lg font-medium mb-1">Was this article helpful?</h3>
              <p className="text-sm text-gray-400">Your feedback helps us improve our content.</p>
            </div>
          </div>

          {/* Comments Section */}
          <div className="mt-12">
            <h2 className="text-2xl font-bold mb-6">Comments ({comments.length})</h2>

            {/* Add Comment Form */}
            <div className="mb-8">
              <div className="flex gap-4">
                <Avatar className="h-10 w-10">
                  <AvatarImage src="/placeholder.svg?height=50&width=50" alt="Your Avatar" />
                  <AvatarFallback>YA</AvatarFallback>
                </Avatar>
                <div className="flex-1">
                  <Textarea
                    placeholder="Add a comment..."
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    className="bg-gray-800 border-gray-700 min-h-[100px] mb-2"
                  />
                  <div className="flex justify-end">
                    <Button onClick={handleSubmitComment} disabled={!newComment.trim()}>
                      <Send className="h-4 w-4 mr-2" />
                      Post Comment
                    </Button>
                  </div>
                </div>
              </div>
            </div>

            {/* Comments List */}
            <div className="space-y-6">
              {comments.map((comment) => (
                <ArticleComment key={comment.id} comment={comment} onVote={handleCommentVote} />
              ))}
            </div>
          </div>

          {/* Author Bio */}
          <div className="mt-12 p-6 bg-gray-900 rounded-xl">
            <div className="flex items-start gap-4">
              <Avatar className="h-16 w-16 border-2 border-white">
                <AvatarImage src={article.author.profileImage || "/placeholder.svg"} alt={article.author.name} />
                <AvatarFallback>{article.author.name.charAt(0)}</AvatarFallback>
              </Avatar>

              <div>
                <h3 className="text-xl font-bold mb-2">About {article.author.name}</h3>
                <p className="text-gray-300 mb-4">{article.author.bio}</p>
                <Link
                  href={`/profile/${article.author.id}`}
                  className="text-white hover:text-gray-300 transition-colors"
                >
                  View Profile
                </Link>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
