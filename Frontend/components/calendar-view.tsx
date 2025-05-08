"use client"

import { useState, useEffect } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon, Clock, MapPin } from "lucide-react"
import { format } from "date-fns"
import { Button } from "@/components/ui/button"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Badge } from "@/components/ui/badge"
import Link from "next/link"

interface Event {
  id: string | number
  title: string
  date: string | Date
  description?: string
  location?: string
  agency?: string
  importance: number
  tags?: string[]
}

interface CalendarViewProps {
  events: Event[]
  initialViewMode?: "year" | "month" | "day"
  initialMonth?: number
  onViewModeChange?: (mode: "year" | "month" | "day") => void
}

export function CalendarView({ events, initialViewMode = "year", initialMonth, onViewModeChange }: CalendarViewProps) {
  const [viewMode, setViewMode] = useState(initialViewMode)
  const [currentDate, setCurrentDate] = useState(new Date())
  const [selectedMonth, setSelectedMonth] = useState(initialMonth || currentDate.getMonth() + 1)
  const [hoveredDay, setHoveredDay] = useState<number | null>(null)

  useEffect(() => {
    if (onViewModeChange) {
      onViewModeChange(viewMode)
    }
  }, [viewMode, onViewModeChange])

  const navigateDate = (direction: "prev" | "next") => {
    setCurrentDate(prev => {
      if (viewMode === "year") {
        return new Date(prev.getFullYear() + (direction === "next" ? 1 : -1), prev.getMonth())
      } else {
        return new Date(prev.getFullYear(), prev.getMonth() + (direction === "next" ? 1 : -1))
      }
    })
  }

  const handleMonthSelect = (month: number) => {
    setSelectedMonth(month)
    setCurrentDate(new Date(currentDate.getFullYear(), month - 1))
    setViewMode("month")
  }

  const toggleView = () => {
    setViewMode(viewMode === "year" ? "month" : "year")
  }

  const getEventsByDate = (date: Date) => {
    return events.filter(event => {
      const eventDate = new Date(event.date)
      return (
        eventDate.getDate() === date.getDate() &&
        eventDate.getMonth() === date.getMonth() &&
        eventDate.getFullYear() === date.getFullYear()
      )
    })
  }

  const getImportanceStyle = (importance: number) => {
    if (importance === 0) return { gradient: "", border: "" }
    
    const styles = {
      90: { gradient: "from-red-500/20 to-red-600/40", border: "border-red-500/50" },
      80: { gradient: "from-orange-500/20 to-orange-600/40", border: "border-orange-500/50" },
      70: { gradient: "from-yellow-500/20 to-yellow-600/40", border: "border-yellow-500/50" },
      60: { gradient: "from-green-500/20 to-green-600/40", border: "border-green-500/50" },
      50: { gradient: "from-blue-500/20 to-blue-600/40", border: "border-blue-500/50" }
    }

    for (const threshold of Object.keys(styles).map(Number).sort((a, b) => b - a)) {
      if (importance >= threshold) return styles[threshold]
    }

    return { gradient: "from-indigo-500/20 to-indigo-600/40", border: "border-indigo-500/50" }
  }

  const renderYearView = () => {
    const monthAbbrs = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"]
    
    return (
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -20 }}
        className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4"
      >
        {monthAbbrs.map((month, index) => {
          const monthEvents = events.filter(event => {
            const eventDate = new Date(event.date)
            return eventDate.getMonth() === index && eventDate.getFullYear() === currentDate.getFullYear()
          })
          
          const maxImportance = monthEvents.length > 0
            ? Math.max(...monthEvents.map(e => e.importance))
            : 0
          
          const { gradient, border } = getImportanceStyle(maxImportance)

          return (
            <motion.button
              key={month}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => handleMonthSelect(index + 1)}
              className={`
                p-4 rounded-xl border backdrop-blur-sm
                ${gradient ? `bg-gradient-to-br ${gradient}` : 'bg-gray-800/40'}
                ${border ? border : 'border-gray-700/50'}
                transition-all duration-200
              `}
            >
              <div className="text-lg font-bold mb-2">{month}</div>
              <div className="text-sm text-gray-400">{monthEvents.length} events</div>
            </motion.button>
          )
        })}
      </motion.div>
    )
  }

  const renderMonthView = () => {
    const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate()
    const firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1).getDay()
    const dayNames = ["S", "M", "T", "W", "T", "F", "S"]

    const days = Array.from({ length: daysInMonth }, (_, i) => i + 1)
    const paddingDays = Array.from({ length: firstDayOfMonth }, (_, i) => null)
    const allDays = [...paddingDays, ...days]

    return (
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -20 }}
        className="w-full max-w-[1024px] mx-auto"
      >
        <div className="grid grid-cols-7 gap-1 mb-2">
          {dayNames.map(day => (
            <div key={day} className="text-center text-sm font-medium text-gray-400">{day}</div>
          ))}
        </div>
        <div className="grid grid-cols-7 gap-1">
          {allDays.map((day, index) => {
            if (!day) return <div key={`empty-${index}`} className="aspect-square" />

            const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), day)
            const dayEvents = getEventsByDate(date)
            const maxImportance = dayEvents.length > 0
              ? Math.max(...dayEvents.map(e => e.importance))
              : 0
            
            const { gradient, border } = getImportanceStyle(maxImportance)

            return (
              <Popover key={day}>
                <PopoverTrigger asChild>
                  <motion.button
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    className={`
                      aspect-square p-2 rounded-lg border relative
                      ${gradient ? `bg-gradient-to-br ${gradient}` : 'bg-gray-800/40'}
                      ${border ? border : 'border-gray-700/50'}
                      transition-all duration-200
                    `}
                  >
                    <div className="text-sm font-medium">{day}</div>
                    {dayEvents.length > 0 && (
                      <div className="absolute bottom-1 right-1 w-2 h-2 rounded-full bg-white/80" />
                    )}
                  </motion.button>
                </PopoverTrigger>
                {dayEvents.length > 0 && (
                  <PopoverContent className="w-80 p-0 bg-gray-900/95 border-gray-700 backdrop-blur-lg">
                    <div className="p-4 space-y-3">
                      {dayEvents.map(event => (
                        <Link
                          key={event.id}
                          href={`/missions/events/${event.id}`}
                          className="block hover:bg-white/5 rounded-lg p-2 transition-colors"
                        >
                          <div className="font-medium text-white">{event.title}</div>
                          <div className="text-sm text-gray-400 mt-1 flex items-center gap-2">
                            <Clock className="w-4 h-4" />
                            {format(new Date(event.date), "HH:mm")}
                            {event.location && (
                              <>
                                <span className="text-gray-600">•</span>
                                <MapPin className="w-4 h-4" />
                                {event.location}
                              </>
                            )}
                          </div>
                          {event.tags && (
                            <div className="flex gap-1 mt-2">
                              {event.tags.map(tag => (
                                <Badge key={tag} variant="secondary" className="bg-white/10">
                                  {tag}
                                </Badge>
                              ))}
                            </div>
                          )}
                        </Link>
                      ))}
                    </div>
                  </PopoverContent>
                )}
              </Popover>
            )
          })}
        </div>
      </motion.div>
    )
  }

  return (
    <div className="w-full max-w-7xl mx-auto px-4 py-8">
      <div className="flex flex-col items-center justify-center mb-8">
        <div className="flex flex-col items-center justify-center gap-2">
          <div className="text-lg font-medium mb-2">
            {viewMode === "year" 
              ? `${currentDate.getFullYear()}` 
              : `${format(currentDate, 'MMMM yyyy')}`
            }
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="icon"
              onClick={() => navigateDate("prev")}
              className="rounded-full bg-gray-800/50 border-gray-700 hover:bg-indigo-900/50 hover:border-indigo-600"
            >
              <ChevronLeft className="h-5 w-5" />
            </Button>
            <Button
              variant="outline"
              size="icon"
              onClick={toggleView}
              className="rounded-full bg-gray-800/50 border-gray-700 hover:bg-indigo-900/50 hover:border-indigo-600"
            >
              <CalendarIcon className="h-5 w-5" />
            </Button>
            <Button
              variant="outline"
              size="icon"
              onClick={() => navigateDate("next")}
              className="rounded-full bg-gray-800/50 border-gray-700 hover:bg-indigo-900/50 hover:border-indigo-600"
            >
              <ChevronRight className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>

      <AnimatePresence mode="wait">
        {viewMode === "year" && renderYearView()}
        {viewMode === "month" && renderMonthView()}
        {viewMode === "day" && renderMonthView()}
      </AnimatePresence>
    </div>
  )
}