"use client"

import { useState, useEffect } from "react"
import axios from "axios"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { ArticleCard } from "@/components/profile-article-card"
import { UserBadge } from "@/components/user-badge"
import { Edit, Camera, PenSquare, Loader2 } from 'lucide-react'
import Image from "next/image"
import Link from "next/link"
import { useToast } from "@/components/ui/use-toast"

// API client service
const API_BASE_URL = "http://localhost:8088";

// Create axios instance with base configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Types based on backend DTOs
interface UserDTO {
  id: number;
  username: string;
  email: string;
  bio: string;
  password?: string; // Don't display this, included for form completeness
  profileImageUrl: string;
  photoCoverUrl: string;
  role: string;
  verificationStatus: string;
  level: string;
  experiencePoints: number;
  articleCount: number;
  commentCount: number;
  quizCompletionCount: number;
  readingHistoryIds: number[];
  courseProgressIds: number[];
  quizCompletionIds: number[];
  createdSpaceMissionIds: number[];
}

interface ArticleDTO {
  id: number;
  title: string;
  summary: string;
  content: string;
  imageUrls: string[];
  createdAt: string;
  authorId: number;
  authorUsername: string;
  score: number;
  commentCount: number;
  tags: string[];
}

// Transform types for UI
interface UserData extends Partial<UserDTO> {
  id: number;
  username: string;
  bio: string;
  profileImage: string;
  coverImage: string;
  xp: number;
  isCurrentUser: boolean;
  joinDate: string;
  articles: UIArticle[];
  upvotedArticles: UIArticle[];
  downvotedArticles: UIArticle[];
}

interface UIArticle {
  id: number;
  title: string;
  summary: string;
  image: string;
  publishDate: string;
  votes: number;
  tags: string[];
}

