"use client"

import { MinimalNavigation } from "@/components/minimal-navigation"
import { Badge } from "@/components/ui/badge"
import { Calendar, Clock, MapPin, Users, ArrowLeft } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import { format, formatDistanceToNow } from "date-fns"

// This would be fetched from the backend in production
const getEventById = (id: string) => {
  // Sample event data
  return {
    id: Number.parseInt(id),
    title: "Artemis III Moon Landing",
    description: "NASA's mission to land the first woman and next man on the Moon's South Pole.",
    longDescription: `
      <p>The Artemis III mission represents humanity's return to the lunar surface after more than 50 years. As part of NASA's broader Artemis program, this mission will land the first woman and next man on the Moon, specifically targeting the South Pole region which has never been explored by humans before.</p>
      
      <h2>Mission Objectives</h2>
      
      <p>Artemis III has several key scientific and exploration objectives:</p>
      
      <ul>
        <li><strong>Lunar South Pole Exploration:</strong> Investigate a region of the Moon that contains permanently shadowed craters believed to contain water ice.</li>
        <li><strong>Sample Collection:</strong> Gather lunar samples from different geological formations to enhance our understanding of the Moon's history.</li>
        <li><strong>Technology Demonstration:</strong> Test new spacesuits, mobility systems, and other technologies needed for future lunar exploration.</li>
        <li><strong>Preparation for Mars:</strong> Develop and validate capabilities needed for eventual human missions to Mars.</li>
      </ul>
      
      <h2>Mission Architecture</h2>
      
      <p>The mission will utilize several key components:</p>
      
      <ul>
        <li><strong>Space Launch System (SLS):</strong> NASA's powerful rocket that will launch the Orion spacecraft.</li>
        <li><strong>Orion Spacecraft:</strong> The vehicle that will carry astronauts to lunar orbit and back to Earth.</li>
        <li><strong>Human Landing System (HLS):</strong> The lunar lander that will transport astronauts from lunar orbit to the surface and back.</li>
        <li><strong>Gateway (optional):</strong> A space station in lunar orbit that may support the mission.</li>
      </ul>
      
      <h2>Timeline and Surface Operations</h2>
      
      <p>Astronauts will spend approximately 6.5 days on the lunar surface, conducting:</p>
      
      <ul>
        <li>Up to four moonwalks (EVAs)</li>
        <li>Scientific experiments and sample collection</li>
        <li>Deployment of scientific instruments</li>
        <li>Testing of resource utilization technologies</li>
      </ul>
      
      <p>The Artemis III mission is a critical step in establishing a sustainable human presence on the Moon and will provide valuable experience for future missions to Mars and beyond.</p>
    `,
    date: "2025-12-15T00:00:00Z",
    agency: "NASA",
    location: "Moon",
    importance: 95, // 0-100 scale
    image: "/placeholder.svg?height=500&width=1000",
    tags: ["Moon", "NASA", "Artemis"],
    relatedEvents: [
      {
        id: 5,
        title: "Lunar Gateway First Module Launch",
        date: "2025-05-22T00:00:00Z",
      },
      {
        id: 10,
        title: "First Artemis Lunar Base Module",
        date: "2028-03-15T00:00:00Z",
      },
    ],
  }
}

export default function EventPage({ params }: { params: { id: string } }) {
  // In a real app, this would be a server component fetching data from the backend
  const event = getEventById(params.id)

  // Format dates
  const eventDate = new Date(event.date)
  const formattedDate = format(eventDate, "MMMM d, yyyy")
  const formattedTime = format(eventDate, "h:mm a")
  const timeUntil = formatDistanceToNow(eventDate, { addSuffix: false })

  // Get color based on importance
  const getImportanceColor = (importance: number) => {
    if (importance >= 90) return "bg-red-500"
    if (importance >= 80) return "bg-orange-500"
    if (importance >= 70) return "bg-yellow-500"
    if (importance >= 60) return "bg-green-500"
    if (importance >= 50) return "bg-blue-500"
    return "bg-indigo-500"
  }

  const importanceColor = getImportanceColor(event.importance)

  return (
    <div className="flex min-h-screen bg-black text-white">
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12">
        <div className="container mx-auto max-w-4xl">
          {/* Back Button */}
          <Link
            href="/missions"
            className="inline-flex items-center gap-2 text-gray-400 hover:text-white mb-6 transition-colors"
          >
            <ArrowLeft className="h-4 w-4" />
            Back to Calendar
          </Link>

          {/* Event Header */}
          <header className="mb-8">
            <h1 className="text-4xl font-bold mb-4">{event.title}</h1>

            {/* Meta Information */}
            <div className="flex flex-wrap items-center gap-6 mb-6">
              <div className="flex items-center gap-2 text-gray-300">
                <Calendar className="h-5 w-5" />
                <span>{formattedDate}</span>
              </div>

              <div className="flex items-center gap-2 text-gray-300">
                <Clock className="h-5 w-5" />
                <span>{formattedTime}</span>
              </div>

              <div className="flex items-center gap-2 text-gray-300">
                <MapPin className="h-5 w-5" />
                <span>{event.location}</span>
              </div>

              <div className="flex items-center gap-2 text-gray-300">
                <Users className="h-5 w-5" />
                <span>{event.agency}</span>
              </div>
            </div>

            {/* Tags */}
            <div className="flex flex-wrap gap-2 mb-6">
              {event.tags.map((tag) => (
                <Link
                  key={tag}
                  href={`/missions/tags/${tag.toLowerCase()}`}
                  className="text-sm px-3 py-1 bg-gray-800 text-white rounded-full hover:bg-gray-700 transition-colors"
                >
                  {tag}
                </Link>
              ))}

              <Badge className={`${importanceColor} ml-2`}>{event.importance}% Anticipated</Badge>
            </div>
          </header>

          {/* Featured Image */}
          <div className="relative h-80 md:h-96 mb-8 rounded-xl overflow-hidden">
            <Image src={event.image || "/placeholder.svg"} alt={event.title} fill className="object-cover" />
          </div>

          {/* Launch Countdown */}
          <div className="mb-8 p-6 bg-gray-900 rounded-xl">
            <h2 className="text-xl font-bold mb-2">Launching in:</h2>
            <p className="text-3xl font-bold text-indigo-400">{timeUntil}</p>
          </div>

          {/* Event Content */}
          <div className="mb-8">
            <div
              className="prose prose-invert prose-indigo max-w-none"
              dangerouslySetInnerHTML={{ __html: event.longDescription }}
            />
          </div>

          {/* Related Events */}
          {event.relatedEvents && event.relatedEvents.length > 0 && (
            <div className="mt-12">
              <h2 className="text-2xl font-bold mb-4">Related Missions</h2>
              <div className="space-y-4">
                {event.relatedEvents.map((relatedEvent) => (
                  <Link
                    key={relatedEvent.id}
                    href={`/missions/events/${relatedEvent.id}`}
                    className="block p-4 bg-gray-900 rounded-lg hover:bg-gray-800 transition-colors"
                  >
                    <h3 className="font-bold text-lg mb-1">{relatedEvent.title}</h3>
                    <div className="text-sm text-gray-400">{format(new Date(relatedEvent.date), "MMMM d, yyyy")}</div>
                  </Link>
                ))}
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  )
}
