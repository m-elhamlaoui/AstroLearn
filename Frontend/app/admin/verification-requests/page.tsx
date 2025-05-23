"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import Image from "next/image"
import axiosInstance from "@/lib/axiosInstance"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { toast } from "react-hot-toast"
import { CheckCircle, XCircle, User } from "lucide-react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { BloomingStars } from "@/components/blooming-stars"
import { useAuthRedirect } from "@/lib/useAuthRedirect"

interface UserVerificationRequest {
  id: number
  username: string
  email: string
  profileImageUrl: string
  bio: string
  verificationStatus: string
  articleCount: number
  level: string
}

export default function VerificationRequestsPage() {
  const [requests, setRequests] = useState<UserVerificationRequest[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const { isLoading: authLoading } = useAuthRedirect()

  // Fetch verification requests
  useEffect(() => {
    const fetchVerificationRequests = async () => {
      try {
        setLoading(true)
        // Fetch users with PENDING verification status
        const response = await axiosInstance.get("/users?verificationStatus=PENDING")
        const data = response.data
        console.log("Verification requests data:", data)
        
        // Handle paginated response
        if (data && data.content && Array.isArray(data.content)) {
          // Filter users with PENDING verification status
          const pendingUsers = data.content.filter((user: any) => 
            user && user.verificationStatus === "PENDING"
          )
          console.log("Pending verification users:", pendingUsers)
          setRequests(pendingUsers)
        } else if (Array.isArray(data)) {
          // If it's already an array, filter for PENDING status
          const pendingUsers = data.filter((user: any) => 
            user && user.verificationStatus === "PENDING"
          )
          setRequests(pendingUsers)
        } else {
          console.error("Unexpected data format for verification requests:", data)
          setRequests([])
        }
      } catch (err) {
        console.error("Error fetching verification requests:", err)
        setError("Failed to load verification requests")
      } finally {
        setLoading(false)
      }
    }

    fetchVerificationRequests()
  }, [])

  // Handle verification approval
  const handleApproveVerification = async (userId: number) => {
    try {
      // Updated URL to match backend controller endpoint
      await axiosInstance.put(`/users/verification/approve/${userId}`)
      
      // Update the local state
      setRequests(prevRequests => 
        prevRequests.filter(request => request.id !== userId)
      )
      
      toast.success("User verification approved successfully")
    } catch (err) {
      console.error("Error approving verification:", err)
      toast.error("Failed to approve verification")
    }
  }

  // Handle verification decline
  const handleDeclineVerification = async (userId: number) => {
    try {
      // Updated URL to match backend controller endpoint
      await axiosInstance.put(`/users/verification/reject/${userId}`)
      // Note: This endpoint supports an optional 'reason' parameter if needed
      
      // Update the local state
      setRequests(prevRequests => 
        prevRequests.filter(request => request.id !== userId)
      )
      
      toast.success("User verification declined")
    } catch (err) {
      console.error("Error declining verification:", err)
      toast.error("Failed to decline verification")
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-screen bg-black text-white items-center justify-center">
        <MinimalNavigation />
        <p className="text-xl">Loading verification requests...</p>
        <div className="ml-4 w-16 h-16 border-4 border-t-indigo-500 border-r-transparent border-b-indigo-500 border-l-transparent rounded-full animate-spin"></div>
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

  return (
    <div className="flex min-h-screen bg-black text-white relative">
      {/* Blooming Stars Animation */}
      <BloomingStars />
      
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300 relative z-10">
        <div className="container mx-auto space-y-8">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Verification Requests</h1>
            <p className="text-gray-400 mt-2">Manage user verification requests</p>
          </div>

          {requests.length === 0 ? (
            <Card className="bg-gray-800/70 border-gray-700 backdrop-blur-sm hover:bg-gray-800/90 transition-colors">
              <CardContent className="p-6 text-center">
                <p className="text-lg text-gray-300">No pending verification requests</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {requests.map((user) => (
                <Card key={user.id} className="bg-gray-800/70 border-gray-700 backdrop-blur-sm hover:bg-gray-800/90 transition-colors overflow-hidden">
                  <div className="flex flex-col md:flex-row">
                    <div className="md:w-1/4 p-6 flex flex-col items-center justify-center border-b md:border-b-0 md:border-r border-gray-700">
                      <div className="relative w-24 h-24 rounded-full overflow-hidden mb-4 bg-gray-700/80">
                        {user.profileImageUrl ? (
                          <Image 
                            src={user.profileImageUrl} 
                            alt={user.username || "User"} 
                            fill 
                            className="object-cover"
                          />
                        ) : (
                          <div className="w-full h-full bg-gray-700/80 flex items-center justify-center text-2xl text-white">
                            {user.username?.[0]?.toUpperCase() || "U"}
                          </div>
                        )}
                      </div>
                      <h3 className="text-lg font-semibold text-white">{user.username}</h3>
                      <p className="text-sm text-gray-400">{user.email}</p>
                      <div className="mt-2 flex items-center space-x-2">
                        <Badge className="bg-indigo-600/90">{user.level}</Badge>
                        <Badge className="bg-gray-700/90">{user.articleCount} articles</Badge>
                      </div>
                    </div>
                    
                    <div className="md:w-2/4 p-6 border-b md:border-b-0 md:border-r border-gray-700">
                      <h4 className="font-semibold text-gray-200 mb-2">Bio</h4>
                      <p className="text-gray-300 text-sm">
                        {user.bio || "No bio provided"}
                      </p>
                    </div>
                    
                    <div className="md:w-1/4 p-6 flex flex-col justify-center space-y-4">
                      <Link href={`/profile/${user.id}`} passHref>
                        <Button variant="outline" className="w-full border-gray-700 text-gray-300 hover:bg-gray-800 hover:text-white">
                          View Profile
                        </Button>
                      </Link>
                      <Button 
                        className="w-full bg-indigo-600 hover:bg-indigo-700 text-white flex items-center justify-center gap-2"
                        onClick={() => handleApproveVerification(user.id)}
                      >
                        <CheckCircle size={16} />
                        <span>Approve</span>
                      </Button>
                      <Button 
                        className="w-full bg-rose-700 hover:bg-rose-800 text-white flex items-center justify-center gap-2"
                        onClick={() => handleDeclineVerification(user.id)}
                      >
                        <XCircle size={16} />
                        <span>Decline</span>
                      </Button>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  )
}
