"use client"

import { useState } from "react"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { Button } from "@/components/ui/button"

interface Event {
  id: string | number
  title: string
  date: string | Date
  importance: number
}

interface CalendarYearViewProps {
  events: Event[]
  onSelectMonth: (month: number) => void
  year?: number
}

export function CalendarYearView({ events, onSelectMonth, year = new Date().getFullYear() }: CalendarYearViewProps) {
  const [currentYear, setCurrentYear] = useState(year)

  const goToPreviousYear = () => {
    setCurrentYear(currentYear - 1)
  }

  const goToNextYear = () => {
    setCurrentYear(currentYear + 1)
  }

  // Month names
  const monthNames = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ]

  // Group events by month
  const getMonthEvents = (month: number) => {
    return events.filter((event) => {
      const eventDate = event.date instanceof Date ? event.date : new Date(event.date)
      return eventDate.getMonth() === month && eventDate.getFullYear() === currentYear
    })
  }

  // Get color based on importance and event count
  const getMonthColor = (events: Event[]) => {
    if (events.length === 0) return "bg-gray-800"
    
    // Find highest importance event
    const maxImportance = Math.max(...events.map(e => e.importance))
    
    if (maxImportance >= 90) return "bg-red-500"
    if (maxImportance >= 80) return "bg-orange-500"
    if (maxImportance >= 70) return "bg-yellow-500"
    if (maxImportance >= 60) return "bg-green-500"
    if (maxImportance >= 50) return "bg-blue-500"
    return "bg-indigo-500"
  }

  // Get hover color based on importance
  const getMonthHoverColor = (events: Event[]) => {
    if (events.length === 0) return "hover:bg-gray-700"
    
    // Find highest importance event
    const maxImportance = Math.max(...events.map(e => e.importance))
    
    if (maxImportance >= 90) return "hover:bg-red-600"
    if (maxImportance >= 80) return "hover:bg-orange-600"
    if (maxImportance >= 70) return "hover:bg-yellow-600"
    if (maxImportance >= 60) return "hover:bg-green-600"
    if (maxImportance >= 50) return "hover:bg-blue-600"
    return "hover:bg-indigo-600"
  }

  return (
    <div className="bg-gray-900 rounded-xl overflow-hidden shadow-lg">
      {/* Year header with smooth gradient background */}
      <div className="bg-gradient-to-r from-blue-900/50 to-purple-900/50 p-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold text-white">{currentYear} Overview</h2>
          <div className="flex space-x-2">
            <Button variant="ghost" size="icon" onClick={goToPreviousYear} className="text-white hover:bg-white/10">
              <ChevronLeft className="h-5 w-5" />
            </Button>
            <Button variant="ghost" size="icon" onClick={goToNextYear} className="text-white hover:bg-white/10">
              <ChevronRight className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>

      {/* Calendar grid with smooth styling */}
      <div className="p-4 bg-gradient-to-b from-gray-900 to-gray-950">
        <div className="grid grid-cols-3 md:grid-cols-4 gap-4">
          {monthNames.map((month, index) => {
            const monthEvents = getMonthEvents(index)
            const hasEvents = monthEvents.length > 0
            const monthColor = getMonthColor(monthEvents)
            const hoverColor = getMonthHoverColor(monthEvents)

            return (
              <button
                key={month}
                onClick={() => onSelectMonth(index + 1)}
                className={`
                  relative p-4 rounded-lg transition-all duration-300
                  ${monthColor} ${hoverColor}
                  flex flex-col items-center justify-center
                  h-24 md:h-32
                `}
              >
                <span className="text-white font-medium mb-1">{month}</span>
                {hasEvents && (
                  <div className="text-white/90 text-sm font-bold">
                    {monthEvents.length} {monthEvents.length === 1 ? "Event" : "Events"}
                  </div>
                )}
              </button>
            )
          })}
        </div>
      </div>
    </div>
  )
}