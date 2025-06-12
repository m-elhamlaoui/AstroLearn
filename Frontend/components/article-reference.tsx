// components/article-reference.tsx
import { motion } from "framer-motion"
import { FileText } from "lucide-react"

interface ArticleReferenceProps {
  reference: {
    id: number
    title: string
    summary: string
  }
}

export function ArticleReference({ reference }: ArticleReferenceProps) {
  return (
    <motion.div
      whileHover={{ scale: 1.02 }}
      className="p-4 rounded-xl bg-gray-800/40 border border-gray-700 hover:border-gray-600 cursor-pointer transition-all group"
    >
      <div className="flex items-start gap-3">
        <div className="w-9 h-9 rounded-lg bg-blue-500/20 flex items-center justify-center flex-shrink-0">
          <FileText size={18} className="text-blue-400" />
        </div>
        <div>
          <h4 className="font-semibold text-white group-hover:text-blue-300 transition-colors">
            {reference.title}
          </h4>
          <p className="text-gray-400 text-sm mt-1 line-clamp-2">
            {reference.summary}
          </p>
        </div>
      </div>
    </motion.div>
  )
}