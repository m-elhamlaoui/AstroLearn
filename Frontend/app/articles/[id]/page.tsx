"use client"

import { useState, useEffect } from "react"
import axiosInstance from "../../../lib/axiosInstance" // Adjusted path
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { ArrowUp, ArrowDown, Calendar, Clock, Tag, ArrowLeft, Send, Loader2 } from "lucide-react" // Added Loader2
import Link from "next/link"
import Image from "next/image"
import { formatDistanceToNow, format } from "date-fns"
import { ArticleComment } from "@/components/article-comment"
import { BloomingStars } from "@/components/blooming-stars"

// Interfaces based on DTOs and page needs
interface Author {
  id: number;
  name: string;
  profileImage: string;
  bio?: string; // Optional, as it's not in ArticleDTO but was in sample
}

interface CommentAuthor {
  id: number;
  name: string;
  profileImage: string;
}
interface Comment {
  id: number;
  author: CommentAuthor;
  content: string;
  publishDate: string; // from createdAt
  votes: number; // Not directly in CommentDTO, might need separate handling or removal
}

interface Article {
  id: number;
  title: string;
  content: string;
  image: string; // from imageUrls[0]
  author: Author;
  publishDate: string; // from createdAt
  readTime?: number; // Not in DTO, might be calculated or omitted
  votes: number; // from score
  tags: string[];
}

// Backend DTOs (for reference during transformation)
interface ArticleDTO {
  id: number;
  title: string;
  summary: string; // Used for preview, full content for page
  content: string;
  imageUrls: string[];
  createdAt: string;
  authorId: number;
  authorUsername: string;
  score: number;
  commentCount: number;
  tags: string[];
}

interface CommentDTO {
  id: number;
  content: string;
  articleId: number;
  createdAt: string;
  userId: number;
  authorUsername: string;
}


