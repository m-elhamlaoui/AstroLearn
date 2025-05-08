"use client"

import { useState, useEffect } from "react" // Added useEffect
import axiosInstance from "@/lib/axiosInstance" // Added
import { MinimalNavigation } from "@/components/minimal-navigation"
import { CalendarView } from "@/components/calendar-view"
import { NextEventCard } from "@/components/next-event-card"
import { AnticipatedEventCard } from "@/components/anticipated-event-card"
import { CalendarSearchBar } from "@/components/calendar-search-bar"
import { Button } from "@/components/ui/button"
import { ChevronLeft, Search, Calendar as CalendarIcon } from "lucide-react"
import { BloomingStars } from "@/components/blooming-stars"

interface Event {
  id: number;
  title: string;
  description: string;
  date: string;
  agency: string;
  location: string;
  // importance: number; // Removed
  image: string;
  tags: string[];
}

// Backend DTO structure
interface SpaceMissionDTO {
  id: number; // Assuming Long maps to number in JS/TS
  name: string;
  agency: string;
  launchDate: string; // LocalDateTime will be string (ISO format)
  description: string;
  missionImage: string | null;
  liveStreamUrl: string | null;
  status: string; // Assuming SpaceMission.MissionStatus maps to string
  creatorUserId: number;
  creatorUsername: string | null;
}

// For paginated response
interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // Current page number
}


// Get the next closest event (first upcoming event)
const getNextClosestEvent = (events: Event[]): Event | undefined => {
  if (!events || events.length === 0) return undefined;
  const now = new Date()
  return events
    .filter((event) => new Date(event.date) > now)
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())[0]
}

// Get the most anticipated events (top 8 by importance, excluding the next closest)
// As 'importance' is not in DTO, we'll sort by date for now, or use a default importance.
const getMostAnticipatedEvents = (events: Event[], nextEventId: number | undefined, limit = 8): Event[] => {
  if (!events || events.length === 0) return [];
  const now = new Date()
  return events
    .filter((event) => new Date(event.date) > now && event.id !== nextEventId)
    // Sort by date as importance is not available from backend
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()) 
    .slice(0, limit)
}

type ViewMode = "year" | "month" | "day";

export default function MissionsPage() {
  const [allEvents, setAllEvents] = useState<Event[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  const [viewMode, setViewMode] = useState<ViewMode>("year")
  const [selectedMonth, setSelectedMonth] = useState<number | null>(null)
  const [isSearchOpen, setIsSearchOpen] = useState(false)

  useEffect(() => {
    const fetchMissions = async () => {
      setIsLoading(true)
      setError(null)
      try {
        // Fetching first page, 20 missions. Adjust size as needed.
        const response = await axiosInstance.get<Page<SpaceMissionDTO>>("/missions?page=0&size=20")
        const missionsData = response.data.content

        const mappedEvents: Event[] = missionsData.map((dto) => ({
          id: dto.id,
          title: dto.name,
          description: dto.description,
          date: dto.launchDate, // Assuming this is an ISO string
          agency: dto.agency,
          location: "Space Event", // Placeholder
          // importance: 50, // Removed
          image: dto.missionImage || "/placeholder.svg?height=300&width=500",
          // Basic tags from title, can be improved
          tags: dto.name.toLowerCase().split(" ").slice(0, 2),
        }))
        setAllEvents(mappedEvents)
      } catch (err) {
        console.error("Failed to fetch missions:", err)
        setError("Failed to load missions. Please try again later.")
      } finally {
        setIsLoading(false)
      }
    }
    fetchMissions()
  }, [])

  // Get next closest event
  const nextEvent = getNextClosestEvent(allEvents)

  // Get most anticipated events
  const anticipatedEvents = getMostAnticipatedEvents(allEvents, nextEvent?.id)

  // Handle month selection from year view
  const handleSelectMonth = (month: number) => {
    setSelectedMonth(month)
    setViewMode("day")
  }

  // Handle back navigation
  const handleBack = () => {
    if (viewMode === "day") {
      setViewMode("year")
      setSelectedMonth(null)
    } else if (viewMode === "month") {
      setViewMode("year")
    }
  }

  // Get the title for the current view
  const getViewTitle = () => {
    if (viewMode === "day" && selectedMonth) {
      return `${new Date(2024, selectedMonth - 1).toLocaleString("default", { month: "long" })} 2024`
    } else if (viewMode === "month") {
      return "Monthly Calendar"
    } else {
      return "Space Missions Calendar"
    }
  }

  return (
    <div className="flex min-h-screen bg-black text-white relative">
      {/* Blooming Stars Animation */}
      <BloomingStars />
      
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300 relative z-10">
        <div className="container mx-auto">
          {/* Header */}
          <div className="flex justify-between items-center mb-8">
            <div>
              {(viewMode === "day" || viewMode === "month") ? (
                <div className="flex items-center gap-2">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={handleBack}
                    className="text-gray-400 hover:text-white"
                  >
                    <ChevronLeft className="h-5 w-5" />
                    <span className="sr-only">Back</span>
                  </Button>
                  <h1 className="text-3xl font-bold">{getViewTitle()}</h1>
                </div>
              ) : (
                <h1 className="text-3xl font-bold">{getViewTitle()}</h1>
              )}
            </div>

            <div className="flex gap-2">
              {/* View Mode Selector */}
              {viewMode === "year" && (
                <Button
                  variant="outline"
                  size="icon"
                  onClick={() => setViewMode("month")}
                  className="h-10 w-10 rounded-full bg-gray-800/50 border-gray-700 hover:bg-indigo-900/50 hover:border-indigo-600 text-gray-300 hover:text-indigo-400 backdrop-blur-sm"
                >
                  <CalendarIcon className="h-5 w-5" />
                  <span className="sr-only">Monthly View</span>
                </Button>
              )}

              {/* Search Button */}
              <Button
                variant="outline"
                size="icon"
                onClick={() => setIsSearchOpen(true)}
                className="h-10 w-10 rounded-full bg-gray-800/50 border-gray-700 hover:bg-indigo-900/50 hover:border-indigo-600 text-gray-300 hover:text-indigo-400 backdrop-blur-sm"
              >
                <Search className="h-5 w-5" />
                <span className="sr-only">Search Missions</span>
              </Button>
            </div>
          </div>

          {/* Calendar View */}
          <div className="bg-transparent rounded-xl mb-10">
            <CalendarView
              events={allEvents}
              initialViewMode={viewMode}
              initialMonth={selectedMonth ?? undefined}
              onViewModeChange={setViewMode}
            />
          </div>

          {/* Next Closest Event */}
          {nextEvent && (
            <div className="mb-10">
              <h2 className="text-2xl font-bold mb-4">Next Mission</h2>
              <NextEventCard event={nextEvent} />
            </div>
          )}

          {/* Most Anticipated Events */}
          {anticipatedEvents.length > 0 && (
            <div className="mb-10">
              <h2 className="text-2xl font-bold mb-4">Most Anticipated Missions</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {anticipatedEvents.map((event) => (
                  <AnticipatedEventCard key={event.id} event={event} />
                ))}
              </div>
            </div>
          )}
        </div>
      </main>

      {/* Search Overlay */}
      {isSearchOpen && <CalendarSearchBar onClose={() => setIsSearchOpen(false)} events={allEvents} />}
    </div>
  )
}
