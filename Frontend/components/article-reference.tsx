import { Card } from "@/components/ui/card"
import Link from "next/link"

interface ArticleReferenceProps {
  reference: {
    id: number
    title: string
    summary: string
  }
}

export function ArticleReference({ reference }: ArticleReferenceProps) {
  return (
    <Card className="bg-gray-800 border-gray-700 p-3 hover:bg-gray-750 transition-all">
      <Link href={`/articles/${reference.id}`} className="block">
        <h4 className="font-medium text-white mb-1 hover:text-indigo-400 transition-colors">{reference.title}</h4>
        <p className="text-xs text-gray-400 line-clamp-2">{reference.summary}</p>
      </Link>
    </Card>
  )
}
