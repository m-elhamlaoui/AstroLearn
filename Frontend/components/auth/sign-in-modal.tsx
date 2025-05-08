"use client"

import { useState } from "react"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

interface SignInModalProps {
  onOpenSignUp: () => void
}

export function SignInModal({ onOpenSignUp }: SignInModalProps) {
  const [open, setOpen] = useState(false)

  const handleOpenSignUp = () => {
    setOpen(false)
    onOpenSignUp()
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
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
            <Label htmlFor="email">Username or Email</Label>
            <Input
              id="email"
              placeholder="Enter your username or email"
              className="bg-gray-800 border-gray-700"
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              placeholder="Enter your password"
              className="bg-gray-800 border-gray-700"
            />
          </div>
        </div>
        <div className="flex flex-col gap-4">
          <Button className="bg-indigo-600 hover:bg-indigo-700 text-white">
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