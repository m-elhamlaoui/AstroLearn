import type React from "react"
export default function ChatbotLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return <div className="bg-black text-white">{children}</div>
}
