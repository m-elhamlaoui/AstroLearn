"use client"

import { useState } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { CalendarMonthView } from "@/components/calendar-month-view"
import { CalendarDayView } from "@/components/calendar-day-view"
import { CalendarYearView } from "@/components/calendar-year-view"
import { NextEventCard } from "@/components/next-event-card"
import { AnticipatedEventCard } from "@/components/anticipated-event-card"
import { CalendarSearchBar } from "@/components/calendar-search-bar"
import { Button } from "@/components/ui/button"
import { ChevronLeft, Search, Calendar as CalendarIcon } from "lucide-react"

// Sample data - would be fetched from backend in production
const sampleEvents = [
  {
    id: 1,
    title: "Artemis III Moon Landing",
    description: "NASA's mission to land the first woman and next man on the Moon's South Pole.",
    date: "2025-12-15T00:00:00Z",
    agency: "NASA",
    location: "Moon",
    importance: 95, // 0-100 scale
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Moon", "NASA", "Artemis"],
  },
  {
    id: 2,
    title: "Europa Clipper Launch",
    description: "Mission to conduct detailed reconnaissance of Jupiter's moon Europa.",
    date: "2024-10-10T00:00:00Z",
    agency: "NASA",
    location: "Jupiter's Moon Europa",
    importance: 85,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Europa", "NASA", "Jupiter"],
  },
  {
    id: 3,
    title: "SpaceX Starship Mars Cargo Mission",
    description: "First uncrewed cargo mission to Mars using Starship.",
    date: "2024-09-20T00:00:00Z",
    agency: "SpaceX",
    location: "Mars",
    importance: 90,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Mars", "SpaceX", "Starship"],
  },
  {
    id: 4,
    title: "James Webb Space Telescope Deep Field Observation",
    description: "Unprecedented deep field observation of the earliest galaxies.",
    date: "2024-07-15T00:00:00Z",
    agency: "NASA/ESA",
    location: "L2 Point",
    importance: 80,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["JWST", "Astronomy", "Deep Space"],
  },
  {
    id: 5,
    title: "Lunar Gateway First Module Launch",
    description: "Launch of the Power and Propulsion Element for the Lunar Gateway station.",
    date: "2025-05-22T00:00:00Z",
    agency: "NASA/International Partners",
    location: "Lunar Orbit",
    importance: 88,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Moon", "Gateway", "NASA"],
  },
  {
    id: 6,
    title: "DAVINCI Mission to Venus",
    description: "Deep Atmosphere Venus Investigation of Noble gases, Chemistry, and Imaging mission launch.",
    date: "2029-06-30T00:00:00Z",
    agency: "NASA",
    location: "Venus",
    importance: 75,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Venus", "NASA", "Atmosphere"],
  },
  {
    id: 7,
    title: "Dragonfly Launch to Titan",
    description: "Launch of the Dragonfly rotorcraft to explore Saturn's moon Titan.",
    date: "2027-07-01T00:00:00Z",
    agency: "NASA",
    location: "Titan",
    importance: 82,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Titan", "NASA", "Dragonfly"],
  },
  {
    id: 8,
    title: "ESA ExoMars Rover Launch",
    description: "Launch of the Rosalind Franklin rover to search for signs of past life on Mars.",
    date: "2028-09-10T00:00:00Z",
    agency: "ESA/Roscosmos",
    location: "Mars",
    importance: 78,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Mars", "ESA", "Rover"],
  },
  {
    id: 9,
    title: "NASA VIPER Moon Rover",
    description: "Volatiles Investigating Polar Exploration Rover to search for water on the Moon.",
    date: "2024-11-22T00:00:00Z",
    agency: "NASA",
    location: "Moon",
    importance: 76,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Moon", "NASA", "Rover"],
  },
  {
    id: 10,
    title: "First Artemis Lunar Base Module",
    description: "Delivery of the first habitat module for the Artemis Base Camp.",
    date: "2028-03-15T00:00:00Z",
    agency: "NASA/International Partners",
    location: "Moon",
    importance: 92,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Moon", "NASA", "Habitat"],
  },
  {
    id: 11,
    title: "OSIRIS-APEX Asteroid Apophis Encounter",
    description:
      "OSIRIS-REx spacecraft (renamed OSIRIS-APEX) will study asteroid Apophis during its close Earth approach.",
    date: "2029-04-13T00:00:00Z",
    agency: "NASA",
    location: "Asteroid Apophis",
    importance: 70,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["Asteroid", "NASA", "OSIRIS"],
  },
  {
    id: 12,
    title: "New Horizons Reaches 100 AU",
    description: "New Horizons spacecraft reaches 100 astronomical units from the Sun.",
    date: "2029-12-25T00:00:00Z",
    agency: "NASA",
    location: "Outer Solar System",
    importance: 65,
    image: "/placeholder.svg?height=300&width=500",
    tags: ["New Horizons", "NASA", "Outer Solar System"],
  },
]

// Get the next closest event (first upcoming event)
const getNextClosestEvent = (events) => {
  const now = new Date()
  return events
    .filter((event) => new Date(event.date) > now)
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())[0]
}

// Get the most anticipated events (top 8 by importance, excluding the next closest)
const getMostAnticipatedEvents = (events, nextEventId, limit = 8) => {
  const now = new Date()
  return events
    .filter((event) => new Date(event.date) > now && event.id !== nextEventId)
    .sort((a, b) => b.importance - a.importance)
    .slice(0, limit)
}

export default function MissionsPage() {
  const [viewMode, setViewMode] = useState("year") // "year", "month", or "day"
  const [selectedMonth, setSelectedMonth] = useState(null)
  const [isSearchOpen, setIsSearchOpen] = useState(false)

  // Get next closest event
  const nextEvent = getNextClosestEvent(sampleEvents)

  // Get most anticipated events
  const anticipatedEvents = getMostAnticipatedEvents(sampleEvents, nextEvent?.id)

  // Handle month selection from year view
  const handleSelectMonth = (month) => {
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
    <div className="flex min-h-screen bg-black text-white">
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300">
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
                  onClick={() => setViewMode("month")}
                  className="border-gray-700 text-gray-300 hover:bg-gray-800"
                >
                  <CalendarIcon className="h-5 w-5 mr-2" />
                  Monthly View
                </Button>
              )}

              {/* Search Button */}
              <Button
                variant="outline"
                onClick={() => setIsSearchOpen(true)}
                className="border-gray-700 text-gray-300 hover:bg-gray-800"
              >
                <Search className="h-5 w-5 mr-2" />
                Search Missions
              </Button>
            </div>
          </div>

          {/* Calendar View */}
          <div className="bg-gray-900 rounded-xl p-6 mb-10">
            {viewMode === "day" && selectedMonth ? (
              <CalendarDayView month={selectedMonth} events={sampleEvents} />
            ) : viewMode === "month" ? (
              <CalendarMonthView events={sampleEvents} onSelectMonth={handleSelectMonth} />
            ) : (
              <CalendarYearView events={sampleEvents} onSelectMonth={handleSelectMonth} />
            )}
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
      {isSearchOpen && <CalendarSearchBar onClose={() => setIsSearchOpen(false)} events={sampleEvents} />}
    </div>
  )
}
