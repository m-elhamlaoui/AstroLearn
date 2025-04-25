import type React from "react"
export default function ArticlesLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return <div className="bg-black text-white">{children}</div>
}
