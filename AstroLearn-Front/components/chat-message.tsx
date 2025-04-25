"use client"

import { motion } from "framer-motion"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"

interface ChatMessageProps {
  message: {
    role: "user" | "assistant" | "system"
    content: string
  }
}

export function ChatMessage({ message }: ChatMessageProps) {
  const isUser = message.role === "user"

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className={`flex ${isUser ? "justify-end" : "justify-start"}`}
    >
      <div className={`flex gap-3 max-w-[80%] ${isUser ? "flex-row-reverse" : ""}`}>
        {/* Avatar */}
        <Avatar className={`h-8 w-8 ${isUser ? "bg-white text-black" : "bg-indigo-600"}`}>
          {!isUser && <AvatarImage src="/placeholder.svg?height=32&width=32" alt="AstroLearn AI" />}
          <AvatarFallback>{isUser ? "You" : "AI"}</AvatarFallback>
        </Avatar>

        {/* Message Bubble */}
        <div className={`py-2 px-4 rounded-2xl ${isUser ? "bg-white text-black" : "bg-gray-800 text-white"} shadow-sm`}>
          <p className="text-sm whitespace-pre-wrap">{message.content}</p>
        </div>
      </div>
    </motion.div>
  )
}
