"use client"

import { useState } from "react"
import { useRouter } from "next/navigation" // Not strictly needed here if not redirecting, but good for consistency
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select" // For role selection
import axiosInstance from "@/lib/axiosInstance"

interface SignUpModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onOpenSignIn: () => void // To switch to sign-in modal after successful sign-up
}

export function SignUpModal({ open, onOpenChange, onOpenSignIn }: SignUpModalProps) {
  const [username, setUsername] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("") // New state for confirm password
  const [role, setRole] = useState("USER") // Default role
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  // const router = useRouter(); // Not used directly for navigation in this version
  // const router = useRouter(); // Not used directly for navigation in this version

  const clearForm = () => {
    setUsername("")
    setEmail("")
    setPassword("")
    setConfirmPassword("")
    setRole("USER")
    setError(null)
  }

  const handleSignUp = async () => {
    setError(null)
    setSuccessMessage(null)
    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    try {
      const response = await axiosInstance.post("/auth/signup", {
        username,
        email,
        password,
        role,
      })

      if (response.data && response.data.message === "User registered successfully!") {
        setSuccessMessage("Sign up successful! Please sign in.")
        clearForm()
        // Optional: Automatically close this modal and open sign-in
        // onOpenChange(false);
        // onOpenSignIn();
      } else {
        setError(response.data.message || "Sign up failed. Please try again.")
      }
    } catch (err: any) {
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message)
      } else {
        setError("An unexpected error occurred. Please try again.")
      }
      console.error("Sign up error:", err)
    }
  }
  
  const handleOpenChangeAndClear = (isOpen: boolean) => {
    if (!isOpen) {
      clearForm()
      setSuccessMessage(null)
    }
    onOpenChange(isOpen)
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChangeAndClear}>
      <DialogContent className="sm:max-w-[425px] bg-gray-900 text-white border-gray-800">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Create an Account</DialogTitle>
          <DialogDescription className="text-gray-400">
            Fill in your details to join the AstroLearn community
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="username-signup">Username</Label>
            <Input
              id="username-signup"
              placeholder="Choose a username"
              className="bg-gray-800 border-gray-700 text-white"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="email-signup">Email</Label>
            <Input
              id="email-signup"
              type="email"
              placeholder="Enter your email"
              className="bg-gray-800 border-gray-700 text-white"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="password-signup">Password</Label>
            <Input
              id="password-signup"
              type="password"
              placeholder="Create a password"
              className="bg-gray-800 border-gray-700 text-white"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="confirm-password-signup">Confirm Password</Label>
            <Input
              id="confirm-password-signup"
              type="password"
              placeholder="Confirm your password"
              className="bg-gray-800 border-gray-700 text-white"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </div>
          
          {error && <p className="text-sm text-red-500">{error}</p>}
          {successMessage && <p className="text-sm text-green-500">{successMessage}</p>}
        </div>
        <Button
          onClick={handleSignUp}
          className="bg-indigo-600 hover:bg-indigo-700 text-white"
        >
          Sign Up
        </Button>
        <div className="text-center text-sm text-gray-400 mt-2">
            Already have an account?{" "}
            <button
              onClick={() => {
                onOpenChange(false); // Close this modal
                onOpenSignIn(); // Open sign-in modal
              }}
              className="text-indigo-400 hover:text-indigo-300 underline"
            >
              Sign in
            </button>
          </div>
      </DialogContent>
    </Dialog>
  )
}