// API functions
const userAPI = {
  // Fetch user by ID
  async getUserById(id: string): Promise<UserDTO> {
    try {
      const response = await api.get(`/users/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error fetching user:", error);
      throw error;
    }
  },

  // Fetch user by username
  async getUserByUsername(username: string): Promise<UserDTO> {
    try {
      const response = await api.get(`/users/username/${username}`);
      return response.data;
    } catch (error) {
      console.error("Error fetching user by username:", error);
      throw error;
    }
  },

  // Update user
  async updateUser(id: number, userData: Partial<UserDTO>): Promise<UserDTO> {
    try {
      const response = await api.put(`/users/${id}`, userData);
      return response.data;
    } catch (error) {
      console.error("Error updating user:", error);
      throw error;
    }
  },

  // Request verification
  async requestVerification(id: number): Promise<void> {
    try {
      await api.put(`/users/${id}/verification/request`);
    } catch (error) {
      console.error("Error requesting verification:", error);
      throw error;
    }
  }
};

const articleAPI = {
  // Get articles by author ID
  async getArticlesByAuthor(authorId: number): Promise<ArticleDTO[]> {
    // Note: This endpoint doesn't exist in the provided backend code
    // We would need to add an endpoint in ArticleController to handle this
    // For now, we'll use a mock implementation
    try {
      // In a real implementation, this would be something like:
      // const response = await api.get(`/articles/author/${authorId}`);
      // For now, we'll use getAllArticles() and filter client-side
      const response = await api.get('/articles');
      // Filter articles by author ID
      return response.data.content.filter((article: ArticleDTO) => article.authorId === authorId);
    } catch (error) {
      console.error("Error fetching articles:", error);
      throw error;
    }
  },

  // Create a new article
  async createArticle(article: Partial<ArticleDTO>, authorId: number): Promise<ArticleDTO> {
    try {
      const response = await api.post(`/articles/${authorId}`, article);
      return response.data;
    } catch (error) {
      console.error("Error creating article:", error);
      throw error;
    }
  },

  // Update an article
  async updateArticle(id: number, userId: number, article: Partial<ArticleDTO>): Promise<ArticleDTO> {
    try {
      const response = await api.put(`/articles/${id}/user/${userId}`, article);
      return response.data;
    } catch (error) {
      console.error("Error updating article:", error);
      throw error;
    }
  },

  // Delete an article
  async deleteArticle(id: number, userId: number): Promise<void> {
    try {
      await api.delete(`/articles/${id}/user/${userId}`);
    } catch (error) {
      console.error("Error deleting article:", error);
      throw error;
    }
  },

  // Vote on an article
  async voteArticle(articleId: number, userId: number, voteValue: number): Promise<ArticleDTO> {
    try {
      const response = await api.post(`/articles/${articleId}/vote/user/${userId}`, {
        voteType: voteValue > 0 ? "UPVOTE" : "DOWNVOTE"
      });
      return response.data;
    } catch (error) {
      console.error("Error voting on article:", error);
      throw error;
    }
  },

  // Get user's voted articles
  // Note: This endpoint doesn't exist in the backend yet
  async getUserVotedArticles(userId: number): Promise<{ upvoted: ArticleDTO[], downvoted: ArticleDTO[] }> {
    // In a real implementation, we would have endpoints like:
    // /users/{userId}/votes?voteType=UPVOTE
    // /users/{userId}/votes?voteType=DOWNVOTE

    // For now, return empty arrays
    console.warn("getUserVotedArticles: This API doesn't exist in the backend yet");
    return { upvoted: [], downvoted: [] };
  }
};

// Helper function to transform UserDTO to UserData
const transformUserDTO = (dto: UserDTO, isCurrentUser: boolean): UserData => {
  return {
    id: dto.id,
    username: dto.username,
    bio: dto.bio || "",
    profileImage: dto.profileImageUrl || "/placeholder.svg?height=200&width=200",
    coverImage: dto.photoCoverUrl || "/placeholder.svg?height=400&width=1200",
    xp: dto.experiencePoints,
    isCurrentUser: isCurrentUser,
    joinDate: new Date().toISOString(), // This field isn't in the DTO, would need to be added
    articles: [], // Will be populated separately
    upvotedArticles: [],
    downvotedArticles: [],
    // Include other fields from UserDTO as needed
  };
};

// Helper function to transform ArticleDTO to UIArticle
const transformArticleDTO = (dto: ArticleDTO): UIArticle => {
  return {
    id: dto.id,
    title: dto.title,
    summary: dto.summary,
    image: dto.imageUrls?.[0] || "/placeholder.svg?height=300&width=500",
    publishDate: dto.createdAt,
    votes: dto.score,
    tags: dto.tags || [],
  };
};

// Helper function to determine badge level based on XP
const getBadgeLevel = (xp: number) => {
  if (xp >= 10000) return "GALACTIC";
  if (xp >= 5000) return "ASTRONAUT";
  if (xp >= 2000) return "EXPLORER";
  return "NOVICE";
};

// Client Component to handle state and UI
function ProfileClient({ userId, isCurrentUser }: { userId: string, isCurrentUser: boolean }) {
  const [activeTab, setActiveTab] = useState("published");
  const [editMode, setEditMode] = useState(false);
  const [loading, setLoading] = useState(true);
  const [userData, setUserData] = useState<UserData | null>(null);
  const [profileData, setProfileData] = useState({
    username: "",
    bio: "",
    profileImage: "",
    coverImage: "",
  });
  const { toast } = useToast();

  // Fetch user data on component mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        // Fetch user data
        const userDto = await userAPI.getUserById(userId);
        const user = transformUserDTO(userDto, isCurrentUser);

        // Fetch user's articles
        const articles = await articleAPI.getArticlesByAuthor(userDto.id);
        user.articles = articles.map(transformArticleDTO);

        // Fetch voted articles if available (this endpoint doesn't exist yet)
        try {
          const votedArticles = await articleAPI.getUserVotedArticles(userDto.id);
          user.upvotedArticles = votedArticles.upvoted.map(transformArticleDTO);
          user.downvotedArticles = votedArticles.downvoted.map(transformArticleDTO);
        } catch (error) {
          console.warn("Could not fetch voted articles:", error);
          // Use empty arrays as fallback (already initialized)
        }

        setUserData(user);
        setProfileData({
          username: user.username,
          bio: user.bio || "",
          profileImage: user.profileImage,
          coverImage: user.coverImage,
        });
      } catch (error) {
        console.error("Error fetching profile data:", error);
        toast({
          title: "Error",
          description: "Failed to load profile data. Please try again later.",
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [userId, isCurrentUser, toast]);

  // Handle article deletion
  const handleDeleteArticle = async (articleId: number) => {
    if (!userData) return;

    try {
      await articleAPI.deleteArticle(articleId, userData.id);
      setUserData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          articles: prev.articles.filter(article => article.id !== articleId)
        };
      });
      toast({
        title: "Success",
        description: "Article deleted successfully",
      });
    } catch (error) {
      console.error("Error deleting article:", error);
      toast({
        title: "Error",
        description: "Failed to delete article. Please try again.",
        variant: "destructive",
      });
    }
  };

  // Handle profile update
  const handleProfileUpdate = async () => {
    if (!userData) return;

    try {
      const updateData: Partial<UserDTO> = {
        username: profileData.username,
        bio: profileData.bio,
        profileImageUrl: profileData.profileImage,
        photoCoverUrl: profileData.coverImage,
      };

      await userAPI.updateUser(userData.id, updateData);

      // Update local state with new values
      setUserData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          username: profileData.username,
          bio: profileData.bio,
          profileImage: profileData.profileImage,
          coverImage: profileData.coverImage,
        };
      });

      setEditMode(false);
      toast({
        title: "Success",
        description: "Profile updated successfully",
      });
    } catch (error) {
      console.error("Error updating profile:", error);
      toast({
        title: "Error",
        description: "Failed to update profile. Please try again.",
        variant: "destructive",
      });
    }
  };

  // Handle requesting verification
  const handleRequestVerification = async () => {
    if (!userData) return;

    try {
      await userAPI.requestVerification(userData.id);
      toast({
        title: "Success",
        description: "Verification request submitted successfully",
      });
    } catch (error) {
      console.error("Error requesting verification:", error);
      toast({
        title: "Error",
        description: "Failed to request verification. Please try again.",
        variant: "destructive",
      });
    }
  };

  // Function to close dialogs programmatically
  const closeDialog = () => {
    const dialog = document.querySelector('[role="dialog"][data-state="open"]');
    if (dialog) {
      const closeButton = dialog.querySelector('button[aria-label="Close"]') as HTMLElement | null;
      closeButton?.click();
    }
  };

  if (loading) {
    return (
        <div className="flex min-h-screen bg-black text-white items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-blue-500" />
          <span className="ml-2">Loading profile...</span>
        </div>
    );
  }

  if (!userData) {
    return (
        <div className="flex min-h-screen bg-black text-white items-center justify-center">
          <div className="text-center">
            <h1 className="text-2xl font-bold mb-4">User Not Found</h1>
            <p className="text-gray-400">The requested profile could not be found.</p>
            <Link href="/">
              <Button className="mt-6 bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600">
                Return Home
              </Button>
            </Link>
          </div>
        </div>
    );
  }

  const badgeLevel = getBadgeLevel(userData.xp);

  return (
      <div className="flex min-h-screen bg-black text-white">
        {/* Minimal Navigation */}
        <MinimalNavigation />

        {/* Main Content */}
        <main className="flex-1 ml-12 transition-all duration-300">
          {/* Cover Image */}
          <div className="relative h-64 md:h-80 group">
            <Image src={profileData.coverImage || "/placeholder.svg"} alt="Cover" fill className="object-cover" priority />
            {userData.isCurrentUser && (
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
                              const file = e.target.files?.[0];
                              if (file) {
                                const reader = new FileReader();
                                reader.onload = ev => setProfileData({ ...profileData, coverImage: ev.target?.result as string });
                                reader.readAsDataURL(file);
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
                              setProfileData({ ...profileData, coverImage: userData.coverImage });
                              closeDialog();
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
                    alt={userData.username}
                    width={160}
                    height={160}
                    className="object-cover"
                />
                {userData.isCurrentUser && (
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
                                  const file = e.target.files?.[0];
                                  if (file) {
                                    const reader = new FileReader();
                                    reader.onload = ev => setProfileData({ ...profileData, profileImage: ev.target?.result as string });
                                    reader.readAsDataURL(file);
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
                                  setProfileData({ ...profileData, profileImage: userData.profileImage });
                                  closeDialog();
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
                  <span>XP: {userData.xp.toLocaleString()}</span>
                  <span>Joined: {new Date(userData.joinDate).toLocaleDateString()}</span>
                </div>
              </div>
              {userData.isCurrentUser && (
                  <div className="flex space-x-2">
                    {editMode ? (
                        <>
                          <Button onClick={handleProfileUpdate} className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600">
                            Save Profile
                          </Button>
                          <Button onClick={() => {
                            setEditMode(false);
                            setProfileData({
                              username: userData.username,
                              bio: userData.bio || "",
                              profileImage: userData.profileImage,
                              coverImage: userData.coverImage,
                            });
                          }} variant="outline" className="border-gray-700 text-gray-300 hover:bg-gray-800">
                            Cancel
                          </Button>
                        </>
                    ) : (
                        <>
                          <Button onClick={() => setEditMode(true)} variant="outline" size="icon" className="border-gray-700 text-gray-300 hover:bg-gray-800">
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Link href="/article-edit">
                            <Button variant="outline" size="icon" className="border-gray-700 text-gray-300 hover:bg-gray-800">
                              <PenSquare className="h-4 w-4" />
                            </Button>
                          </Link>
                          {userData.isCurrentUser && (
                              <Button
                                  onClick={handleRequestVerification}
                                  variant="outline"
                                  className="border-gray-700 text-gray-300 hover:bg-gray-800"
                              >
                                Request Verification
                              </Button>
                          )}
                        </>
                    )}
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
                  {userData.articles.length > 0 ? (
                      userData.articles.map((article) => (
                          <ArticleCard
                              key={article.id}
                              article={article}
                              showEditDelete={userData.isCurrentUser}
                              onDelete={() => handleDeleteArticle(article.id)}
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
                  {userData.upvotedArticles.length > 0 ? (
                      userData.upvotedArticles.map((article) => (
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
                  {userData.downvotedArticles.length > 0 ? (
                      userData.downvotedArticles.map((article) => (
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
  );
}

// Server Component to fetch data and render the Client Component
export default function ProfilePage({ params }: { params: { id: string } }) {
  // For production usage, we would fetch the current user ID from an auth context/cookie
  // For demo purposes, we'll consider the profile to be the current user if id is "me"
  const isCurrentUser = params.id === "me";

  // Use a real user ID if "me", or use the provided ID
  const userId = params.id === "me" ? "1" : params.id;

  return <ProfileClient userId={userId} isCurrentUser={isCurrentUser} />;
}