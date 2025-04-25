import { Badge } from "@/components/ui/badge"

interface UserBadgeProps {
  level: "NOVICE" | "EXPLORER" | "ASTRONAUT" | "GALACTIC"
}

export function UserBadge({ level }: UserBadgeProps) {
  // Define badge styles based on level
  const getBadgeStyles = () => {
    switch (level) {
      case "NOVICE":
        return "bg-gradient-to-r from-green-600 to-green-400 text-white border border-green-300/30"
      case "EXPLORER":
        return "bg-gradient-to-r from-blue-600 to-blue-400 text-white border border-blue-300/30"
      case "ASTRONAUT":
        return "bg-gradient-to-r from-purple-600 to-purple-400 text-white border border-purple-300/30"
      case "GALACTIC":
        return "bg-gradient-to-r from-pink-600 via-purple-500 to-indigo-600 text-white border border-indigo-300/30"
      default:
        return "bg-gray-600 text-white"
    }
  }

  // Define badge icons or emojis based on level
  const getBadgeIcon = () => {
    switch (level) {
      case "NOVICE":
        return "🌱"
      case "EXPLORER":
        return "🔭"
      case "ASTRONAUT":
        return "👨‍🚀"
      case "GALACTIC":
        return "✨"
      default:
        return ""
    }
  }

  // Define tooltip text based on level
  const getTooltipText = () => {
    switch (level) {
      case "NOVICE":
        return "Just starting your cosmic journey (0-1,999 XP)"
      case "EXPLORER":
        return "Venturing into the unknown (2,000-4,999 XP)"
      case "ASTRONAUT":
        return "Mastering the cosmos (5,000-9,999 XP)"
      case "GALACTIC":
        return "A true cosmic authority (10,000+ XP)"
      default:
        return ""
    }
  }

  return (
    <div className="group relative inline-block">
      <Badge
        className={`px-3 py-1 font-semibold ${getBadgeStyles()} shadow-lg hover:shadow-xl transition-all duration-300 animate-shimmer bg-[length:200%_100%]`}
      >
        {getBadgeIcon()} {level}
      </Badge>
      <div className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 py-2 bg-gray-900 text-white text-xs rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none whitespace-nowrap z-10 border border-gray-700 shadow-xl">
        {getTooltipText()}
      </div>
    </div>
  )
}
