"use client"

import { motion } from "framer-motion"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { formatMessageContent } from "@/lib/message-formatting"
import { JSX } from "react"

interface Message {
  role: "user" | "assistant" | "system"
  content: string
}

interface ChatMessageProps {
  message: Message
}

export function ChatMessage({ message }: ChatMessageProps) {
  const { role, content } = message;
  const isUser = role === "user";
  const isSystem = role === "system";
  
  let contentToRender: string | JSX.Element | JSX.Element[];
  
  if (role === "assistant") {
    // Assistant messages get the special formatting
    contentToRender = formatMessageContent(content);
  } else if (role === "user") {
    // User messages are displayed as plain text, but handle line breaks
    contentToRender = content;
  } else { // System message
    contentToRender = content;
  }

  // Define bubble styles with modern floating aesthetic
  let bubbleClasses = "py-4 px-5 rounded-2xl shadow-lg text-lg backdrop-blur-md"; 
  let avatarFallbackText = "AI";
  let avatarSrc = "/placeholder.svg?height=32&width=32"; // Default AI avatar
  let avatarAlt = "AstroLearn AI";
  
  if (isUser) {
    bubbleClasses += " bg-gradient-to-r from-blue-600/80 to-blue-700/80 text-white border border-blue-500/30";
    avatarFallbackText = "You";
  } else if (isSystem) {
    bubbleClasses += " bg-gray-700/50 text-gray-300 italic border border-gray-700";
    avatarFallbackText = "Sys";
  } else { // Assistant
    bubbleClasses += " bg-gray-900/50 text-gray-100 border border-gray-800";
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4 }}
      className={`flex mb-8 ${isUser ? "justify-end" : "justify-start"}`}
    >
      <div className={`flex gap-3 max-w-[85%] w-fit ${isUser ? "flex-row-reverse" : ""}`}>
        {!isSystem ? (
          <Avatar className={`h-10 w-10 self-end ${isUser ? 
            "bg-blue-600 shadow-lg shadow-blue-500/20" : 
            "bg-gradient-to-br from-purple-500 to-blue-600 shadow-lg shadow-purple-500/20"}`}>
            {!isUser && <AvatarImage src={avatarSrc} alt={avatarAlt} />}
            <AvatarFallback className="text-white">{avatarFallbackText}</AvatarFallback>
          </Avatar>
        ) : (
          <div className="w-10 h-10 flex-shrink-0"></div>
        )}
        
        <div className={bubbleClasses}>
          {role === "assistant" ? (
            contentToRender
          ) : (
            <p className="whitespace-pre-wrap">{contentToRender}</p>
          )}
        </div>
      </div>
    </motion.div>
  )
}