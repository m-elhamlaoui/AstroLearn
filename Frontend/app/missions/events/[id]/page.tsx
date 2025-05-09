"use client"

"use client"

import { useState, useEffect, use } from "react" // Added useEffect, use
import axiosInstance from "@/lib/axiosInstance" // Added
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"; // Added Button import
import { Badge } from "@/components/ui/badge"
import { Calendar, Clock, MapPin, Users, ArrowLeft } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import { format, formatDistanceToNow, isValid } from "date-fns" // Added isValid

// Interface for the data structure expected by the page
interface PageEventData {
  id: number;
  title: string;
  description: string; // Short description
  longDescription: string;
  date: string; // ISO string
  agency: string;
  location: string;
  importance: number;
  image: string;
  tags: string[];
  relatedEvents: { id: number; title: string; date: string }[]; // Simplified for now
  liveStreamUrl?: string | null; // Added from DTO
  status?: string; // Added from DTO
}

// Backend DTO structure
interface SpaceMissionDTO {
  id: number;
  name: string;
  agency: string;
  launchDate: string; // LocalDateTime will be string (ISO format)
  description: string;
  missionImage: string | null;
  liveStreamUrl: string | null;
  status: string;
  // creatorUserId and creatorUsername are not directly used on this page
}

export default function EventPage({ params: paramsPromise }: { params: Promise<{ id: string }> }) {
  const params = use(paramsPromise);
  const [event, setEvent] = useState<PageEventData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!params.id) {
      setError("Mission ID is missing.");
      setIsLoading(false);
      return;
    }

    const fetchEventData = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await axiosInstance.get<SpaceMissionDTO>(`/missions/${params.id}`);
        const dto = response.data;

        // Map DTO to PageEventData
        const mappedEvent: PageEventData = {
          id: dto.id,
          title: dto.name,
          description: dto.description.substring(0, 150) + (dto.description.length > 150 ? "..." : ""), // Simple short desc
          longDescription: `<p>${dto.description.replace(/\n/g, "</p><p>")}</p>`, // Basic HTML formatting
          date: dto.launchDate,
          agency: dto.agency,
          location: "Space Event", // Placeholder
          importance: 75, // Default importance
          image: dto.missionImage || "/placeholder.svg?height=500&width=1000",
          tags: dto.name.toLowerCase().split(" ").slice(0, 3).filter(tag => tag.length > 2), // Simple tags
          relatedEvents: [], // Placeholder, as not directly available
          liveStreamUrl: dto.liveStreamUrl,
          status: dto.status,
        };
        setEvent(mappedEvent);
      } catch (err) {
        console.error("Failed to fetch event data:", err);
        setError("Failed to load mission details. Please try again later.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchEventData();
  }, [params.id]);

  if (isLoading) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p className="text-xl">Loading mission details...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center p-8 text-center">
        <div>
          <p className="text-xl text-red-500 mb-4">{error}</p>
          <Link href="/missions">
            <Button variant="outline">Back to Missions</Button>
          </Link>
        </div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p className="text-xl">Mission not found.</p>
      </div>
    );
  }

  // Format dates (moved here to ensure 'event' is not null)
  const eventDate = new Date(event.date);
  const isValidDate = isValid(eventDate);
  const formattedDate = isValidDate ? format(eventDate, "MMMM d, yyyy") : "Invalid Date";
  const formattedTime = isValidDate ? format(eventDate, "h:mm a") : "N/A";
  const timeUntil = isValidDate ? formatDistanceToNow(eventDate, { addSuffix: true }) : "N/A";
  // Removed duplicate declarations of formattedDate and formattedTime
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
            {event.status && <Badge variant="outline" className="mb-4 text-sm">{event.status}</Badge>}


            {/* Meta Information */}
            <div className="flex flex-wrap items-center gap-x-6 gap-y-2 mb-6">
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
          {/* Launch Countdown / Status */}
          <div className="mb-8 p-6 bg-gray-900 rounded-xl">
            <h2 className="text-xl font-bold mb-2">
              {isValidDate && eventDate > new Date() ? "Launching:" : "Launch Date:"}
            </h2>
            <p className="text-3xl font-bold text-indigo-400">
              {isValidDate && eventDate > new Date() ? timeUntil : formattedDate}
            </p>
            {event.liveStreamUrl && (
              <a 
                href={event.liveStreamUrl} 
                target="_blank" 
                rel="noopener noreferrer" 
                className="mt-3 inline-block text-indigo-400 hover:text-indigo-300 underline"
              >
                Watch Live Stream
              </a>
            )}
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
