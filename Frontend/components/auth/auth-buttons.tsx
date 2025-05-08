"use client"

import { useState } from "react"
import { SignInModal } from "./sign-in-modal"
import { SignUpModal } from "./sign-up-modal"

export function AuthButtons() {
  const [isSignUpOpen, setIsSignUpOpen] = useState(false)

  const handleOpenSignUp = () => {
    setIsSignUpOpen(true)
  }

  return (
    <>
      <SignInModal onOpenSignUp={handleOpenSignUp} />
      <SignUpModal open={isSignUpOpen} onOpenChange={setIsSignUpOpen} />
    </>
  )
} 