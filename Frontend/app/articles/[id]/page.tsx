"use client"

import { useState, useEffect } from "react"
import axios from "axios"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { ArrowUp, ArrowDown, Calendar, Clock, Tag, ArrowLeft, Send } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import { formatDistanceToNow, format, parseISO } from "date-fns"

// API base URL - in production, this would come from environment variables
const API_BASE_URL = "http://localhost:8080/api"

export default function ArticlePage({ params }: { params: { id: string } }) {
  const [article, setArticle] = useState(null)
  const [comments, setComments] = useState([])
  const [author, setAuthor] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [votes, setVotes] = useState(0)
  const [userVote, setUserVote] = useState(null)
  const [newComment, setNewComment] = useState("")

  // Fetch article and comments data
  useEffect(() => {
    const fetchArticleData = async () => {
      try {
        setLoading(true)
        
        // Fetch article details
        const articleRes = await axios.get(`${API_BASE_URL}/articles/${params.id}`)
        setArticle(articleRes.data)
        setVotes(articleRes.data.score)
        
        // Fetch comments
        const commentsRes = await axios.get(`${API_BASE_URL}/articles/${params.id}/comments`)
        setComments(commentsRes.data)
        
        // Fetch author details
        if (articleRes.data.authorId) {
          const authorRes = await axios.get(`${API_BASE_URL}/users/${articleRes.data.authorId}`)
          setAuthor(authorRes.data)
        }
        
        setLoading(false)
      } catch (err) {
        console.error("Error fetching article data:", err)
        setError("Failed to load article. Please try again later.")
        setLoading(false)
      }
    }

    fetchArticleData()
  }, [params.id])

  // Handle article voting
  const handleArticleVote = async (voteType) => {
    try {
      // In a real implementation, you would get the user ID from authentication
      const currentUserId = 1; // This would come from auth context
      
      // Prepare vote request
      const voteRequest = {
        voteValue: voteType === "up" ? 1 : -1,
        // Any other fields required by your backend
      }
      
      // Call API to vote on the article
      await axios.post(
        `${API_BASE_URL}/articles/${article.id}/vote/user/${currentUserId}`, 
        voteRequest
      )
      
      // Update local state
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
    } catch (err) {
      console.error("Error voting on article:", err)
      alert("Failed to register your vote. Please try again.")
    }
  }

  // Handle comment submission
  const handleSubmitComment = async () => {
    if (!newComment.trim()) return

    try {
      // Assuming we have a userId for the logged-in user
      const currentUserId = 1; // This would come from auth context
      
      const commentData = {
        content: newComment,
        articleId: article.id
      }

      // Using the endpoint provided in your controller
      const response = await axios.post(
        `${API_BASE_URL}/articles/${article.id}/comments/user/${currentUserId}`, 
        commentData
      )
      
      // Add the new comment to the list
      const newCommentObj = response.data;
      setComments([...comments, newCommentObj])
      setNewComment("")
    } catch (err) {
      console.error("Error posting comment:", err)
      alert("Failed to post your comment. Please try again.")
    }
  }

  // Comment component embedded directly in the ArticlePage
  const Comment = ({ comment }) => {
    const [commentUserVote, setCommentUserVote] = useState(null)
    const [commentVotes, setCommentVotes] = useState(comment.score || 0)

    // Format the date
    const timeAgo = formatDistanceToNow(
      typeof comment.createdAt === "string" ? parseISO(comment.createdAt) : new Date(comment.createdAt),
      { addSuffix: true }
    )

    // Handle comment votes
    const handleCommentVote = async (voteType) => {
      try {
        // In a real implementation, you would get the user ID from authentication
        const currentUserId = 1; // This would come from auth context
        
        // Prepare vote request
        const voteRequest = {
          voteValue: voteType === "up" ? 1 : -1,
        }
        
        // Make API call to vote on the comment
        // This endpoint should match what's expected in your backend
        await axios.post(
          `${API_BASE_URL}/comments/${comment.id}/vote/user/${currentUserId}`, 
          voteRequest
        )
        
        // Update local state for immediate feedback
        if (commentUserVote === voteType) {
          // User is removing their vote
          setCommentVotes(voteType === "up" ? commentVotes - 1 : commentVotes + 1)
          setCommentUserVote(null)
        } else if (commentUserVote === null) {
          // User is adding a new vote
          setCommentVotes(voteType === "up" ? commentVotes + 1 : commentVotes - 1)
          setCommentUserVote(voteType)
        } else {
          // User is changing their vote (e.g., from up to down)
          setCommentVotes(voteType === "up" ? commentVotes + 2 : commentVotes - 2)
          setCommentUserVote(voteType)
        }
        
        // Update the comment in the parent component's state
        setComments(
          comments.map((c) => {
            if (c.id === comment.id) {
              return { ...c, score: commentVotes }
            }
            return c
          })
        )
      } catch (err) {
        console.error("Error voting on comment:", err)
        alert("Failed to register your vote. Please try again.")
      }
    }

    return (
      <div className="flex gap-4">
        {/* User avatar */}
        <Avatar className="h-10 w-10">
          <AvatarImage src={comment.authorProfileImage || "/placeholder.svg?height=50&width=50"} alt={comment.authorUsername} />
          <AvatarFallback>{comment.authorUsername?.charAt(0)}</AvatarFallback>
        </Avatar>

        {/* Comment content */}
        <div className="flex-1">
          <div className="flex items-center justify-between mb-1">
            <Link href={`/profile/${comment.userId}`} className="text-sm font-medium hover:underline">
              {comment.authorUsername}
            </Link>
            <span className="text-xs text-gray-400">{timeAgo}</span>
          </div>

          <p className="text-gray-300 mb-2">{comment.content}</p>

          {/* Voting controls */}
          <div className="flex items-center">
            <button
              className={`p-1 text-gray-400 hover:text-white ${commentUserVote === "up" ? "text-green-500" : ""}`}
              onClick={() => handleCommentVote("up")}
              aria-label="Upvote comment"
            >
              <ArrowUp className="h-4 w-4" />
            </button>

            <span
              className={`text-sm mx-1 ${
                commentVotes > 0 ? "text-green-500" : commentVotes < 0 ? "text-red-500" : "text-gray-400"
              }`}
            >
              {commentVotes}
            </span>

            <button
              className={`p-1 text-gray-400 hover:text-white ${commentUserVote === "down" ? "text-red-500" : ""}`}
              onClick={() => handleCommentVote("down")}
              aria-label="Downvote comment"
            >
              <ArrowDown className="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>
    )
  }

  // Loading state
  if (loading) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p>Loading article...</p>
      </div>
    )
  }

  // Error state
  if (error || !article) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p>{error || "Article not found"}</p>
      </div>
    )
  }

  // Format dates
  const formattedDate = format(parseISO(article.createdAt), "MMMM d, yyyy")
  const timeAgo = formatDistanceToNow(parseISO(article.createdAt), { addSuffix: true })

  // Default image if none provided
  const articleImage = article.imageUrls && article.imageUrls.length > 0 
    ? article.imageUrls[0] 
    : "/placeholder.svg?height=500&width=1000"

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
                href={`/profile/${article.authorId}`}
                className="flex items-center gap-3 hover:opacity-80 transition-opacity"
              >
                <Avatar className="h-10 w-10 border-2 border-white">
                  <AvatarImage 
                    src={author?.profileImageUrl || "/placeholder.svg?height=50&width=50"} 
                    alt={author?.username || article.authorUsername} 
                  />
                  <AvatarFallback>{(author?.username || article.authorUsername)?.charAt(0)}</AvatarFallback>
                </Avatar>
                <div>
                  <div className="text-sm font-medium text-white">{author?.username || article.authorUsername}</div>
                  <div className="text-xs text-gray-400">{timeAgo}</div>
                </div>
              </Link>

              <div className="flex items-center gap-2 text-gray-400">
                <Calendar className="h-4 w-4" />
                <span className="text-xs">{formattedDate}</span>
              </div>

              {/* Read time calculated based on content length */}
              <div className="flex items-center gap-2 text-gray-400">
                <Clock className="h-4 w-4" />
                <span className="text-xs">{Math.ceil(article.content.length / 1500)} min read</span>
              </div>
            </div>

            {/* Tags */}
            <div className="flex flex-wrap gap-2 mb-6">
              {article.tags && article.tags.map((tag) => (
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
            <Image src={articleImage} alt={article.title} fill className="object-cover" />
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
                onClick={() => handleArticleVote("up")}
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
                onClick={() => handleArticleVote("down")}
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
            <h2 className="text-2xl font-bold mb-6">Comments ({article.commentCount || comments.length})</h2>

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
                <Comment key={comment.id} comment={comment} />
              ))}
            </div>
          </div>

          {/* Author Bio */}
          <div className="mt-12 p-6 bg-gray-900 rounded-xl">
            <div className="flex items-start gap-4">
              <Avatar className="h-16 w-16 border-2 border-white">
                <AvatarImage 
                  src={author?.profileImageUrl || "/placeholder.svg?height=50&width=50"} 
                  alt={author?.username || article.authorUsername} 
                />
                <AvatarFallback>{(author?.username || article.authorUsername)?.charAt(0)}</AvatarFallback>
              </Avatar>

              <div>
                <h3 className="text-xl font-bold mb-2">About {author?.username || article.authorUsername}</h3>
                <p className="text-gray-300 mb-4">
                  {author?.bio || "This author hasn't added a bio yet."}
                </p>
                
                {/* User stats */}
                {author && (
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-4 text-sm">
                    <div>
                      <span className="block text-gray-400">Articles</span>
                      <span className="font-semibold">{author.articleCount || 0}</span>
                    </div>
                    <div>
                      <span className="block text-gray-400">Comments</span>
                      <span className="font-semibold">{author.commentCount || 0}</span>
                    </div>
                    <div>
                      <span className="block text-gray-400">Level</span>
                      <span className="font-semibold">{author.level || "Beginner"}</span>
                    </div>
                  </div>
                )}
                
                <Link
                  href={`/profile/${article.authorId}`}
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