"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import Image from "next/image"
import axiosInstance from "@/lib/axiosInstance"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { toast } from "react-hot-toast"

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

  // Fetch verification requests
  useEffect(() => {
    const fetchVerificationRequests = async () => {
      try {
        setLoading(true)
        // Fetch users with PENDING verification status
        const response = await axiosInstance.get("/users?verificationStatus=PENDING")
        setRequests(response.data)
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
      await axiosInstance.put(`/users/${userId}/verification`, {
        verificationStatus: "VERIFIED"
      })
      
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
      await axiosInstance.put(`/users/${userId}/verification`, {
        verificationStatus: "UNVERIFIED"
      })
      
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
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="w-16 h-16 border-4 border-t-indigo-500 border-r-transparent border-b-indigo-500 border-l-transparent rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="text-center py-10">
        <h2 className="text-2xl font-bold text-red-500">Error</h2>
        <p className="mt-2">{error}</p>
        <button 
          onClick={() => window.location.reload()}
          className="mt-4 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 rounded-md text-white"
        >
          Retry
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Verification Requests</h1>
        <p className="text-gray-400 mt-2">Manage user verification requests</p>
      </div>

      {requests.length === 0 ? (
        <Card className="bg-gray-800 border-gray-700">
          <CardContent className="p-6 text-center">
            <p className="text-lg text-gray-300">No pending verification requests</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {requests.map((user) => (
            <Card key={user.id} className="bg-gray-800 border-gray-700 overflow-hidden">
              <div className="flex flex-col md:flex-row">
                <div className="md:w-1/4 p-6 flex flex-col items-center justify-center border-b md:border-b-0 md:border-r border-gray-700">
                  <div className="relative w-24 h-24 rounded-full overflow-hidden mb-4">
                    {user.profileImageUrl ? (
                      <Image 
                        src={user.profileImageUrl} 
                        alt={user.username || "User"} 
                        fill 
                        className="object-cover"
                      />
                    ) : (
                      <div className="w-full h-full bg-gray-700 flex items-center justify-center text-2xl text-white">
                        {user.username?.[0]?.toUpperCase() || "U"}
                      </div>
                    )}
                  </div>
                  <h3 className="text-lg font-semibold text-white">{user.username}</h3>
                  <p className="text-sm text-gray-400">{user.email}</p>
                  <div className="mt-2 flex items-center space-x-2">
                    <Badge className="bg-indigo-600">{user.level}</Badge>
                    <Badge className="bg-gray-700">{user.articleCount} articles</Badge>
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
                    <Button variant="outline" className="w-full">
                      View Profile
                    </Button>
                  </Link>
                  <Button 
                    className="w-full bg-green-600 hover:bg-green-700"
                    onClick={() => handleApproveVerification(user.id)}
                  >
                    Approve
                  </Button>
                  <Button 
                    className="w-full bg-red-600 hover:bg-red-700"
                    onClick={() => handleDeclineVerification(user.id)}
                  >
                    Decline
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
