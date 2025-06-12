import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Calendar, MapPin } from "lucide-react"
import Image from "next/image"
import Link from "next/link"
import { format } from "date-fns"

interface AnticipatedEventCardProps {
  event: any
}

export function AnticipatedEventCard({ event }: AnticipatedEventCardProps) {
  const eventDate = new Date(event.date)

  // Removed getImportanceColor function and importanceColor variable

  return (
    <Card className="bg-gray-900 border-gray-700 overflow-hidden rounded-xl hover:shadow-lg hover:shadow-indigo-900/20 transition-all duration-300 h-full flex flex-col">
      {/* Event Image */}
      <div className="relative h-40">
        <Image src={event.image || "/placeholder.svg"} alt={event.title} fill className="object-cover" />
        {/* Removed Anticipated Badge */}
      </div>

      {/* Event Content */}
      <div className="p-4 flex-1 flex flex-col">
        <Link href={`/missions/events/${event.id}`} className="block flex-1">
          <h3 className="text-lg font-bold mb-2 hover:text-indigo-400 transition-colors">{event.title}</h3>
        </Link>

        <div className="flex flex-wrap gap-2 mb-2 text-xs text-gray-400">
          <div className="flex items-center gap-1">
            <Calendar className="h-3 w-3" />
            {format(eventDate, "MMM d, yyyy")}
          </div>
          <div className="flex items-center gap-1">
            <MapPin className="h-3 w-3" />
            {event.location}
          </div>
        </div>

        <p className="text-gray-400 text-sm mb-3 line-clamp-2">{event.description}</p>

        <div className="mt-auto">
          <Link
            href={`/missions/events/${event.id}`}
            className="text-sm text-indigo-400 hover:text-indigo-300 transition-colors"
          >
            View Details →
          </Link>
        </div>
      </div>
    </Card>
  )
}