export default function ArticlePage({ params }: { params: { id: string } }) {
  const [article, setArticle] = useState<Article | null>(null)
  const [comments, setComments] = useState<Comment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  const [userVote, setUserVote] = useState<"up" | "down" | null>(null) // For article voting UI
  const [newComment, setNewComment] = useState("")

  useEffect(() => {
    const articleId = params.id
    if (!articleId) {
      setError("Article ID is missing.")
      setLoading(false)
      return
    }

    const fetchArticleData = async () => {
      setLoading(true)
      setError(null)
      try {
        // Fetch article details
        const articleResponse = await axiosInstance.get<ArticleDTO>(`/articles/${articleId}`)
        const dto = articleResponse.data
        const transformedArticle: Article = {
          id: dto.id,
          title: dto.title,
          content: dto.content,
          image: dto.imageUrls && dto.imageUrls.length > 0 ? dto.imageUrls[0] : "/placeholder.svg?height=500&width=1000",
          author: {
            id: dto.authorId,
            name: dto.authorUsername,
            profileImage: "/placeholder.svg?height=50&width=50", // Placeholder, ideally fetch user details
            // bio: "Placeholder bio..." // Placeholder, ideally fetch user details
          },
          publishDate: dto.createdAt,
          votes: dto.score,
          tags: dto.tags || [],
          // readTime: calculateReadTime(dto.content), // Implement if needed
        }
        setArticle(transformedArticle)
        // Set initial votes for UI based on fetched article
        // setVotes(transformedArticle.votes) // This state is now part of 'article' state

        // Fetch comments
        const commentsResponse = await axiosInstance.get<CommentDTO[]>(`/articles/${articleId}/comments`)
        const transformedComments: Comment[] = commentsResponse.data.map(commentDto => ({
          id: commentDto.id,
          author: {
            id: commentDto.userId,
            name: commentDto.authorUsername,
            profileImage: "/placeholder.svg?height=50&width=50", // Placeholder
          },
          content: commentDto.content,
          publishDate: commentDto.createdAt,
          votes: 0, // CommentDTO doesn't have votes, default to 0 or remove voting
        }))
        setComments(transformedComments)

      } catch (err: any) {
        console.error("Failed to fetch article data:", err)
        setError(err.response?.data?.message || err.message || "Failed to load article.")
      } finally {
        setLoading(false)
      }
    }

    fetchArticleData()
  }, [params.id])

  // Handle article voting
  const handleVote = async (voteType: "up" | "down") => {
    if (!article) return;
    // Placeholder userId, replace with actual authenticated user ID
    const userId = 1 
    try {
      const response = await axiosInstance.post<ArticleDTO>(`/articles/${article.id}/vote/user/${userId}`, { voteType })
      setArticle(prevArticle => prevArticle ? { ...prevArticle, votes: response.data.score } : null)
      setUserVote(voteType) // Update UI to reflect user's current vote
    } catch (err) {
      console.error("Error voting on article:", err)
      // Optionally, show an error to the user
    }
  }

  // Handle comment submission
  const handleSubmitComment = async () => {
    if (!newComment.trim() || !article) return
    // Placeholder userId, replace with actual authenticated user ID
    const userId = 1 
    try {
      const response = await axiosInstance.post<CommentDTO>(`/articles/${article.id}/comments/user/${userId}`, {
        content: newComment,
        // articleId is implicit in the endpoint
      })
      const newCommentDto = response.data
      const addedComment: Comment = {
        id: newCommentDto.id,
        author: {
          id: newCommentDto.userId,
          name: newCommentDto.authorUsername, // Assuming backend returns this or fetch separately
          profileImage: "/placeholder.svg?height=50&width=50",
        },
        content: newCommentDto.content,
        publishDate: newCommentDto.createdAt,
        votes: 0, // Default or remove
      }
      setComments(prevComments => [...prevComments, addedComment])
      setNewComment("")
    } catch (err) {
      console.error("Error submitting comment:", err)
      // Optionally, show an error to the user
    }
  }
  
  // Comment voting is not directly supported by a simple DTO field or endpoint in the provided list.
  // The ArticleComment component might need its own logic or this feature might be simplified/removed.
  // For now, the local handleCommentVote is removed as it doesn't interact with backend.
  // If ArticleComment handles its own voting via API, that's separate.
  // Dummy onVote for ArticleComment to satisfy prop requirements
  const handleDummyCommentVote = (commentId: number, voteType: "up" | "down") => {
    console.log(`[DummyVote] Comment ${commentId} vote: ${voteType}. Needs backend integration.`);
    // In a real scenario, this would call a backend endpoint.
  };

  if (loading) {
    return (
      <div className="flex min-h-screen bg-black text-white items-center justify-center">
        <MinimalNavigation />
        <Loader2 className="h-12 w-12 animate-spin text-indigo-400" />
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
            onClick={() => window.location.reload()}
            className="mt-4 border-gray-700 text-gray-300 hover:bg-gray-800"
          >
            Try Again
          </Button>
        </div>
      </div>
    )
  }

  if (!article) {
    return (
      <div className="flex min-h-screen bg-black text-white items-center justify-center">
        <MinimalNavigation />
        <p className="text-xl">Article not found.</p>
      </div>
    )
  }

  // Format dates for display
  const formattedDate = format(new Date(article.publishDate), "MMMM d, yyyy")
  const timeAgo = formatDistanceToNow(new Date(article.publishDate), { addSuffix: true })

  return (
    <div className="flex min-h-screen bg-black text-white relative">
      {/* Background Animation */}
      <BloomingStars />
      
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300 relative z-10">
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

            {article.readTime && ( // Conditionally render read time if available
              <div className="flex items-center gap-2 text-gray-400">
                <Clock className="h-4 w-4" />
                <span className="text-xs">{article.readTime} min read</span>
              </div>
            )}
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
                className={`p-2 rounded-full bg-gray-800 hover:bg-gray-700 transition-colors ${userVote === "up" ? "text-green-500" : "text-gray-400 hover:text-green-400"}`}
                aria-label="Upvote"
                onClick={() => handleVote("up")}
              >
                <ArrowUp className="h-6 w-6" />
              </button>

              <span
                className={`text-lg font-medium my-1 ${
                  article.votes > 0 ? "text-green-500" : article.votes < 0 ? "text-red-500" : "text-gray-400"
                }`}
              >
                {article.votes}
              </span>

              <button
                className={`p-2 rounded-full bg-gray-800 hover:bg-gray-700 transition-colors ${userVote === "down" ? "text-red-500" : "text-gray-400 hover:text-red-400"}`}
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
                <ArticleComment 
                  key={comment.id} 
                  comment={comment} 
                  onVote={handleDummyCommentVote} // Pass dummy function
                />
              ))}
            </div>
          </div>

          {/* Author Bio - Conditionally render if bio is available */}
          {article.author.bio && (
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
          )}
        </div>
      </main>
    </div>
  )
}
