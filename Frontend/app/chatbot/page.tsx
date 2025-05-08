"use client"

import { useState, useRef, useEffect } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { ChatMessage } from "@/components/chat-message"
import { ChatInput } from "@/components/chat-input"
import { ArticleReference } from "@/components/article-reference"
import { motion, AnimatePresence } from "framer-motion"
import { BloomingStars } from "@/components/blooming-stars"
import { sendChatMessage } from "@/lib/gemini-service"

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
      role: "user",
      content:
        "You are a helpful assistant for answering questions about space exploration. You can provide information about space missions, space science, or any other related topics.",
    },
  ])
  const [references, setReferences] = useState<Reference[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [initialView, setInitialView] = useState(true)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  // Scroll to bottom of messages when new messages are added
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages])

  // Handle sending a new message
  const handleSendMessage = async (content: string) => {
    if (!content.trim()) return

    // Switch to conversation layout on first message
    if (initialView) {
      setInitialView(false)
    }

    const newUserMessage: Message = { role: "user", content }
    setMessages((prev) => [...prev, newUserMessage])

    setIsLoading(true)

    try {
      const chatHistory = messages.map(msg => ({
        role: msg.role,
        content: msg.content,
      }))

      const { message: botResponse, references: newReferences } = await sendChatMessage(content, chatHistory)
      
      const assistantMessage: Message = { 
        role: "assistant", 
        content: typeof botResponse === 'string' ? botResponse : botResponse.content
      }

      setMessages((prev) => [...prev, assistantMessage])
      setReferences(newReferences || [])
    } catch (error) {
      console.error("Error sending message:", error)
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: "Sorry, I encountered an error. Please try again." },
      ])
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen bg-black text-white relative overflow-hidden">
      <BloomingStars />
      <MinimalNavigation />

      <AnimatePresence mode="wait">
        {initialView ? (
          <motion.div 
            key="initial-view"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0, y: 50, transition: { duration: 0.4 } }}
            className="flex-1 flex flex-col items-center justify-center p-6 relative z-10"
          >
            <motion.div 
              initial={{ y: -20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: -50, opacity: 0 }}
              transition={{ duration: 0.6 }}
              className="text-center mb-16"
            >
              <h1 className="text-5xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-purple-600 mb-4">
                Space Exploration Chatbot
              </h1>
              <p className="text-gray-400 text-xl max-w-lg mx-auto">
                Ask questions about astronomy, space missions, or any cosmic curiosities
              </p>
            </motion.div>
            
            <motion.div 
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: 100, opacity: 0 }}
              transition={{ duration: 0.6 }}
              className="w-full max-w-2xl"
            >
              <ChatInput onSendMessage={handleSendMessage} isLoading={isLoading} />
            </motion.div>
          </motion.div>
        ) : (
          <motion.main 
            key="conversation-view"
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
            className="flex-1 p-6 ml-12 transition-all duration-300 flex flex-col relative z-10"
          >
            <div className="container mx-auto max-w-5xl flex-1 flex flex-col">
              <motion.div 
                initial={{ opacity: 0, y: -20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6 }}
                className="mb-6"
              >
                <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-purple-600">
                  Space Exploration Chatbot
                </h1>
                <p className="text-gray-400 mt-1 text-base">
                  Ask questions about astronomy, space missions, or any cosmic curiosities
                </p>
              </motion.div>

              <div className="flex-1 flex gap-8">
                <div className="flex-1 flex flex-col">
                  <div className="flex-1 rounded-xl custom-scrollbar overflow-y-auto max-h-[calc(100vh-300px)] px-2">
                    <div className="space-y-8">
                      {messages.slice(1).map((message, index) => (
                        <motion.div
                          key={`${message.role}-${index}`}
                          initial={{ opacity: 0, y: 20 }}
                          animate={{ opacity: 1, y: 0 }}
                          transition={{ duration: 0.4 }}
                        >
                          <ChatMessage message={message} />
                        </motion.div>
                      ))}
                      {isLoading && (
                        <motion.div
                          initial={{ opacity: 0 }}
                          animate={{ opacity: 1 }}
                          className="flex items-center gap-3 text-gray-300"
                        >
                          <div className="w-10 h-10 rounded-full bg-gray-800/60 backdrop-blur-md flex items-center justify-center">
                            <ThinkingAnimation />
                          </div>
                          <div className="text-lg font-medium">AstroLearn is thinking...</div>
                        </motion.div>
                      )}
                      <div ref={messagesEndRef} />
                    </div>
                  </div>
                  
                  <div className="mt-8 mb-4">
                    <ChatInput onSendMessage={handleSendMessage} isLoading={isLoading} />
                  </div>
                </div>

                {references.length > 0 && (
                  <motion.div
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.5 }}
                    className="w-96 bg-gray-900/30 backdrop-blur-md rounded-2xl p-6 h-[calc(100vh-280px)] custom-scrollbar overflow-y-auto hidden lg:block border border-gray-800"
                  >
                    <h3 className="text-xl font-bold mb-6 text-white">Related Articles</h3>
                    <div className="space-y-4">
                      {references.map((reference) => (
                        <ArticleReference key={reference.id} reference={reference} />
                      ))}
                    </div>
                  </motion.div>
                )}
              </div>
            </div>
          </motion.main>
        )}
      </AnimatePresence>
    </div>
  )
}

// Custom thinking animation component
const ThinkingAnimation = () => {
  return (
    <div className="flex space-x-1">
      {[0, 1, 2].map((dot) => (
        <motion.div
          key={dot}
          className="w-1.5 h-1.5 bg-blue-400 rounded-full"
          initial={{ opacity: 0.3 }}
          animate={{
            opacity: [0.3, 1, 0.3],
            y: [0, -4, 0],
          }}
          transition={{
            duration: 1.5,
            repeat: Infinity,
            delay: dot * 0.2,
            ease: "easeInOut",
          }}
        />
      ))}
    </div>
  )
}
