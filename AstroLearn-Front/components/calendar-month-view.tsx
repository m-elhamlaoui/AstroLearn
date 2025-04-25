"use client"

import { useState } from "react"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { Button } from "@/components/ui/button"

interface Event {
  id: string | number
  title: string
  date: string | Date
  type: "launch" | "observation" | "anniversary" | "other"
}

interface CalendarMonthViewProps {
  events: Event[]
  onSelectDay: (day: number, month: number, year: number) => void
}

export function CalendarMonthView({ events, onSelectDay }: CalendarMonthViewProps) {
  const [currentDate, setCurrentDate] = useState(new Date())

  const goToPreviousMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1))
  }

  const goToNextMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1))
  }

  // Get month details
  const currentYear = currentDate.getFullYear()
  const currentMonth = currentDate.getMonth()
  const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay()
  const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate()

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

  // Generate calendar days
  const calendarDays = []

  // Fill in days from previous month if needed
  const prevMonthDays = new Date(currentYear, currentMonth, 0).getDate()
  for (let i = 0; i < firstDayOfMonth; i++) {
    calendarDays.push({
      day: prevMonthDays - firstDayOfMonth + i + 1,
      currentMonth: false,
      date: new Date(currentYear, currentMonth - 1, prevMonthDays - firstDayOfMonth + i + 1),
    })
  }

  // Fill in days of current month
  for (let i = 1; i <= daysInMonth; i++) {
    calendarDays.push({
      day: i,
      currentMonth: true,
      date: new Date(currentYear, currentMonth, i),
    })
  }

  // Fill in days from next month if needed
  const remainingDays = 42 - calendarDays.length // 6 rows of 7 days
  for (let i = 1; i <= remainingDays; i++) {
    calendarDays.push({
      day: i,
      currentMonth: false,
      date: new Date(currentYear, currentMonth + 1, i),
    })
  }

  // Check if a day has events
  const getDayEvents = (date: Date) => {
    return events.filter(
      (event) => {
        // Convert event.date to Date object if it's a string
        const eventDate = event.date instanceof Date ? event.date : new Date(event.date)
        return (
          eventDate.getDate() === date.getDate() &&
          eventDate.getMonth() === date.getMonth() &&
          eventDate.getFullYear() === date.getFullYear()
        )
      }
    )
  }

  // Get event indicator color based on event type
  const getEventColor = (type: string) => {
    switch (type) {
      case "launch":
        return "bg-blue-500"
      case "observation":
        return "bg-purple-500"
      case "anniversary":
        return "bg-yellow-500"
      default:
        return "bg-gray-500"
    }
  }

  return (
    <div className="bg-gray-900 rounded-xl overflow-hidden shadow-lg">
      {/* Calendar header with smooth gradient background */}
      <div className="bg-gradient-to-r from-blue-900/50 to-purple-900/50 p-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold text-white">
            {monthNames[currentMonth]} {currentYear}
          </h2>
          <div className="flex space-x-2">
            <Button variant="ghost" size="icon" onClick={goToPreviousMonth} className="text-white hover:bg-white/10">
              <ChevronLeft className="h-5 w-5" />
            </Button>
            <Button variant="ghost" size="icon" onClick={goToNextMonth} className="text-white hover:bg-white/10">
              <ChevronRight className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </div>

      {/* Calendar grid with smooth styling */}
      <div className="p-2 bg-gradient-to-b from-gray-900 to-gray-950">
        {/* Day names */}
        <div className="grid grid-cols-7 mb-2">
          {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((day, index) => (
            <div key={index} className="text-center py-2 text-gray-400 text-sm">
              {day}
            </div>
          ))}
        </div>

        {/* Calendar days */}
        <div className="grid grid-cols-7 gap-1">
          {calendarDays.map((day, index) => {
            const dayEvents = getDayEvents(day.date)
            const hasEvents = dayEvents.length > 0

            return (
              <div
                key={index}
                onClick={() => day.currentMonth && onSelectDay(day.day, currentMonth, currentYear)}
                className={`
                  relative h-14 p-1 rounded-lg transition-all
                  ${day.currentMonth ? "cursor-pointer" : "cursor-default"}
                  ${day.currentMonth ? "bg-gray-800/80 hover:bg-gray-700/80" : "bg-gray-900/50 text-gray-600"}
                `}
              >
                <div className="text-sm font-medium mb-1">{day.day}</div>

                {/* Event indicators */}
                {hasEvents && (
                  <div className="flex space-x-1 mt-1">
                    {dayEvents.slice(0, 3).map((event, i) => (
                      <div
                        key={i}
                        className={`h-1.5 w-1.5 rounded-full ${getEventColor(event.type)}`}
                        title={event.title}
                      />
                    ))}
                    {dayEvents.length > 3 && <div className="text-xs text-gray-400">+{dayEvents.length - 3}</div>}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
