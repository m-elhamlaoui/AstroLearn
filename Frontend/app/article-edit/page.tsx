"use client"

import type React from "react"
import axios from "axios"
import { useState, useRef, useEffect } from "react"
import { useRouter } from "next/navigation"
import Image from "next/image"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Bold,
  Italic,
  Underline,
  AlignLeft,
  AlignCenter,
  AlignRight,
  ImageIcon,
  Link,
  List,
  ListOrdered,
  CheckCircle2,
  Loader2,
  Tag as TagIcon,
  X,
} from "lucide-react"

interface ArticleDTO {
  id?: number
  title: string
  summary: string
  content: string
  imageUrls: string[]
  createdAt?: string
  authorId?: number
  authorUsername?: string
  score?: number
  commentCount?: number
  tags: Set<string>
}

interface ArticleTagDTO {
  id?: number
  name: string
}

// API base URL - replace with your actual backend URL
const API_BASE_URL = "http://localhost:8080"

export default function ArticleEditPage() {
  const router = useRouter()
  const [title, setTitle] = useState("")
  const [summary, setSummary] = useState("")
  const [content, setContent] = useState("")
  const [coverImage, setCoverImage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [currentCheck, setCurrentCheck] = useState(0)
  const [tags, setTags] = useState<Set<string>>(new Set())
  const [newTag, setNewTag] = useState("")
  const editorRef = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  
  // Mock user ID - in a real app, this would come from authentication
  const currentUserId = 1

  const checks = [
    "Validating article content...",
    "Processing tags...",
    "Optimizing images...",
    "Finalizing and publishing...",
  ]

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        if (e.target?.result) {
          setCoverImage(e.target.result as string)
        }
      }
      reader.readAsDataURL(file)
    }
  }

  const handleFormatting = (format: string) => {
    if (!editorRef.current) return

    document.execCommand(format, false)
    editorRef.current.focus()
  }

  const handleImageClick = () => {
    fileInputRef.current?.click()
  }

  // Handle tag addition
  const handleAddTag = () => {
    if (newTag.trim()) {
      const updatedTags = new Set(tags)
      updatedTags.add(newTag.trim())
      setTags(updatedTags)
      setNewTag("")
    }
  }

  // Handle tag removal
  const handleRemoveTag = (tagToRemove: string) => {
    const updatedTags = new Set(tags)
    updatedTags.delete(tagToRemove)
    setTags(updatedTags)
  }

  // Handle form submission
  const handleSubmit = async () => {
    if (!title.trim() || !content.trim()) {
      alert("Please provide both a title and content for your article")
      return
    }

    setIsSubmitting(true)

    try {
      // Prepare the article data
      const articleData: ArticleDTO = {
        title: title.trim(),
        summary: summary.trim(),
        content: content.trim(),
        imageUrls: coverImage ? [coverImage] : [],
        tags: tags
      }

      // Start simulated check animation
      for (let i = 0; i < checks.length - 1; i++) {
        setCurrentCheck(i)
        // Wait between each check
        await new Promise((resolve) => setTimeout(resolve, 800))
      }

      // Send the article to the backend
      const response = await axios.post(
        `${API_BASE_URL}/articles/${currentUserId}`,
        articleData
      )

      // Final check animation
      setCurrentCheck(checks.length - 1)
      await new Promise((resolve) => setTimeout(resolve, 800))

      // Handle successful submission
      if (response.data && response.data.id) {
        // If tags were set, update the article with tags
        // Note: This step might not be needed if the backend handles tags in the initial POST
        // But we're adding it based on the separate endpoint in ArticleController
        if (tags.size > 0) {
          await axios.put(
            `${API_BASE_URL}/articles/${response.data.id}/tags`, 
            Array.from(tags)
          )
        }

        // Redirect to article view or profile
        router.push(`/profile/${currentUserId}`)
      }
    } catch (error) {
      console.error("Error submitting article:", error)
      alert("Failed to submit your article. Please try again.")
    } finally {
      setIsSubmitting(false)
    }
  }

  useEffect(() => {
    if (editorRef.current) {
      editorRef.current.focus()
    }
  }, [])

  return (
    <div className="min-h-screen bg-black text-white">
      <MinimalNavigation />

      <main className="container mx-auto px-4 py-8 max-w-4xl">
        <h1 className="text-3xl font-bold mb-8 text-center">Create New Article</h1>

        {isSubmitting ? (
          <div className="flex flex-col items-center justify-center space-y-8 py-16">
            <div className="relative w-64 h-64">
              <div className="absolute inset-0 bg-gradient-to-r from-purple-500 to-blue-500 rounded-full opacity-20 animate-pulse"></div>
              <div className="absolute inset-0 flex items-center justify-center">
                <Loader2 className="w-16 h-16 text-blue-400 animate-spin" />
              </div>
            </div>

            <div className="space-y-4 w-full max-w-md">
              {checks.map((check, index) => (
                <div
                  key={index}
                  className={`flex items-center space-x-3 p-3 rounded-lg ${
                    index < currentCheck
                      ? "bg-green-900/20 text-green-400"
                      : index === currentCheck
                        ? "bg-blue-900/20 text-blue-400 animate-pulse"
                        : "bg-gray-900/20 text-gray-500"
                  }`}
                >
                  {index < currentCheck ? (
                    <CheckCircle2 className="w-5 h-5 text-green-400" />
                  ) : index === currentCheck ? (
                    <Loader2 className="w-5 h-5 animate-spin" />
                  ) : (
                    <div className="w-5 h-5 rounded-full border border-gray-700" />
                  )}
                  <span>{check}</span>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <>
            <div className="mb-6">
              <label htmlFor="title" className="block text-sm font-medium mb-2">
                Article Title
              </label>
              <Input
                id="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Enter a compelling title..."
                className="bg-gray-900 border-gray-700 text-white"
              />
            </div>

            <div className="mb-6">
              <label htmlFor="summary" className="block text-sm font-medium mb-2">
                Summary
              </label>
              <Input
                id="summary"
                value={summary}
                onChange={(e) => setSummary(e.target.value)}
                placeholder="Brief summary of your article..."
                className="bg-gray-900 border-gray-700 text-white"
              />
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium mb-2">Cover Image</label>
              <div
                className="relative h-64 bg-gray-900 border border-dashed border-gray-700 rounded-lg flex items-center justify-center cursor-pointer overflow-hidden"
                onClick={handleImageClick}
              >
                {coverImage ? (
                  <Image src={coverImage || "/placeholder.svg"} alt="Cover" fill className="object-cover" />
                ) : (
                  <div className="text-center">
                    <ImageIcon className="mx-auto h-12 w-12 text-gray-500" />
                    <p className="mt-2 text-sm text-gray-500">Click to upload a cover image</p>
                    <p className="mt-1 text-xs text-gray-600">This will be stored as the first image URL</p>
                  </div>
                )}
                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={handleImageUpload}
                  accept="image/*"
                  className="hidden"
                />
              </div>
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium mb-2">Content</label>
              <div className="bg-gray-900 border border-gray-700 rounded-lg overflow-hidden">
                <div className="flex flex-wrap gap-1 p-2 border-b border-gray-700 bg-gray-800">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleFormatting("bold")}
                    className="text-gray-300 hover:text-white hover:bg-gray-700"
                  >
                    <Bold className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleFormatting("italic")}
                    className="text-gray-300 hover:text-white hover:bg-gray-700"
                  >
                    <Italic className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleFormatting("underline")}
                    className="text-gray-300 hover:text-white hover:bg-gray-700"
                  >
                    <Underline className="h-4 w-4" />
                  </Button>
                  <div className="h-6 w-px bg-gray-700 mx-1"></div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleFormatting("justifyLeft")}
                    className="text-gray-300 hover:text-white hover:bg-gray-700"
                  >
                    <AlignLeft className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleFormatting("justifyCenter")}
                    className="text-gray-300 hover:text-white hover:bg-gray-700"
                  >
                    <AlignCenter className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleFormatting("justifyRight")}
                    className="text-gray-300 hover:text-white hover:bg-gray-700"
                  >
                    <AlignRight className="h-4 w-4" />
                  </Button>
                  <div className="h-6 w-px bg-gray-700 mx-1"></div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleFormatting("insertUnorderedList")}
                    className="text-gray-300 hover:text-white hover:bg-gray-700"
                  >
                    <List className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleFormatting("insertOrderedList")}
                    className="text-gray-300 hover:text-white hover:bg-gray-700"
                  >
                    <ListOrdered className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleFormatting("createLink")}
                    className="text-gray-300 hover:text-white hover:bg-gray-700"
                  >
                    <Link className="h-4 w-4" />
                  </Button>
                </div>
                <div
                  ref={editorRef}
                  contentEditable
                  className="min-h-[300px] p-4 focus:outline-none"
                  onInput={(e) => setContent((e.target as HTMLDivElement).innerHTML)}
                  dangerouslySetInnerHTML={{ __html: content }}
                />
              </div>
            </div>

            {/* Tags section */}
            <div className="mb-6">
              <label className="block text-sm font-medium mb-2">Tags</label>
              <div className="flex flex-wrap gap-2 mb-3">
                {Array.from(tags).map((tag) => (
                  <div 
                    key={tag}
                    className="flex items-center bg-blue-900/30 text-blue-400 px-3 py-1 rounded-full"
                  >
                    <span className="mr-1">{tag}</span>
                    <button 
                      onClick={() => handleRemoveTag(tag)}
                      className="text-blue-400 hover:text-blue-300"
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </div>
                ))}
              </div>
              <div className="flex gap-2">
                <Input
                  value={newTag}
                  onChange={(e) => setNewTag(e.target.value)}
                  placeholder="Add a tag..."
                  className="bg-gray-900 border-gray-700 text-white"
                  onKeyPress={(e) => e.key === 'Enter' && handleAddTag()}
                />
                <Button 
                  onClick={handleAddTag}
                  variant="outline"
                  className="border-gray-700 text-gray-300"
                >
                  <TagIcon className="h-4 w-4 mr-1" />
                  Add
                </Button>
              </div>
            </div>

            <div className="flex justify-end">
              <Button variant="outline" className="mr-2" onClick={() => router.back()}>
                Cancel
              </Button>
              <Button
                onClick={handleSubmit}
                className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600"
              >
                Post Article
              </Button>
            </div>
          </>
        )}
      </main>
    </div>
  )
}


