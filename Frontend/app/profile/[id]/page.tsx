"use client"

import { useState } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { ArticleCard } from "@/components/profile-article-card"
import { UserBadge } from "@/components/user-badge"
import { Edit, Camera, ArrowUp, ArrowDown, PenSquare } from 'lucide-react'
import Image from "next/image"
import Link from "next/link"

// Sample user data - would be fetched from backend in production
const getUserById = (id: string, isCurrentUser: boolean) => {
  // Simulate async data fetching
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        id: Number(id) || 1, // Handle potential NaN if id is not a number
        username: "CosmicExplorer",
        bio: "Space enthusiast and amateur astronomer. Fascinated by the mysteries of the cosmos and dedicated to sharing knowledge about our universe.",
        profileImage: "/placeholder.svg?height=200&width=200",
        coverImage: "/placeholder.svg?height=400&width=1200",
        xp: 7500,
        joinDate: "2022-06-15T00:00:00Z",
        isCurrentUser: isCurrentUser, // Determine if the profile belongs to the current user
        articles: [
          {
            id: 1,
            title: "The Future of Mars Colonization",
            summary: "Exploring the challenges and possibilities of establishing human settlements on the Red Planet.",
            image: "/placeholder.svg?height=300&width=500",
            publishDate: "2023-11-15T14:30:00Z",
            votes: 128,
            tags: ["Mars", "Colonization", "Space Travel"],
          },
          {
            id: 2,
            title: "Understanding Black Holes",
            summary: "A comprehensive guide to one of the universe's most mysterious phenomena.",
            image: "/placeholder.svg?height=300&width=500",
            publishDate: "2023-10-05T16:45:00Z",
            votes: 189,
            tags: ["Black Holes", "Astrophysics", "Space"],
          },
        ],
        upvotedArticles: [
          {
            id: 3,
            title: "James Webb's Latest Discoveries",
            summary: "A deep dive into the groundbreaking observations from NASA's most powerful space telescope.",
            image: "/placeholder.svg?height=300&width=500",
            publishDate: "2023-11-10T09:15:00Z",
            votes: 245,
            tags: ["James Webb", "Telescope", "Astronomy"],
          },
          {
            id: 4,
            title: "The Search for Exoplanets",
            summary: "How astronomers are discovering and analyzing planets outside our solar system.",
            image: "/placeholder.svg?height=300&width=500",
            publishDate: "2023-10-28T11:20:00Z",
            votes: 156,
            tags: ["Exoplanets", "Astronomy", "Space Exploration"],
          },
        ],
        downvotedArticles: [
          {
            id: 5,
            title: "Conspiracy Theories About Space",
            summary: "Debunking common misconceptions and conspiracy theories about space exploration.",
            image: "/placeholder.svg?height=300&width=500",
            publishDate: "2023-09-18T08:30:00Z",
            votes: -42,
            tags: ["Misconceptions", "Conspiracy Theories", "Space Facts"],
          },
        ],
      })
    }, 50) // Simulate network delay
  })
}

// Helper function to determine badge level based on XP
const getBadgeLevel = (xp: number) => {
  if (xp >= 10000) return "GALACTIC"
  if (xp >= 5000) return "ASTRONAUT"
  if (xp >= 2000) return "EXPLORER"
  return "NOVICE"
}

// Define the type for the user data
type UserData = Awaited<ReturnType<typeof getUserById>>

