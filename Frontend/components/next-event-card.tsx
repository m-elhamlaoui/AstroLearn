import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Calendar, Clock, MapPin, Users } from "lucide-react"
import Image from "next/image"
import Link from "next/link"
import { format, formatDistanceToNow } from "date-fns"

interface NextEventCardProps {
  event: any
}

export function NextEventCard({ event }: NextEventCardProps) {
  const eventDate = new Date(event.date)
  const timeUntil = formatDistanceToNow(eventDate, { addSuffix: false })

  return (
    <Card className="bg-gray-900 border-gray-700 overflow-hidden rounded-xl hover:shadow-lg hover:shadow-indigo-900/20 transition-all duration-300">
      <div className="md:flex">
        {/* Event Image */}
        <div className="md:w-1/3 relative h-60 md:h-auto">
          <Image src={event.image || "/placeholder.svg"} alt={event.title} fill className="object-cover" />
          <div className="absolute top-4 left-4">
            <Badge className="bg-red-500 hover:bg-red-600 text-white px-3 py-1">Next Mission</Badge>
          </div>
        </div>

        {/* Event Content */}
        <div className="p-6 md:w-2/3">
          <Link href={`/missions/events/${event.id}`}>
            <h3 className="text-2xl font-bold mb-2 hover:text-indigo-400 transition-colors">{event.title}</h3>
          </Link>

          <div className="flex flex-wrap gap-3 mb-4 text-sm text-gray-400">
            <div className="flex items-center gap-1">
              <Calendar className="h-4 w-4" />
              {format(eventDate, "MMMM d, yyyy")}
            </div>
            <div className="flex items-center gap-1">
              <Clock className="h-4 w-4" />
              {format(eventDate, "h:mm a")}
            </div>
            <div className="flex items-center gap-1">
              <MapPin className="h-4 w-4" />
              {event.location}
            </div>
            <div className="flex items-center gap-1">
              <Users className="h-4 w-4" />
              {event.agency}
            </div>
          </div>

          <p className="text-gray-300 mb-6">{event.description}</p>

          <div className="flex flex-wrap gap-2 mb-4">
            {event.tags.map((tag) => (
              <Badge key={tag} className="bg-gray-700 hover:bg-gray-600">
                {tag}
              </Badge>
            ))}
          </div>

          <div className="mt-4">
            <div className="text-sm text-gray-400 mb-1">Launching in:</div>
            <div className="text-xl font-bold text-indigo-400">{timeUntil}</div>
          </div>

          <Link
            href={`/missions/events/${event.id}`}
            className="mt-4 inline-block px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md transition-colors"
          >
            View Details
          </Link>
        </div>
      </div>
    </Card>
  )
}
