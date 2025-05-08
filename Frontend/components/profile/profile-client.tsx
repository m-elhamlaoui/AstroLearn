"use client"

import { useState, useEffect, useRef } from "react" 
import axios from "axios"; // For direct S3 upload
import axiosInstance from "@/lib/axiosInstance"; 
import { useRouter } from "next/navigation"; 
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { ArticleCard } from "@/components/article-card" // Use the standard ArticleCard
import { UserBadge } from "@/components/user-badge"
import { Edit, Camera, ArrowUp, ArrowDown, PenSquare, Loader2 } from 'lucide-react' 
import Image from "next/image"
import Link from "next/link"
import { BloomingStars } from "@/components/blooming-stars"
import { useToast } from "@/components/ui/use-toast"; 

// --- Interfaces ---
// Define Author type expected by ArticleCard
interface Author {
  id: number;
  name: string;
  profileImage: string; // Expect string for ArticleCard
}
// Define Article type based on DTO, ensuring compatibility with ArticleCard
interface Article {
  id: number;
  title: string;
  summary: string;
  image: string;
  publishDate: string;
  votes: number;
  tags: string[];
  currentUserVote?: number | null; 
  author: Author; // Use the Author interface defined above
}

// Define UserData type based on fetched data
interface UserData {
  id: number;
  username: string;
  bio: string | null; 
  profileImage: string | null; 
  coverImage: string | null; 
  xp: number;
  joinDate: string; // Placeholder or fetch if available
  isCurrentUser: boolean; // Determined client-side
  articles: Article[]; // Published articles by this user
  upvotedArticles: Article[];
  downvotedArticles: Article[];
}

// Backend DTOs (for reference during transformation)
interface UserDTO {
  id: number;
  username: string;
  email: string; 
  bio: string | null;
  profileImageUrl: string | null;
  photoCoverUrl: string | null;
  role: string; 
  verificationStatus: string; 
  level: string; 
  experiencePoints: number;
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
  currentUserVote?: number | null;
}

// Helper to transform ArticleDTO to frontend Article type
const transformArticleDTO = (dto: ArticleDTO): Article => ({
  id: dto.id,
  title: dto.title,
  summary: dto.summary,
  image: dto.imageUrls && dto.imageUrls.length > 0 ? dto.imageUrls[0] : "/placeholder.svg",
  publishDate: dto.createdAt,
  votes: dto.score,
  tags: dto.tags || [],
  currentUserVote: dto.currentUserVote,
  author: { 
    id: dto.authorId, 
    name: dto.authorUsername, 
    // Ensure profileImage is always a string for ArticleCard
    profileImage: "/placeholder.svg" // Placeholder - Ideally fetch author's actual image if needed by card
  }, 
});

// Helper function to determine badge level based on XP
const getBadgeLevel = (xp: number) => {
  if (xp >= 10000) return "GALACTIC"
  if (xp >= 5000) return "ASTRONAUT"
  if (xp >= 2000) return "EXPLORER"
  return "NOVICE"
}

