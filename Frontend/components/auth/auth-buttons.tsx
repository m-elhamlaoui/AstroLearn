"use client"

import { useState } from "react"
import { SignInModal } from "./sign-in-modal"
import { SignUpModal } from "./sign-up-modal"

export function AuthButtons() {
  const [isSignInOpen, setIsSignInOpen] = useState(false) // SignInModal manages its own open state via DialogTrigger
  const [isSignUpOpen, setIsSignUpOpen] = useState(false)

  const openSignInModal = () => {
    // SignInModal's open state is primarily controlled by its DialogTrigger.
    // To programmatically open it if needed, one might need to pass `open` and `onOpenChange` to SignInModal too.
    // For now, SignInModal is triggered by its own button.
    // This function is for SignUpModal to call.
    setIsSignUpOpen(false)
    // setIsSignInOpen(true); // If SignInModal was controlled like SignUpModal
  }

  const openSignUpModal = () => {
    // This will be called by SignInModal
    // setIsSignInOpen(false); // If SignInModal was controlled
    setIsSignUpOpen(true)
  }

  // SignInModal already has its own DialogTrigger, so it controls its own visibility initially.
  // We only need to provide a way for it to open the SignUpModal.
  // And SignUpModal needs a way to open SignInModal (which is usually just closing itself, and user clicks SignIn again, or we can enhance)

  return (
    <>
      {/* SignInModal's onOpenSignUp will call openSignUpModal */}
      <SignInModal onOpenSignUp={openSignUpModal} />
      
      {/* SignUpModal's onOpenSignIn will call openSignInModal */}
      {/* It also needs its open state and onOpenChange managed */}
      <SignUpModal 
        open={isSignUpOpen} 
        onOpenChange={setIsSignUpOpen} 
        onOpenSignIn={openSignInModal} 
      />
    </>
  )
}
