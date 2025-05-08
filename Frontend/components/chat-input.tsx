// components/chat-input.tsx
import { useState } from "react"
import { motion } from "framer-motion"
import { Send } from "lucide-react"

interface ChatInputProps {
  onSendMessage: (message: string) => void
  isLoading: boolean
}

export function ChatInput({ onSendMessage, isLoading }: ChatInputProps) {
  const [message, setMessage] = useState("")

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (message.trim() && !isLoading) {
      onSendMessage(message)
      setMessage("")
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.2 }}
    >
      <form 
        onSubmit={handleSubmit} 
        className="relative group"
      >
        <div className="relative">
          <input
            type="text"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="Ask about space exploration..."
            className="w-full bg-gray-900/30 backdrop-blur-lg text-white px-6 py-4 rounded-full border border-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all pr-16 text-lg placeholder:text-gray-500 shadow-lg"
            disabled={isLoading}
          />
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            type="submit"
            disabled={isLoading}
            className="absolute right-3 top-1/2 transform -translate-y-1/2 bg-gradient-to-r from-blue-600 to-purple-600 text-white p-2.5 rounded-full disabled:opacity-50 transition-all"
          >
            <Send size={20} />
          </motion.button>
        </div>
        
        {/* Optional floating hint - uncomment if you want this feature */}
        {/* <div className="absolute -bottom-6 left-2 text-xs text-gray-500 opacity-0 group-hover:opacity-100 transition-opacity">
          Press Enter to send
        </div> */}
      </form>
    </motion.div>
  )
}