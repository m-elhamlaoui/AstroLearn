"use client"

import { useState } from "react"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Badge } from "@/components/ui/badge"
import { Calendar, Clock, MapPin } from "lucide-react"
import { format } from "date-fns"
import Link from "next/link"

interface CalendarDayViewProps {
  month: number // 1-12
  events: any[]
}

export function CalendarDayView({ month, events }: CalendarDayViewProps) {
  const [hoveredDay, setHoveredDay] = useState<number | null>(null)

  const currentYear = new Date().getFullYear()
  const daysInMonth = new Date(currentYear, month, 0).getDate()

  // Get the first day of the month (0 = Sunday, 1 = Monday, etc.)
  const firstDayOfMonth = new Date(currentYear, month - 1, 1).getDay()

  // Group events by day
  const eventsByDay = Array.from({ length: daysInMonth }, (_, i) => {
    const day = i + 1
    const dayEvents = events.filter((event) => {
      const eventDate = new Date(event.date)
      return (
        eventDate.getDate() === day && eventDate.getMonth() + 1 === month && eventDate.getFullYear() === currentYear
      )
    })

    return {
      day,
      events: dayEvents,
      importance: dayEvents.length > 0 ? Math.max(...dayEvents.map((e) => e.importance)) : 0,
    }
  })

  // Create calendar grid with proper offset for first day of month
  // Ensure days start from the top left (day 1 is in the first row)
  const calendarDays = []

  // Add empty cells for days before the 1st of the month
  for (let i = 0; i < firstDayOfMonth; i++) {
    calendarDays.push(null)
  }

  // Add the actual days of the month
  for (let i = 0; i < daysInMonth; i++) {
    calendarDays.push(eventsByDay[i])
  }

  // Split into weeks
  const weeks = []
  for (let i = 0; i < calendarDays.length; i += 7) {
    weeks.push(calendarDays.slice(i, i + 7))
  }

  // Get color based on importance
  const getImportanceColor = (importance: number) => {
    if (importance >= 90) return "bg-red-500"
    if (importance >= 80) return "bg-orange-500"
    if (importance >= 70) return "bg-yellow-500"
    if (importance >= 60) return "bg-green-500"
    if (importance >= 50) return "bg-blue-500"
    if (importance > 0) return "bg-indigo-500"
    return "bg-gray-800"
  }

  const getImportanceHoverColor = (importance: number) => {
    if (importance >= 90) return "hover:bg-red-600"
    if (importance >= 80) return "hover:bg-orange-600"
    if (importance >= 70) return "hover:bg-yellow-600"
    if (importance >= 60) return "hover:bg-green-600"
    if (importance >= 50) return "hover:bg-blue-600"
    if (importance > 0) return "hover:bg-indigo-600"
    return "hover:bg-gray-700"
  }

  const dayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]

  return (
    <div>
      {/* Day names header */}
      <div className="grid grid-cols-7 mb-2">
        {dayNames.map((name) => (
          <div key={name} className="text-center text-gray-400 font-medium py-2">
            {name}
          </div>
        ))}
      </div>

      {/* Calendar grid */}
      {weeks.map((week, weekIndex) => (
        <div key={weekIndex} className="grid grid-cols-7 mb-2 gap-1">
          {week.map((day, dayIndex) => {
            if (day === null) {
              return <div key={`empty-${dayIndex}`} className="p-2 bg-gray-900/50 rounded-md" />
            }

            const hasEvents = day.events.length > 0
            const importanceColor = getImportanceColor(day.importance)
            const hoverColor = getImportanceHoverColor(day.importance)

            return (
              <div
                key={`day-${day.day}`}
                className="p-1"
                onMouseEnter={() => hasEvents && setHoveredDay(day.day)}
                onMouseLeave={() => setHoveredDay(null)}
              >
                {hasEvents ? (
                  <Popover open={hoveredDay === day.day}>
                    <PopoverTrigger asChild>
                      <Link
                        href={`/missions/events/${day.events[0].id}`}
                        className={`
                          block w-full h-full min-h-16 rounded-md p-2
                          ${importanceColor} ${hoverColor} transition-colors duration-200
                          flex flex-col items-center justify-center
                          transform hover:scale-105 shadow-md hover:shadow-lg
                        `}
                      >
                        <span className="font-bold text-lg">{day.day}</span>
                        {day.events.length > 1 && (
                          <Badge className="mt-1 bg-white/20">{day.events.length} events</Badge>
                        )}
                      </Link>
                    </PopoverTrigger>
                    <PopoverContent className="w-80 bg-gray-800 border-gray-700 text-white p-0 shadow-xl">
                      <div className="max-h-80 overflow-y-auto">
                        {day.events.map((event) => (
                          <Link
                            key={event.id}
                            href={`/missions/events/${event.id}`}
                            className="block p-4 hover:bg-gray-700 transition-colors border-b border-gray-700 last:border-0"
                          >
                            <h4 className="font-bold text-lg mb-1">{event.title}</h4>
                            <p className="text-gray-300 text-sm mb-2 line-clamp-2">{event.description}</p>
                            <div className="flex flex-wrap gap-2 text-xs text-gray-400">
                              <div className="flex items-center gap-1">
                                <Calendar className="h-3 w-3" />
                                {format(new Date(event.date), "MMM d, yyyy")}
                              </div>
                              <div className="flex items-center gap-1">
                                <Clock className="h-3 w-3" />
                                {format(new Date(event.date), "h:mm a")}
                              </div>
                              <div className="flex items-center gap-1">
                                <MapPin className="h-3 w-3" />
                                {event.location}
                              </div>
                            </div>
                          </Link>
                        ))}
                      </div>
                    </PopoverContent>
                  </Popover>
                ) : (
                  <div
                    className={`
                      w-full h-full min-h-16 rounded-md p-2 bg-gray-800/70
                      flex items-center justify-center
                      hover:bg-gray-700/70 transition-colors
                    `}
                  >
                    <span className="text-gray-500 font-medium">{day.day}</span>
                  </div>
                )}
              </div>
            )
          })}
        </div>
      ))}

      {/* Legend */}
      <div className="mt-6 flex flex-wrap gap-4 justify-center">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-full bg-red-500"></div>
          <span className="text-sm text-gray-300">Critical (90-100)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-full bg-orange-500"></div>
          <span className="text-sm text-gray-300">Major (80-89)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-full bg-yellow-500"></div>
          <span className="text-sm text-gray-300">Important (70-79)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-full bg-green-500"></div>
          <span className="text-sm text-gray-300">Moderate (60-69)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-full bg-blue-500"></div>
          <span className="text-sm text-gray-300">Minor (50-59)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-full bg-indigo-500"></div>
          <span className="text-sm text-gray-300">Low (1-49)</span>
        </div>
      </div>
    </div>
  )
}