// Client Component to handle state and UI
function ProfileClient({ initialUser }: { initialUser: UserData }) {
  const [activeTab, setActiveTab] = useState("published")
  const [editMode, setEditMode] = useState(false)
  const [profileData, setProfileData] = useState({
    username: initialUser.username,
    bio: initialUser.bio,
    profileImage: initialUser.profileImage,
    coverImage: initialUser.coverImage,
  })
  const [articles, setArticles] = useState(initialUser.articles)
  const badgeLevel = getBadgeLevel(initialUser.xp)

  // Handle article deletion
  const handleDeleteArticle = (articleId: number) => {
    setArticles(articles.filter((article) => article.id !== articleId))
  }

  // Handle profile update
  const handleProfileUpdate = () => {
    // In a real app, this would call an API to update the user profile
    console.log("Profile updated:", profileData)
    setEditMode(false)
    // Potentially update initialUser state if needed, or refetch
  }

  // Function to close dialogs programmatically
  const closeDialog = () => {
    const dialog = document.querySelector('[role="dialog"][data-state="open"]')
    if (dialog) {
      const closeButton = dialog.querySelector('button[aria-label="Close"]') as HTMLElement | null
      closeButton?.click()
    }
  }

  return (
    <div className="flex min-h-screen bg-black text-white">
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 ml-12 transition-all duration-300">
        {/* Cover Image */}
        <div className="relative h-64 md:h-80 group">
          <Image src={profileData.coverImage || "/placeholder.svg"} alt="Cover" fill className="object-cover" priority />
          {initialUser.isCurrentUser && (
            <Dialog>
              <DialogTrigger asChild>
                <Button
                  variant="outline"
                  size="icon"
                  className="absolute top-4 right-4 bg-black/50 border-white/20 hover:bg-black/70 h-8 w-8 transition-all duration-300 hover:scale-110 opacity-0 group-hover:opacity-100"
                >
                  <Camera className="h-4 w-4" />
                </Button>
              </DialogTrigger>
              <DialogContent className="bg-gray-900 border-gray-800">
                <DialogHeader>
                  <DialogTitle>Update Cover Image</DialogTitle>
                </DialogHeader>
                <div className="space-y-4 py-4">
                  <div className="mb-2">
                    <label className="block text-xs text-gray-400 mb-1">Upload new image</label>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={e => {
                        const file = e.target.files?.[0]
                        if (file) {
                          const reader = new FileReader()
                          reader.onload = ev => setProfileData({ ...profileData, coverImage: ev.target?.result as string })
                          reader.readAsDataURL(file)
                        }
                      }}
                      className="block w-full text-sm text-gray-300 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                    />
                  </div>
                  <Input
                    placeholder="Or enter image URL"
                    value={profileData.coverImage}
                    onChange={e => setProfileData({ ...profileData, coverImage: e.target.value })}
                    className="bg-gray-800 border-gray-700"
                  />
                  <div className="flex justify-end">
                    <Button
                      onClick={() => {
                        setProfileData({ ...profileData, coverImage: initialUser.coverImage })
                        closeDialog()
                      }}
                      variant="outline"
                      className="mr-2 border-gray-700 text-gray-300 hover:bg-gray-800"
                    >
                      Cancel
                    </Button>
                    <Button onClick={() => { handleProfileUpdate(); closeDialog(); }} className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600">Save Changes</Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          )}
          {/* Profile picture overlays cover image */}
          <div className="absolute left-8 bottom-[-64px] md:bottom-[-80px] z-20 group">
            <div className="relative h-32 w-32 md:h-40 md:w-40 rounded-full overflow-hidden border-4 border-white bg-gray-900 shadow-lg">
              <Image
                src={profileData.profileImage || "/placeholder.svg"}
                alt={initialUser.username}
                width={160}
                height={160}
                className="object-cover"
              />
              {initialUser.isCurrentUser && (
                <Dialog>
                  <DialogTrigger asChild>
                    <Button
                      variant="outline"
                      size="icon"
                      className="absolute bottom-2 right-2 bg-black/50 border-white/20 hover:bg-black/70 h-8 w-8 transition-all duration-300 hover:scale-110 opacity-0 group-hover:opacity-100"
                    >
                      <Camera className="h-4 w-4" />
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="bg-gray-900 border-gray-800">
                    <DialogHeader>
                      <DialogTitle>Update Profile Picture</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                      <div className="mb-2">
                        <label className="block text-xs text-gray-400 mb-1">Upload new image</label>
                        <input
                          type="file"
                          accept="image/*"
                          onChange={e => {
                            const file = e.target.files?.[0]
                            if (file) {
                              const reader = new FileReader()
                              reader.onload = ev => setProfileData({ ...profileData, profileImage: ev.target?.result as string })
                              reader.readAsDataURL(file)
                            }
                          }}
                          className="block w-full text-sm text-gray-300 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                        />
                      </div>
                      <Input
                        placeholder="Or enter image URL"
                        value={profileData.profileImage}
                        onChange={e => setProfileData({ ...profileData, profileImage: e.target.value })}
                        className="bg-gray-800 border-gray-700"
                      />
                      <div className="flex justify-end">
                        <Button
                          onClick={() => {
                            setProfileData({ ...profileData, profileImage: initialUser.profileImage })
                            closeDialog()
                          }}
                          variant="outline"
                          className="mr-2 border-gray-700 text-gray-300 hover:bg-gray-800"
                        >
                          Cancel
                        </Button>
                        <Button onClick={() => { handleProfileUpdate(); closeDialog(); }} className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600">Save Changes</Button>
                      </div>
                    </div>
                  </DialogContent>
                </Dialog>
              )}
            </div>
          </div>
        </div>

        {/* Profile Info - Adjusted padding-top */}
        <div className="pt-24 md:pt-28 px-8 pb-8">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-end mb-6">
            <div className="mb-4 md:mb-0">
              {editMode ? (
                <Input
                  value={profileData.username}
                  onChange={e => setProfileData({ ...profileData, username: e.target.value })}
                  className="text-3xl font-bold mb-1 bg-gray-800 border-gray-700 w-full md:w-auto"
                />
              ) : (
                <h1 className="text-3xl font-bold mb-1">{profileData.username}</h1>
              )}
              <div className="flex items-center space-x-4 text-gray-400 text-sm">
                <UserBadge level={badgeLevel} />
                <span>XP: {initialUser.xp.toLocaleString()}</span>
                <span>Joined: {new Date(initialUser.joinDate).toLocaleDateString()}</span>
              </div>
            </div>
            {initialUser.isCurrentUser && (
              <div className="flex space-x-2">
                {editMode ? (
                  <>
                    <Button onClick={handleProfileUpdate} className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600">
                      Save Profile
                    </Button>
                    <Button onClick={() => {
                      setEditMode(false)
                      setProfileData({
                        username: initialUser.username,
                        bio: initialUser.bio,
                        profileImage: initialUser.profileImage,
                        coverImage: initialUser.coverImage,
                      })
                    }} variant="outline" className="border-gray-700 text-gray-300 hover:bg-gray-800">
                      Cancel
                    </Button>
                  </>
                ) : (
                  <Button onClick={() => setEditMode(true)} variant="outline" size="icon" className="border-gray-700 text-gray-300 hover:bg-gray-800">
                    <Edit className="h-4 w-4" />
                  </Button>
                )}
                <Link href="/article-edit">
                  <Button variant="outline" size="icon" className="border-gray-700 text-gray-300 hover:bg-gray-800">
                    <PenSquare className="h-4 w-4" />
                  </Button>
                </Link>
              </div>
            )}
          </div>

          {/* Bio Section */}
          <div className="mb-8">
            <h2 className="text-xl font-semibold mb-2">Bio</h2>
            {editMode ? (
              <Textarea
                value={profileData.bio}
                onChange={e => setProfileData({ ...profileData, bio: e.target.value })}
                className="bg-gray-800 border-gray-700 min-h-[100px]"
              />
            ) : (
              <p className="text-gray-300 whitespace-pre-wrap">{profileData.bio || "No bio provided."}</p>
            )}
          </div>

          {/* Tabs for Articles and Interactions */}
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="grid w-full grid-cols-3 bg-gray-900 border border-gray-800 rounded-lg mb-6">
              <TabsTrigger value="published" className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-purple-600 data-[state=active]:to-blue-600 data-[state=active]:text-white rounded-md">Published Articles</TabsTrigger>
              <TabsTrigger value="upvoted" className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-purple-600 data-[state=active]:to-blue-600 data-[state=active]:text-white rounded-md">Upvoted</TabsTrigger>
              <TabsTrigger value="downvoted" className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-purple-600 data-[state=active]:to-blue-600 data-[state=active]:text-white rounded-md">Downvoted</TabsTrigger>
            </TabsList>

            {/* Published Articles Tab */}
            <TabsContent value="published">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {articles.length > 0 ? (
                  articles.map((article) => (
                    <ArticleCard
                      key={article.id}
                      article={article}
                      showEditDelete={initialUser.isCurrentUser}
                      onDelete={handleDeleteArticle}
                    />
                  ))
                ) : (
                  <p className="text-gray-500 col-span-full text-center py-8">No articles published yet.</p>
                )}
              </div>
            </TabsContent>

            {/* Upvoted Articles Tab */}
            <TabsContent value="upvoted">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {initialUser.upvotedArticles.length > 0 ? (
                  initialUser.upvotedArticles.map((article) => (
                    <ArticleCard key={article.id} article={article} interactionType="upvoted" />
                  ))
                ) : (
                  <p className="text-gray-500 col-span-full text-center py-8">No upvoted articles yet.</p>
                )}
              </div>
            </TabsContent>

            {/* Downvoted Articles Tab */}
            <TabsContent value="downvoted">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {initialUser.downvotedArticles.length > 0 ? (
                  initialUser.downvotedArticles.map((article) => (
                    <ArticleCard key={article.id} article={article} interactionType="downvoted" />
                  ))
                ) : (
                  <p className="text-gray-500 col-span-full text-center py-8">No downvoted articles yet.</p>
                )}
              </div>
            </TabsContent>
          </Tabs>
        </div>

        {/* Add some bottom margin for spacing */}
        <div className="h-16"></div>
      </main>
    </div>
  )
}

// Server Component to fetch data and render the Client Component
export default async function ProfilePage({ params }: { params: { id: string } }) {
  // For demo purposes, we'll consider the profile to be the current user if id is "me"
  // Ensure params.id is awaited or handled correctly if needed before this check
  const isCurrentUser = params.id === "me"
  const user = await getUserById(params.id === "me" ? "1" : params.id, isCurrentUser) // Use a default ID like "1" if params.id is "me"

  if (!user) {
    // Handle case where user is not found, e.g., return a 404 page
    // For now, just return null or a message
    return <div>User not found</div>
  }

  return <ProfileClient initialUser={user} />
}
