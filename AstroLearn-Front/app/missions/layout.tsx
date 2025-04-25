import type React from "react"
export default function MissionsLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return <div className="bg-black text-white">{children}</div>
}
