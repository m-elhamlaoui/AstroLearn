"use client"

import { useState, useRef, useEffect } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { ChatMessage } from "@/components/chat-message"
import { ChatInput } from "@/components/chat-input"
import { ArticleReference } from "@/components/article-reference"
import { motion } from "framer-motion"

// Types for our chat interface
interface Message {
  role: "user" | "assistant" | "system"
  content: string
}

interface Reference {
  id: number
  title: string
  summary: string
}

export default function ChatbotPage() {
  const [messages, setMessages] = useState<Message[]>([
    {
      role: "assistant",
      content: "Hello! I'm AstroLearn's AI assistant. How can I help you with space exploration today?",
    },
  ])
  const [references, setReferences] = useState<Reference[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  // Scroll to bottom of messages when new messages are added
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages])

  // Handle sending a new message
  const handleSendMessage = async (content: string) => {
    if (!content.trim()) return

    // Add user message to chat
    const newUserMessage: Message = { role: "user", content }
    setMessages((prev) => [...prev, newUserMessage])

    // Show loading state
    setIsLoading(true)

    try {
      // This is where you would call the botServer function
      // const chatHistory = messages.concat(newUserMessage);
      // const { message, references } = await botServer(chatHistory);

      // For now, we'll just echo the message after a short delay
      setTimeout(() => {
        // Mock response
        const botResponse: Message = {
          role: "assistant",
          content: `You said: "${content}"`,
        }

        // Mock references
        const mockReferences: Reference[] = [
          {
            id: 1,
            title: "Understanding Space Exploration",
            summary: "An overview of current space exploration initiatives and technologies.",
          },
          {
            id: 2,
            title: "The Future of Mars Missions",
            summary: "Detailed analysis of upcoming Mars missions and their scientific objectives.",
          },
        ]

        setMessages((prev) => [...prev, botResponse])
        setReferences(mockReferences)
        setIsLoading(false)
      }, 1000)
    } catch (error) {
      console.error("Error in chat:", error)

      // Add error message
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: "Sorry, I encountered an error. Please try again." },
      ])
      setIsLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen bg-black text-white">
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300 flex flex-col">
        <div className="container mx-auto max-w-4xl flex-1 flex flex-col">
          {/* Header */}
          <div className="mb-6">
            <h1 className="text-3xl font-bold">Space Exploration Chatbot</h1>
            <p className="text-gray-400 mt-2">
              Ask questions about astronomy, space missions, or any cosmic curiosities
            </p>
          </div>

          {/* Chat Container */}
          <div className="flex-1 flex gap-6">
            {/* Messages Area */}
            <div className="flex-1 flex flex-col">
              <div className="flex-1 bg-gray-900 rounded-xl p-4 mb-4 overflow-y-auto max-h-[calc(100vh-280px)]">
                <div className="space-y-4">
                  {messages.map((message, index) => (
                    <ChatMessage key={index} message={message} />
                  ))}
                  {isLoading && (
                    <motion.div
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                      className="flex items-center gap-2 text-gray-400"
                    >
                      <div className="w-8 h-8 rounded-full bg-gray-800 flex items-center justify-center">
                        <div className="dot-typing"></div>
                      </div>
                      <div>AstroLearn is thinking...</div>
                    </motion.div>
                  )}
                  <div ref={messagesEndRef} />
                </div>
              </div>

              {/* Input Area */}
              <ChatInput onSendMessage={handleSendMessage} isLoading={isLoading} />
            </div>

            {/* References Panel */}
            {references.length > 0 && (
              <div className="w-80 bg-gray-900 rounded-xl p-4 h-[calc(100vh-280px)] overflow-y-auto hidden lg:block">
                <h3 className="text-lg font-bold mb-4 text-white">Related Articles</h3>
                <div className="space-y-3">
                  {references.map((reference) => (
                    <ArticleReference key={reference.id} reference={reference} />
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}
