"use client"

import type React from "react"
import axios from "axios" // For direct S3 upload

import { useState, useRef, useEffect } from "react"
import axiosInstance from "../../lib/axiosInstance" // Your configured axios instance
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
  const [summary, setSummary] = useState("")
  const [content, setContent] = useState("")
  const [tags, setTags] = useState("") // Comma-separated tags
  const [coverImage, setCoverImage] = useState<string | null>(null) // This will store the S3 URL
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [isUploadingImage, setIsUploadingImage] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submissionError, setSubmissionError] = useState<string | null>(null)
  // const [currentCheck, setCurrentCheck] = useState(0) // To be removed
  const editorRef = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  // const checks = [...] // To be removed

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      setSelectedFile(file)
      // Display a preview locally before uploading to S3
      const reader = new FileReader()
      reader.onload = (event) => {
        if (event.target?.result) {
          setCoverImage(event.target.result as string) // Temporary local preview
        }
      }
      reader.readAsDataURL(file)
    }
  }

  const uploadImageToS3 = async (file: File): Promise<string | null> => {
    setIsUploadingImage(true)
    setSubmissionError(null)
    console.log("[S3 Upload] Starting image upload for file:", file)
    try {
      // 1. Get pre-signed URL from your backend
      console.log("[S3 Upload] Fetching pre-signed URL from /generate-upload-url")
      const response = await axiosInstance.get<{ uploadUrl: string; key: string }>("/generate-upload-url")
      const presignedUrlData = response.data
      console.log("[S3 Upload] Received pre-signed URL data:", presignedUrlData)
      
      if (!presignedUrlData || !presignedUrlData.uploadUrl) {
        console.error("[S3 Upload] Failed to get pre-signed URL or uploadUrl is missing.")
        throw new Error("Failed to get valid pre-signed URL.")
      }

      const actualUploadUrl = presignedUrlData.uploadUrl;

      // 2. Upload file to S3 using the pre-signed URL
      console.log(`[S3 Upload] Attempting PUT to S3 with URL: ${actualUploadUrl}`)
      console.log(`[S3 Upload] File details: name=${file.name}, size=${file.size}, type=${file.type}`)
      const s3PutHeaders = { "Content-Type": file.type }
      console.log("[S3 Upload] Headers for S3 PUT:", s3PutHeaders)
      
      await axios.put(actualUploadUrl, file, {
        headers: s3PutHeaders,
      })
      console.log("[S3 Upload] Successfully uploaded file to S3.")

      // 3. The actual URL of the uploaded image is the pre-signed URL without query parameters
      // This should be derived from the uploadUrl that was used for the PUT request.
      const imageUrl = actualUploadUrl.split("?")[0]
      console.log("[S3 Upload] Derived image URL for storage:", imageUrl)
      setCoverImage(imageUrl) // Update coverImage state with the final S3 URL
      setSelectedFile(null) // Clear selected file after successful upload
      return imageUrl
    } catch (error: any) {
      console.error("[S3 Upload] Error during image upload process:", error)
      if (error.response) {
        console.error("[S3 Upload] Axios error response data:", error.response.data)
        console.error("[S3 Upload] Axios error response status:", error.response.status)
        console.error("[S3 Upload] Axios error response headers:", error.response.headers)
      } else if (error.request) {
        console.error("[S3 Upload] Axios error request (no response received):", error.request)
      } else {
        console.error("[S3 Upload] General error message:", error.message)
      }
      setSubmissionError(`Image upload failed: ${error.message}. Status: ${error.response?.status || 'N/A'}. Check console for details.`)
      setCoverImage(null) // Clear preview on error
      return null
    } finally {
      console.log("[S3 Upload] Finished image upload attempt.")
      setIsUploadingImage(false)
    }
  }


  const handleFormatting = (format: string, value?: string) => {
    if (!editorRef.current) return

    document.execCommand(format, false, value) // Pass value for commands like createLink
    editorRef.current?.focus()
  }

  const handleInsertLink = () => {
    const url = prompt("Enter the URL:")
    if (url) {
      handleFormatting("createLink", url)
    }
  }

  const handleImageClick = () => {
    fileInputRef.current?.click()
  }

  const handleSubmit = async () => {
    if (!title.trim()) {
      setSubmissionError("Article title is required.")
      return
    }
    const articleContent = editorRef.current?.innerHTML || ""
    if (!articleContent.trim()) {
      console.warn("[Submit] Article content is empty.")
      setSubmissionError("Article content cannot be empty.")
      return
    }

    setIsSubmitting(true)
    setSubmissionError(null)
    console.log("[Submit] Starting article submission process.")
    let finalImageUrl = coverImage // Use existing S3 URL if image wasn't changed (i.e. selectedFile is null)

    // If a new file was selected, it needs to be uploaded.
    // If coverImage is a data URL (local preview) and selectedFile is present, it means new upload is needed.
    // If coverImage is an S3 URL and selectedFile is null, it means image wasn't changed.
    if (selectedFile) {
      console.log("[Submit] New file selected. Attempting to upload to S3.")
      const s3Url = await uploadImageToS3(selectedFile)
      if (!s3Url) {
        console.error("[Submit] S3 upload failed. Aborting article submission.")
        setIsSubmitting(false) 
        // uploadImageToS3 already sets submissionError
        return
      }
      finalImageUrl = s3Url
      console.log("[Submit] S3 upload successful. Using URL:", finalImageUrl)
    } else {
      console.log("[Submit] No new file selected. Using existing coverImage URL (if any):", finalImageUrl)
    }

    const articleData = {
      title,
      summary,
      content: articleContent,
      imageUrls: finalImageUrl ? [finalImageUrl] : [],
      tags: tags.split(",").map((tag) => tag.trim()).filter(tag => tag),
      // authorId will be handled by the backend based on the authenticated user
    }

    try {
      // Assuming authorId is derived from the authenticated user on the backend
      // The endpoint is POST /articles/{authorId}, but if backend handles auth,
      // it might just be POST /articles
      // For now, let's assume a placeholder authorId or that backend handles it.
      // The DTO expects authorId, so we might need to pass it or adjust backend.
      // Let's use a placeholder authorId '1' as per previous ArticleController.
       // TODO: Replace '1' with actual authenticated user ID from auth context. // No longer needed, backend uses principal
       console.log("[Submit] Attempting to POST article data to /articles:", articleData)
       const response = await axiosInstance.post(`/articles`, articleData) // Use endpoint without authorId
       
       console.log("[Submit] Article created successfully:", response.data)
      // Redirect to the articles page or the new article page
      router.push("/articles") // Or router.push(`/articles/${response.data.id}`) if ID is returned
    } catch (error: any) {
      console.error("[Submit] Error creating article:", error)
      if (error.response) {
        console.error("[Submit] Axios error response data:", error.response.data)
        console.error("[Submit] Axios error response status:", error.response.status)
      }
      setSubmissionError(error.response?.data?.message || error.message || "Failed to create article. Check console for details.")
    } finally {
      console.log("[Submit] Finished article submission attempt.")
      setIsSubmitting(false)
    }
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
        <h1 className="text-3xl font-bold mb-8 text-center">
          {/* TODO: Add logic for "Edit Article" if an existing article ID is present */}
          Create New Article 
        </h1>

        {/* Submission Error Display */}
        {submissionError && (
          <div className="mb-4 p-3 bg-red-900/30 border border-red-700 text-red-300 rounded-md">
            <p>{submissionError}</p>
          </div>
        )}

        <form onSubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
            <div className="mb-6">
              <label htmlFor="title" className="block text-sm font-medium mb-1 text-gray-300">
                Article Title <span className="text-red-500">*</span>
              </label>
              <Input
                id="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Enter a compelling title..."
                className="bg-gray-800 border-gray-700 text-white focus:border-indigo-500"
                required
              />
            </div>

            <div className="mb-6">
              <label htmlFor="summary" className="block text-sm font-medium mb-1 text-gray-300">
                Summary
              </label>
              <Input
                id="summary"
                value={summary}
                onChange={(e) => setSummary(e.target.value)}
                placeholder="A brief summary of your article..."
                className="bg-gray-800 border-gray-700 text-white focus:border-indigo-500"
              />
            </div>
            
            <div className="mb-6">
              <label className="block text-sm font-medium mb-1 text-gray-300">Cover Image</label>
              <div
                className="relative h-64 bg-gray-800 border-2 border-dashed border-gray-700 rounded-lg flex items-center justify-center cursor-pointer overflow-hidden hover:border-indigo-500 transition-colors"
                onClick={handleImageClick}
              >
                {isUploadingImage && (
                  <div className="absolute inset-0 bg-black/70 flex flex-col items-center justify-center z-10">
                    <Loader2 className="w-12 h-12 text-indigo-400 animate-spin" />
                    <p className="mt-2 text-indigo-300">Uploading image...</p>
                  </div>
                )}
                {coverImage && !isUploadingImage ? (
                  <Image src={coverImage || "/placeholder.svg"} alt="Cover preview" fill className="object-cover" />
                ) : (
                  !isUploadingImage && (
                    <div className="text-center">
                      <ImageIcon className="mx-auto h-12 w-12 text-gray-500" />
                      <p className="mt-2 text-sm text-gray-500">Click to upload a cover image</p>
                      <p className="text-xs text-gray-600">Recommended: 1200x630px</p>
                    </div>
                  )
                )}
                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={handleFileSelect}
                  accept="image/*"
                  className="hidden"
                  disabled={isUploadingImage}
                />
              </div>
               {selectedFile && !isUploadingImage && (
                <p className="text-xs text-gray-400 mt-1">Selected: {selectedFile.name} (Preview shown. Click "Post Article" to upload to S3 if not already done via a dedicated upload button)</p>
              )}
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium mb-1 text-gray-300">
                Content <span className="text-red-500">*</span>
              </label>
              <div className="bg-gray-800 border border-gray-700 rounded-lg overflow-hidden focus-within:border-indigo-500">
                <div className="flex flex-wrap gap-1 p-2 border-b border-gray-700 bg-gray-900/50">
                  {/* Formatting Buttons */}
                  <Button type="button" variant="ghost" size="sm" onClick={() => handleFormatting("bold")} className="text-gray-300 hover:text-white hover:bg-gray-700"><Bold className="h-4 w-4" /></Button>
                  <Button type="button" variant="ghost" size="sm" onClick={() => handleFormatting("italic")} className="text-gray-300 hover:text-white hover:bg-gray-700"><Italic className="h-4 w-4" /></Button>
                  <Button type="button" variant="ghost" size="sm" onClick={() => handleFormatting("underline")} className="text-gray-300 hover:text-white hover:bg-gray-700"><Underline className="h-4 w-4" /></Button>
                  <div className="h-6 w-px bg-gray-700 mx-1"></div>
                  <Button type="button" variant="ghost" size="sm" onClick={() => handleFormatting("justifyLeft")} className="text-gray-300 hover:text-white hover:bg-gray-700"><AlignLeft className="h-4 w-4" /></Button>
                  <Button type="button" variant="ghost" size="sm" onClick={() => handleFormatting("justifyCenter")} className="text-gray-300 hover:text-white hover:bg-gray-700"><AlignCenter className="h-4 w-4" /></Button>
                  <Button type="button" variant="ghost" size="sm" onClick={() => handleFormatting("justifyRight")} className="text-gray-300 hover:text-white hover:bg-gray-700"><AlignRight className="h-4 w-4" /></Button>
                  <div className="h-6 w-px bg-gray-700 mx-1"></div>
                  <Button type="button" variant="ghost" size="sm" onClick={() => handleFormatting("insertUnorderedList")} className="text-gray-300 hover:text-white hover:bg-gray-700"><List className="h-4 w-4" /></Button>
                  <Button type="button" variant="ghost" size="sm" onClick={() => handleFormatting("insertOrderedList")} className="text-gray-300 hover:text-white hover:bg-gray-700"><ListOrdered className="h-4 w-4" /></Button>
                  <Button type="button" variant="ghost" size="sm" onClick={handleInsertLink} className="text-gray-300 hover:text-white hover:bg-gray-700"><Link className="h-4 w-4" /></Button>
                </div>
                <div
                  ref={editorRef}
                  contentEditable
                  className="min-h-[300px] p-4 focus:outline-none prose prose-invert max-w-none"
                  onInput={(e) => setContent((e.target as HTMLDivElement).innerHTML)} // Keep content state in sync if needed elsewhere, though direct read from ref is also fine
                  // dangerouslySetInnerHTML={{ __html: content }} // Not needed if editorRef.current.innerHTML is source of truth
                />
              </div>
            </div>

            <div className="mb-6">
              <label htmlFor="tags" className="block text-sm font-medium mb-1 text-gray-300">
                Tags (comma-separated)
              </label>
              <Input
                id="tags"
                value={tags}
                onChange={(e) => setTags(e.target.value)}
                placeholder="e.g., space, mars, astronomy"
                className="bg-gray-800 border-gray-700 text-white focus:border-indigo-500"
              />
            </div>

            <div className="flex justify-end items-center mt-8">
              <Button type="button" variant="outline" className="mr-3 border-gray-700 text-gray-300 hover:bg-gray-800" onClick={() => router.back()} disabled={isSubmitting || isUploadingImage}>
                Cancel
              </Button>
              <Button
                type="submit"
                className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white px-6 py-2.5"
                disabled={isSubmitting || isUploadingImage}
              >
                {isSubmitting ? (
                  <Loader2 className="w-5 h-5 animate-spin mr-2" />
                ) : null}
                {isSubmitting ? "Posting..." : "Post Article"}
              </Button>
            </div>
        </form>
      </main>
    </div>
  )
}
