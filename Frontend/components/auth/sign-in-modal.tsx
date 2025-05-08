"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import axiosInstance from "@/lib/axiosInstance" // Import axios instance

interface SignInModalProps {
  onOpenSignUp: () => void
}

export function SignInModal({ onOpenSignUp }: SignInModalProps) {
  const [open, setOpen] = useState(false)
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState<string | null>(null)
  const router = useRouter()

  const handleOpenSignUp = () => {
    setOpen(false)
    setError(null) // Clear error when switching modals
    onOpenSignUp()
  }

  const handleSignIn = async () => {
    setError(null) // Clear previous errors
    try {
      const response = await axiosInstance.post("/auth/signin", {
        email,
        password,
      })

      if (response.data && response.data.token) {
        localStorage.setItem("authToken", response.data.token)
        localStorage.setItem("userId", response.data.id) // Store user ID if needed
        localStorage.setItem("userRoles", JSON.stringify(response.data.roles)) // Store user roles
        router.push("/articles")
        setOpen(false) // Close modal on success
      } else {
        setError("Sign in failed. Please check your credentials.")
      }
    } catch (err: any) {
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message)
      } else {
        setError("An unexpected error occurred. Please try again.")
      }
      console.error("Sign in error:", err)
    }
  }

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { setOpen(isOpen); if (!isOpen) setError(null); }}>
      <DialogTrigger asChild>
        <Button className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-full">
          Sign In
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px] bg-gray-900 text-white border-gray-800">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Sign In</DialogTitle>
          <DialogDescription className="text-gray-400">
            Enter your credentials to access your account
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              placeholder="Enter your email"
              className="bg-gray-800 border-gray-700 text-white"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              placeholder="Enter your password"
              className="bg-gray-800 border-gray-700 text-white"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          {error && <p className="text-sm text-red-500">{error}</p>}
        </div>
        <div className="flex flex-col gap-4">
          <Button
            onClick={handleSignIn}
            className="bg-indigo-600 hover:bg-indigo-700 text-white"
          >
            Sign In
          </Button>
          <div className="text-center text-sm text-gray-400">
            Don't have an account?{" "}
            <button
              onClick={handleOpenSignUp}
              className="text-indigo-400 hover:text-indigo-300 underline"
            >
              Sign up
            </button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