// --- Client Component ---
export function ProfileClient({ profileId }: { profileId: string }) {
  const router = useRouter();
  const { toast } = useToast();
  
  const [userData, setUserData] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState("published");
  const [editMode, setEditMode] = useState(false);
  
  // State for editable profile data - Initialize with default structure
  const [profileEditData, setProfileEditData] = useState({
    username: "", // Will be set from userData
    bio: "", 
    profileImage: "", 
    coverImage: "", 
  });
  
  const [selectedProfileFile, setSelectedProfileFile] = useState<File | null>(null);
  const [selectedCoverFile, setSelectedCoverFile] = useState<File | null>(null);
  const [isUploadingProfile, setIsUploadingProfile] = useState(false);
  const [isUploadingCover, setIsUploadingCover] = useState(false);
  const [isSavingProfile, setIsSavingProfile] = useState(false);
  const [profileEditError, setProfileEditError] = useState<string | null>(null);

  const profileFileInputRef = useRef<HTMLInputElement>(null);
  const coverFileInputRef = useRef<HTMLInputElement>(null);
  // Use userData directly for published articles unless deletion is needed client-side without refresh
  // const [publishedArticles, setPublishedArticles] = useState<Article[]>([]); 

  // --- Data Fetching ---
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      setUserData(null); // Clear previous data
      const currentUserIdStr = localStorage.getItem("userId"); 

      let actualProfileId = profileId;
      if (profileId === "me") {
        if (currentUserIdStr) {
          // If the current route is already the user's actual ID, no need to replace
          if (router && typeof router.replace === 'function' && `/profile/${currentUserIdStr}` !== window.location.pathname) {
             console.log("Redirecting from /profile/me to /profile/" + currentUserIdStr);
             router.replace(`/profile/${currentUserIdStr}`); 
          }
          actualProfileId = currentUserIdStr; 
        } else {
          setError("Please log in to view your profile.");
          setLoading(false);
          // Optionally redirect to login: router.push('/auth/signin');
          return; 
        }
      }

      const profileUserIdNum = Number(actualProfileId);
      if (isNaN(profileUserIdNum)) {
        setError("Invalid profile ID.");
        setLoading(false);
        return;
      }

      try {
        console.log(`Fetching data for profile ID: ${profileUserIdNum}`);
        // Use Promise.allSettled to handle potential errors in individual requests
        const results = await Promise.allSettled([
          axiosInstance.get<UserDTO>(`/users/${profileUserIdNum}`),
          axiosInstance.get<ArticleDTO[]>(`/articles/user/${profileUserIdNum}`),
          axiosInstance.get<ArticleDTO[]>(`/articles/votes/user/${profileUserIdNum}?voteType=UP`),
          axiosInstance.get<ArticleDTO[]>(`/articles/votes/user/${profileUserIdNum}?voteType=DOWN`)
        ]);

        // Check results
        const userResult = results[0];
        const publishedResult = results[1];
        const upvotedResult = results[2];
        const downvotedResult = results[3];

        if (userResult.status === 'rejected') {
            throw new Error(userResult.reason?.response?.data?.message || userResult.reason?.message || "Failed to load user data");
        }
        
        const userDto = userResult.value.data;
        const loggedInUserIdNum = currentUserIdStr ? Number(currentUserIdStr) : null;
        const isCurrentUser = loggedInUserIdNum === profileUserIdNum;

        // Handle potential errors for article lists gracefully
        const publishedArticlesData = publishedResult.status === 'fulfilled' ? publishedResult.value.data.map(transformArticleDTO) : [];
        const upvotedArticlesData = upvotedResult.status === 'fulfilled' ? upvotedResult.value.data.map(transformArticleDTO) : [];
        const downvotedArticlesData = downvotedResult.status === 'fulfilled' ? downvotedResult.value.data.map(transformArticleDTO) : [];

        if (publishedResult.status === 'rejected') console.error("Failed to fetch published articles:", publishedResult.reason);
        if (upvotedResult.status === 'rejected') console.error("Failed to fetch upvoted articles:", upvotedResult.reason);
        if (downvotedResult.status === 'rejected') console.error("Failed to fetch downvoted articles:", downvotedResult.reason);


        const fetchedUserData: UserData = {
          id: userDto.id,
          username: userDto.username,
          bio: userDto.bio,
          profileImage: userDto.profileImageUrl,
          coverImage: userDto.photoCoverUrl,
          xp: userDto.experiencePoints,
          joinDate: "2024-01-01T00:00:00Z", // Placeholder - fetch if available from UserDTO
          isCurrentUser: isCurrentUser,
          articles: publishedArticlesData,
          upvotedArticles: upvotedArticlesData,
          downvotedArticles: downvotedArticlesData,
        };

        setUserData(fetchedUserData);
        // setPublishedArticles(fetchedUserData.articles); // Not needed if using userData directly
        setProfileEditData({ // Initialize edit state *after* data is fetched
            username: fetchedUserData.username, 
            bio: fetchedUserData.bio ?? "",
            profileImage: fetchedUserData.profileImage ?? "",
            coverImage: fetchedUserData.coverImage ?? ""
        });

      } catch (err: any) {
        console.error("Failed to fetch profile data:", err);
        setError(err.message || "Failed to load profile.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [profileId]); // Depend only on profileId, router changes shouldn't trigger refetch directly

  // --- S3 Upload Logic --- 
  const uploadImageToS3 = async (file: File, type: 'profile' | 'cover'): Promise<string | null> => {
    if (type === 'profile') setIsUploadingProfile(true);
    if (type === 'cover') setIsUploadingCover(true);
    setProfileEditError(null); 
    console.log(`[S3 Upload] Starting ${type} image upload for file:`, file.name);
    try {
      // Expect an object like { uploadUrl: string, key: string }
      const response = await axiosInstance.get<{ uploadUrl: string; key: string }>("/generate-upload-url");
      const presignedUrlData = response.data;
      console.log(`[S3 Upload] Received pre-signed URL data for ${type}:`, presignedUrlData);

      if (!presignedUrlData || !presignedUrlData.uploadUrl) {
        throw new Error("Failed to get valid pre-signed URL.");
      }
      const actualUploadUrl = presignedUrlData.uploadUrl;

      await axios.put(actualUploadUrl, file, { headers: { "Content-Type": file.type } });
      console.log(`[S3 Upload] Successfully uploaded ${type} file to S3.`);

      // The final URL is the upload URL without query params
      const imageUrl = actualUploadUrl.split("?")[0]; 
      console.log(`[S3 Upload] Derived ${type} image URL:`, imageUrl);
      return imageUrl;
    } catch (error: any) {
      console.error(`[S3 Upload] Error during ${type} image upload:`, error);
      const errorMsg = `Image upload failed: ${error.message}. Check console.`;
      setProfileEditError(errorMsg); 
      toast({ variant: "destructive", title: "Upload Failed", description: errorMsg });
      return null;
    } finally {
       if (type === 'profile') setIsUploadingProfile(false);
       if (type === 'cover') setIsUploadingCover(false);
    }
  }

  // --- File Selection Handlers --- 
  const handleProfileFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedProfileFile(file);
      const reader = new FileReader();
      reader.onload = ev => setProfileEditData(prev => ({ ...prev, profileImage: ev.target?.result as string }));
      reader.readAsDataURL(file);
    }
  }
  const handleCoverFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedCoverFile(file);
      const reader = new FileReader();
      reader.onload = ev => setProfileEditData(prev => ({ ...prev, coverImage: ev.target?.result as string }));
      reader.readAsDataURL(file);
    }
  }

  // --- Profile Update --- 
  const handleProfileUpdate = async () => {
    if (!userData || !userData.isCurrentUser) return; 

    setIsSavingProfile(true);
    setProfileEditError(null);
    // Use profileEditData for current values, fallback to userData for original if needed
    let finalProfileImageUrl = profileEditData.profileImage; 
    let finalCoverImageUrl = profileEditData.coverImage;

    try {
      if (selectedProfileFile) {
        const uploadedUrl = await uploadImageToS3(selectedProfileFile, 'profile');
        if (!uploadedUrl) { setIsSavingProfile(false); return; } // Stop if upload failed
        finalProfileImageUrl = uploadedUrl;
        setSelectedProfileFile(null); 
      }
      if (selectedCoverFile) {
         const uploadedUrl = await uploadImageToS3(selectedCoverFile, 'cover');
         if (!uploadedUrl) { setIsSavingProfile(false); return; } // Stop if upload failed
         finalCoverImageUrl = uploadedUrl;
         setSelectedCoverFile(null);
      }

      // Prepare data for backend PUT request
      // Send S3 URLs if uploads occurred, otherwise send existing URLs (or null)
      const updateData = {
        bio: profileEditData.bio,
        profileImageUrl: finalProfileImageUrl.startsWith('data:') ? userData.profileImage : (finalProfileImageUrl || null),
        photoCoverUrl: finalCoverImageUrl.startsWith('data:') ? userData.coverImage : (finalCoverImageUrl || null),
      };

      console.log("Updating profile with data:", updateData);
      await axiosInstance.put(`/users/${userData.id}`, updateData);

      toast({ title: "Profile Updated", description: "Your profile has been saved successfully." });
      setEditMode(false); 
      // Update main userData state to reflect changes immediately
       setUserData(prev => prev ? ({
           ...prev,
           bio: updateData.bio,
           profileImage: updateData.profileImageUrl,
           coverImage: updateData.photoCoverUrl
       }) : null);
       // Update profileEditData to match the saved state
        setProfileEditData(prev => ({
           ...prev, 
           bio: updateData.bio,
           profileImage: updateData.profileImageUrl ?? "",
           coverImage: updateData.photoCoverUrl ?? ""
       }));

    } catch (error: any) {
      console.error("Error updating profile:", error);
      const errorMsg = `Profile update failed: ${error.message}. Check console.`;
      setProfileEditError(errorMsg);
      toast({ variant: "destructive", title: "Update Failed", description: errorMsg });
    } finally {
      setIsSavingProfile(false);
    }
  }

  // --- Article Deletion --- 
  const handleDeleteArticle = async (articleId: number) => {
    if (!userData || !userData.isCurrentUser) return;
    
    // Use userData.articles for the original list
    const originalArticles = userData.articles; 
    // Optimistically update the main userData state
    setUserData(prev => prev ? ({ ...prev, articles: prev.articles.filter(a => a.id !== articleId) }) : null);

    try {
      await axiosInstance.delete(`/articles/${articleId}/user/${userData.id}`); 
      toast({ title: "Article Deleted", description: "The article has been removed." });
    } catch (error: any) {
      console.error("Error deleting article:", error);
      toast({ variant: "destructive", title: "Deletion Failed", description: "Could not delete the article." });
      // Revert UI update on error
      setUserData(prev => prev ? ({ ...prev, articles: originalArticles }) : null); 
    }
  }

  // Function to close dialogs programmatically 
  const closeDialog = () => {
     document.querySelector<HTMLElement>('[role="dialog"][data-state="open"] [aria-label="Close"]')?.click();
     if (!document.querySelector('[role="dialog"][data-state="open"] [aria-label="Close"]')) {
        document.querySelector<HTMLElement>('[role="dialog"][data-state="open"]')?.parentElement?.click();
     }
  }

  // Reset edit form if editMode is cancelled 
   useEffect(() => {
     if (!editMode && userData) { 
       setProfileEditData({
         username: userData.username, 
         bio: userData.bio ?? "", 
         profileImage: userData.profileImage ?? "", 
         coverImage: userData.coverImage ?? "", 
       });
       setSelectedProfileFile(null); 
      setSelectedCoverFile(null);
      setProfileEditError(null); 
    }
  }, [editMode, userData]);

  // --- Render Logic ---
  if (loading) {
    return ( <div className="flex min-h-screen bg-black text-white items-center justify-center"><MinimalNavigation /><Loader2 className="h-12 w-12 animate-spin text-indigo-400" /><p className="ml-4">Loading Profile...</p></div> );
  }
  if (error) {
     return ( <div className="flex min-h-screen bg-black text-white items-center justify-center p-6"><MinimalNavigation /><div className="text-center"><p className="text-xl text-red-500">Error: {error}</p></div></div> );
  }
  if (!userData) {
     return ( <div className="flex min-h-screen bg-black text-white items-center justify-center"><MinimalNavigation /><p className="text-xl">Profile not found.</p></div> );
  }

  const badgeLevel = getBadgeLevel(userData.xp);

  return (
    <div className="flex min-h-screen bg-black text-white relative">
      <BloomingStars />
      <MinimalNavigation />
      <main className="flex-1 ml-12 transition-all duration-300 relative z-10">
        {/* Cover Image */}
        <div className="relative h-64 md:h-80 group">
          <Image src={editMode ? profileEditData.coverImage || "/placeholder.svg" : userData.coverImage || "/placeholder.svg"} alt="Cover" fill className="object-cover" priority />
          {userData.isCurrentUser && (
            <Dialog onOpenChange={(open) => { if (!open) setProfileEditError(null); }}>
              <DialogTrigger asChild>
                <Button variant="outline" size="icon" className="absolute top-4 right-4 bg-black/50 border-white/20 hover:bg-black/70 h-8 w-8 transition-all duration-300 hover:scale-110 opacity-0 group-hover:opacity-100" disabled={isUploadingCover || isSavingProfile}>
                  {isUploadingCover ? <Loader2 className="h-4 w-4 animate-spin" /> : <Camera className="h-4 w-4" />}
                </Button>
              </DialogTrigger>
              <DialogContent className="bg-gray-900 border-gray-800">
                <DialogHeader><DialogTitle>Update Cover Image</DialogTitle></DialogHeader>
                <div className="space-y-4 py-4">
                  {profileEditError && <p className="text-sm text-red-500">{profileEditError}</p>}
                  <div className="mb-2">
                    <label className="block text-xs text-gray-400 mb-1">Upload new image</label>
                    <input ref={coverFileInputRef} type="file" accept="image/*" onChange={handleCoverFileSelect} className="block w-full text-sm text-gray-300 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100" disabled={isUploadingCover || isSavingProfile} />
                  </div>
                   {profileEditData.coverImage && profileEditData.coverImage.startsWith('data:') && ( <div className="mt-2"><Image src={profileEditData.coverImage} alt="Local Preview" width={100} height={60} className="object-cover rounded" /></div> )}
                      <div className="flex justify-end">
                     <Button onClick={() => { setProfileEditData({ ...profileEditData, coverImage: userData.coverImage ?? "" }); setSelectedCoverFile(null); if(coverFileInputRef.current) coverFileInputRef.current.value = ""; setProfileEditError(null); closeDialog(); }} variant="outline" className="mr-2 border-gray-700 text-gray-300 hover:bg-gray-800" disabled={isUploadingCover || isSavingProfile}>Cancel</Button>
                    <Button onClick={handleProfileUpdate} className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600" disabled={isUploadingCover || isSavingProfile || (!selectedCoverFile && profileEditData.coverImage === (userData.coverImage ?? ""))}> 
                      {isUploadingCover || isSavingProfile ? <Loader2 className="mr-2 h-4 w-4 animate-spin"/> : null} Save Changes
                    </Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          )}
          {/* Profile picture */}
          <div className="absolute left-8 bottom-[-64px] md:bottom-[-80px] z-20 group">
            <div className="relative h-32 w-32 md:h-40 md:w-40 rounded-full overflow-hidden border-4 border-white bg-gray-900 shadow-lg">
              <Image src={editMode ? profileEditData.profileImage || "/placeholder.svg" : userData.profileImage || "/placeholder.svg"} alt={userData.username} width={160} height={160} className="object-cover"/>
              {userData.isCurrentUser && (
                <Dialog onOpenChange={(open) => { if (!open) setProfileEditError(null); }}>
                  <DialogTrigger asChild>
                    <Button variant="outline" size="icon" className="absolute bottom-2 right-2 bg-black/50 border-white/20 hover:bg-black/70 h-8 w-8 transition-all duration-300 hover:scale-110 opacity-0 group-hover:opacity-100" disabled={isUploadingProfile || isSavingProfile}>
                       {isUploadingProfile ? <Loader2 className="h-4 w-4 animate-spin" /> : <Camera className="h-4 w-4" />}
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="bg-gray-900 border-gray-800">
                    <DialogHeader><DialogTitle>Update Profile Picture</DialogTitle></DialogHeader>
                     <div className="space-y-4 py-4">
                       {profileEditError && <p className="text-sm text-red-500">{profileEditError}</p>}
                      <div className="mb-2">
                        <label className="block text-xs text-gray-400 mb-1">Upload new image</label>
                        <input ref={profileFileInputRef} type="file" accept="image/*" onChange={handleProfileFileSelect} className="block w-full text-sm text-gray-300 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100" disabled={isUploadingProfile || isSavingProfile} />
                      </div>
                      {profileEditData.profileImage && profileEditData.profileImage.startsWith('data:') && ( <div className="mt-2"><Image src={profileEditData.profileImage} alt="Local Preview" width={60} height={60} className="object-cover rounded-full" /></div> )}
                      <div className="flex justify-end">
                         <Button onClick={() => { setProfileEditData({ ...profileEditData, profileImage: userData.profileImage ?? "" }); setSelectedProfileFile(null); if(profileFileInputRef.current) profileFileInputRef.current.value = ""; setProfileEditError(null); closeDialog(); }} variant="outline" className="mr-2 border-gray-700 text-gray-300 hover:bg-gray-800" disabled={isUploadingProfile || isSavingProfile}>Cancel</Button>
                        <Button onClick={handleProfileUpdate} className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600" disabled={isUploadingProfile || isSavingProfile || (!selectedProfileFile && profileEditData.profileImage === (userData.profileImage ?? ""))}> 
                           {isUploadingProfile || isSavingProfile ? <Loader2 className="mr-2 h-4 w-4 animate-spin"/> : null} Save Changes
                         </Button>
                      </div>
                    </div>
                  </DialogContent>
                </Dialog>
              )}
            </div>
          </div>
        </div>

        {/* Profile Info */}
        <div className="pt-24 md:pt-28 px-8 pb-8">
          {profileEditError && !isUploadingCover && !isUploadingProfile && ( <div className="mb-4 p-3 bg-red-900/30 border border-red-700 text-red-300 rounded-md"><p>{profileEditError}</p></div> )}
          <div className="flex flex-col md:flex-row justify-between items-start md:items-end mb-6">
            <div className="mb-4 md:mb-0">
              {editMode ? ( <Input value={profileEditData.username} readOnly className="text-3xl font-bold mb-1 bg-gray-800/50 border-gray-700 w-full md:w-auto cursor-not-allowed"/> ) : ( <h1 className="text-3xl font-bold mb-1">{userData.username}</h1> )}
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
                    <Button onClick={handleProfileUpdate} className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600" disabled={isSavingProfile || isUploadingProfile || isUploadingCover}>
                       {isSavingProfile ? <Loader2 className="mr-2 h-4 w-4 animate-spin"/> : null} Save Profile
                    </Button>
                    <Button onClick={() => setEditMode(false)} variant="outline" className="border-gray-700 text-gray-300 hover:bg-gray-800" disabled={isSavingProfile || isUploadingProfile || isUploadingCover}>Cancel</Button>
                  </>
                ) : ( <Button onClick={() => setEditMode(true)} variant="outline" size="icon" className="border-gray-700 text-gray-300 hover:bg-gray-800"><Edit className="h-4 w-4" /></Button> )}
                <Link href="/article-edit"><Button variant="outline" size="icon" className="border-gray-700 text-gray-300 hover:bg-gray-800"><PenSquare className="h-4 w-4" /></Button></Link>
              </div>
            )}
          </div>

          {/* Bio Section */}
          <div className="mb-8">
            <h2 className="text-xl font-semibold mb-2">Bio</h2>
            {editMode ? (
                <Textarea value={profileEditData.bio} onChange={e => setProfileEditData({ ...profileEditData, bio: e.target.value })} className="bg-gray-800 border-gray-700 min-h-[100px]" placeholder="Tell us a bit about yourself..." disabled={isSavingProfile} />
              ) : ( <p className="text-gray-300 whitespace-pre-wrap">{userData.bio || "No bio provided."}</p> )}
            </div>

          {/* Tabs for Articles */}
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="grid w-full grid-cols-3 bg-gray-900 border border-gray-800 rounded-lg mb-6">
              <TabsTrigger value="published" className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-purple-600 data-[state=active]:to-blue-600 data-[state=active]:text-white rounded-md">Published Articles</TabsTrigger>
              <TabsTrigger value="upvoted" className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-purple-600 data-[state=active]:to-blue-600 data-[state=active]:text-white rounded-md">Upvoted</TabsTrigger>
              <TabsTrigger value="downvoted" className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-purple-600 data-[state=active]:to-blue-600 data-[state=active]:text-white rounded-md">Downvoted</TabsTrigger>
            </TabsList>

            <TabsContent value="published">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {userData.articles.length > 0 ? ( userData.articles.map((article: Article) => ( <ArticleCard key={article.id} article={article} /* Pass props needed by ArticleCard */ /> )) ) : ( <p className="text-gray-500 col-span-full text-center py-8">No articles published yet.</p> )}
              </div>
            </TabsContent>
            <TabsContent value="upvoted">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {userData.upvotedArticles.length > 0 ? ( userData.upvotedArticles.map((article: Article) => ( <ArticleCard key={article.id} article={article} /> )) ) : ( <p className="text-gray-500 col-span-full text-center py-8">No upvoted articles yet.</p> )}
              </div>
            </TabsContent>
            <TabsContent value="downvoted">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {userData.downvotedArticles.length > 0 ? ( userData.downvotedArticles.map((article: Article) => ( <ArticleCard key={article.id} article={article} /> )) ) : ( <p className="text-gray-500 col-span-full text-center py-8">No downvoted articles yet.</p> )}
              </div>
            </TabsContent>
          </Tabs>
        </div>
        <div className="h-16"></div> {/* Bottom margin */}
      </main>
    </div>
  )
}
