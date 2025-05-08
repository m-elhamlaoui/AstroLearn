"use client"

import type React from "react"

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
} from "lucide-react"
import { BloomingStars } from "@/components/blooming-stars"

export default function ArticleEditPage() {
  const router = useRouter()
  const [title, setTitle] = useState("")
  const [content, setContent] = useState("")
  const [coverImage, setCoverImage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [currentCheck, setCurrentCheck] = useState(0)
  const editorRef = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const checks = [
    "Checking content relevance...",
    "Analyzing for plagiarism...",
    "Verifying scientific accuracy...",
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

  const handleSubmit = async () => {
    if (!title.trim() || !content.trim()) {
      alert("Please provide both a title and content for your article")
      return
    }

    setIsSubmitting(true)

    // Simulate AI checks with timeouts
    for (let i = 0; i < checks.length; i++) {
      setCurrentCheck(i)
      // Wait for 1.5 seconds between each check
      await new Promise((resolve) => setTimeout(resolve, 1500))
    }

    // Simulate posting the article
    setTimeout(() => {
      setIsSubmitting(false)
      // Redirect to profile page
      router.push("/profile/1") // Assuming user ID is 1
    }, 1000)
  }

  useEffect(() => {
    if (editorRef.current) {
      editorRef.current.focus()
    }
  }, [])

  return (
    <div className="min-h-screen bg-black text-white relative">
      {/* Background Animation */}
      <BloomingStars />
      
      <MinimalNavigation />

      <main className="container mx-auto px-4 py-8 max-w-4xl relative z-10">
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
