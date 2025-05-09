"use client"

import { useState, useEffect } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon, Clock, MapPin } from "lucide-react"
import { format } from "date-fns"
import { Button } from "@/components/ui/button"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Badge } from "@/components/ui/badge"
import Link from "next/link"
import { useRouter } from "next/navigation" // Added useRouter

interface Event {
  id: number 
  title: string
  date: string; 
  description: string; 
  location: string; 
  agency: string; 
  image: string; 
  tags: string[]; 
  status?: string; 
}

interface CalendarViewProps {
  events: Event[]
  initialViewMode?: "year" | "month" | "day"
  initialMonth?: number // Month number (1-12)
  initialYear?: number // Added to correctly initialize currentDate for month/day views on specific month pages
  onViewModeChange?: (mode: "year" | "month" | "day") => void // Still useful for parent to know the mode
  onDayClick?: (date: Date, events: Event[]) => void;
  // onMonthSelect is removed as navigation will handle month changes
}

export function CalendarView({ 
  events, 
  initialViewMode = "year", 
  initialMonth, 
  initialYear,
  onViewModeChange, 
  onDayClick 
}: CalendarViewProps) {
  const router = useRouter();
  const [viewMode, setViewMode] = useState(initialViewMode);
  
  const [currentDate, setCurrentDate] = useState(() => {
    const now = new Date();
    const yearToUse = initialYear || now.getFullYear();
    const monthToUse = initialMonth ? initialMonth - 1 : now.getMonth(); // initialMonth is 1-12, Date month is 0-11
    return new Date(yearToUse, monthToUse);
  });

  useEffect(() => {
    if (onViewModeChange) {
      onViewModeChange(viewMode)
    }
  }, [viewMode, onViewModeChange])

  useEffect(() => {
    // Sync currentDate if initialMonth or initialYear props change,
    // and CalendarView is in a mode where these props are relevant (month/day view).
    if (viewMode === "month" || viewMode === "day") {
      const yearToSet = initialYear !== undefined ? initialYear : currentDate.getFullYear();
      const monthToSet = initialMonth !== undefined ? initialMonth - 1 : currentDate.getMonth(); // initialMonth is 1-12

      if (currentDate.getFullYear() !== yearToSet || currentDate.getMonth() !== monthToSet) {
        setCurrentDate(new Date(yearToSet, monthToSet));
      }
    }
  }, [initialMonth, initialYear, viewMode]); // Removed currentDate from dependencies


  const navigateDate = (direction: "prev" | "next") => {
    setCurrentDate(prev => {
      const newDateBasis = new Date(prev); // Clone to avoid modifying 'prev' directly before calculation
      if (viewMode === "year") {
        newDateBasis.setFullYear(newDateBasis.getFullYear() + (direction === "next" ? 1 : -1));
        return newDateBasis;
      } else { // month or day view
        newDateBasis.setMonth(newDateBasis.getMonth() + (direction === "next" ? 1 : -1));
        // If on a specific month page, update URL
        if (window.location.pathname.startsWith("/missions/month/")) {
             router.push(`/missions/month/${newDateBasis.getFullYear()}/${newDateBasis.getMonth() + 1}`);
        }
        return newDateBasis;
      }
    })
  }

  const handleMonthSelectFromYearView = (monthNumber: number) => { // monthNumber is 1-12
    const year = currentDate.getFullYear();
    router.push(`/missions/month/${year}/${monthNumber}`);
  }

  const toggleView = () => {
    const newViewMode = viewMode === "year" ? "month" : "year";
    setViewMode(newViewMode);
    // If on main /missions page and toggling to month view, go to current month's page
    if (newViewMode === "month" && window.location.pathname === "/missions") {
        router.push(`/missions/month/${currentDate.getFullYear()}/${currentDate.getMonth() + 1}`);
    }
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
          
          return (
            <motion.button
              key={month}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => handleMonthSelectFromYearView(index + 1)}
              className={`
                p-4 rounded-xl border backdrop-blur-sm
                ${monthEvents.length > 0 ? 'bg-indigo-700/50 border-indigo-500 hover:bg-indigo-600/60' : 'bg-gray-800/40 border-gray-700/50 hover:bg-gray-700/50'}
                transition-all duration-200
              `}
            >
              <div className={`text-lg font-bold mb-2 ${monthEvents.length > 0 ? 'text-white' : 'text-gray-300'}`}>{month}</div>
              {monthEvents.length > 0 ? (
                <div className="text-sm text-indigo-200">{monthEvents.length} event{monthEvents.length === 1 ? '' : 's'}</div>
              ) : (
                <div className="text-sm text-gray-500">No events</div>
              )}
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

    const daysArray = Array.from({ length: daysInMonth }, (_, i) => i + 1)
    const paddingDays = Array.from({ length: firstDayOfMonth }, (_, i) => null)
    const allDays = [...paddingDays, ...daysArray]

    return (
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -20 }}
        className="w-full max-w-[1024px] mx-auto"
      >
        <div className="grid grid-cols-7 gap-1 mb-2">
          {dayNames.map((day, index) => (
            <div key={`${day}-${index}`} className="text-center text-sm font-medium text-gray-400">{day}</div>
          ))}
        </div>
        <div className="grid grid-cols-7 gap-1">
          {allDays.map((day, idx) => {
            if (!day) return <div key={`empty-${idx}`} className="aspect-square" />

            const dateObj = new Date(currentDate.getFullYear(), currentDate.getMonth(), day)
            const dayEvents = getEventsByDate(dateObj)
            
            return (
              <Popover key={day}>
                <PopoverTrigger asChild>
                  <motion.button
                    onClick={() => {
                      if (onDayClick) {
                        onDayClick(dateObj, dayEvents);
                      }
                    }}
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    className={`
                      aspect-square p-2 rounded-lg border relative
                      ${dayEvents.length > 0 ? 'bg-sky-700/40 border-sky-600 hover:bg-sky-600/50' : 'bg-gray-800/40 border-gray-700/50 hover:bg-gray-700/50'}
                      transition-all duration-200 flex flex-col items-center justify-center
                    `}
                  >
                    <div className={`text-sm font-medium ${dayEvents.length > 0 ? 'text-white' : 'text-gray-300'}`}>{day}</div>
                    {dayEvents.length > 0 && (
                      <span className="text-xs text-sky-300 mt-1">{dayEvents.length}</span>
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
