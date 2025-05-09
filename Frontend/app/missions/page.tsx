"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation" 
import axiosInstance from "@/lib/axiosInstance"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { CalendarView } from "@/components/calendar-view"
import { AnticipatedEventCard } from "@/components/anticipated-event-card" 
import { CalendarSearchBar } from "@/components/calendar-search-bar"
import { Button } from "@/components/ui/button"
import { ChevronLeft, Search, Calendar as CalendarIcon } from "lucide-react" // ChevronLeft might be unused now
import { BloomingStars } from "@/components/blooming-stars"

interface Event {
  id: number;
  title: string;
  description: string;
  date: string;
  agency: string;
  location: string;
  image: string;
  tags: string[];
  status?: string; 
}

interface SpaceMissionDTO {
  id: number; 
  name: string;
  agency: string;
  launchDate: string; 
  description: string;
  missionImage: string | null;
  liveStreamUrl: string | null;
  status: string; 
  creatorUserId: number;
  creatorUsername: string | null;
}

interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; 
}

// type ViewMode = "year" | "month" | "day"; // Only "year" view will be primarily used here

export default function MissionsPage() {
  const router = useRouter(); 
  const [upcomingMissions, setUpcomingMissions] = useState<Event[]>([])
  const [inProgressMissions, setInProgressMissions] = useState<Event[]>([])
  const [completedMissions, setCompletedMissions] = useState<Event[]>([])
  const [allEventsForCalendar, setAllEventsForCalendar] = useState<Event[]>([])
  // eventsForMonthView, selectedMonth, selectedYear are removed

  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  // viewMode will be predominantly "year" on this page. 
  // CalendarView itself handles internal view mode for display if needed, but navigation takes precedence.
  const [viewMode, setViewMode] = useState<"year" | "month" | "day">("year"); 
  const [isSearchOpen, setIsSearchOpen] = useState(false)

  const dtoToEvent = (dto: SpaceMissionDTO): Event => ({
    id: dto.id,
    title: dto.name,
    description: dto.description,
    date: dto.launchDate,
    agency: dto.agency,
    location: "Space Event", 
    image: dto.missionImage || "/placeholder.svg?height=300&width=500",
    tags: dto.name.toLowerCase().split(" ").slice(0, 2), 
    status: dto.status,
  });

  useEffect(() => {
    const fetchAllMissionsData = async () => {
      setIsLoading(true);
      setError(null);
      let allFetchedMissions: Event[] = [];
      const fetchPromises = [
        axiosInstance.get<Page<SpaceMissionDTO>>("/missions/status/UPCOMING?page=0&size=20"),
        axiosInstance.get<Page<SpaceMissionDTO>>("/missions/status/IN_PROGRESS?page=0&size=20"),
        axiosInstance.get<Page<SpaceMissionDTO>>("/missions/status/COMPLETED?page=0&size=20"),
      ];

      try {
        const results = await Promise.allSettled(fetchPromises);
        
        results.forEach((result, index) => {
          if (result.status === "fulfilled") {
            const mapped = result.value.data.content.map(dtoToEvent);
            if (index === 0) setUpcomingMissions(mapped);
            else if (index === 1) setInProgressMissions(mapped);
            else if (index === 2) setCompletedMissions(mapped);
            allFetchedMissions = [...allFetchedMissions, ...mapped];
          } else {
            console.error(`Failed to fetch missions for status index ${index}:`, result.reason);
          }
        });
        
        setAllEventsForCalendar(allFetchedMissions);
        if (allFetchedMissions.length === 0 && results.some(r => r.status === 'rejected')) {
            setError("Failed to load some mission data. Please check console for details.");
        }

      } catch (err) { 
        console.error("Unexpected error fetching all missions:", err);
        setError("An unexpected error occurred while loading missions.");
      } finally {
        setIsLoading(false);
      }
    };
    fetchAllMissionsData();
  }, []);

  // Removed useEffect for eventsForMonthView
  // Removed handleMonthYearSelect

  // handleBack is no longer needed as this page is primarily year view.
  // Navigation back from month page will use router.back() or link to /missions.

  const pageTitle = "Space Missions Calendar"; // Simplified title

  const handleDayClick = (date: Date, eventsOnDay: Event[]) => {
    // This might still be relevant if CalendarView is in month mode on this page,
    // but the primary interaction is navigating away when a month is clicked.
    // For now, if a day is clicked (e.g. if user somehow gets to month view here without navigating),
    // it will try to navigate to the event.
    if (eventsOnDay.length === 1) {
      router.push(`/missions/events/${eventsOnDay[0].id}`);
    } else if (eventsOnDay.length > 1) {
      console.log(`Multiple events on ${date.toDateString()}:`, eventsOnDay);
    }
  };
  
  // onViewModeChange for CalendarView can still update local viewMode if needed for UI elements on this page
  // but CalendarView's internal logic now drives navigation for month selection.
  const handleViewModeChange = (newMode: "year" | "month" | "day") => {
    setViewMode(newMode);
    // If CalendarView somehow switches to month view on this page (e.g. via its own toggle),
    // and we want to navigate, we could add logic here.
    // However, CalendarView's toggle now also tries to navigate.
  };


  if (isLoading) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p className="text-xl">Loading missions...</p>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-black text-white relative">
      <BloomingStars />
      <MinimalNavigation />
      <main className="flex-1 p-6 ml-12 transition-all duration-300 relative z-10">
        <div className="container mx-auto">
          <div className="flex justify-between items-center mb-8">
            <div>
              {/* Header no longer needs back button as this is top level for calendar year view */}
              <h1 className="text-3xl font-bold">{pageTitle}</h1>
            </div>
            <div className="flex gap-2">
              {/* The CalendarIcon button in CalendarView now handles navigation to current month page */}
              {/* This button could be removed or repurposed if CalendarView's toggle is preferred */}
               <Button
                  variant="outline"
                  size="icon"
                  onClick={() => {
                    // This button forces navigation to current month's page
                    const now = new Date();
                    router.push(`/missions/month/${now.getFullYear()}/${now.getMonth() + 1}`);
                  }}
                  className="h-10 w-10 rounded-full bg-gray-800/50 border-gray-700 hover:bg-indigo-900/50 hover:border-indigo-600 text-gray-300 hover:text-indigo-400 backdrop-blur-sm"
                  title="View Current Month"
                >
                  <CalendarIcon className="h-5 w-5" />
                  <span className="sr-only">View Current Month</span>
                </Button>
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

          <div className="bg-transparent rounded-xl mb-10">
            <CalendarView
              events={allEventsForCalendar} // Always pass all events for year view context
              initialViewMode="year" // This page is now primarily for year view
              // initialMonth is not needed as month selection navigates away
              onViewModeChange={handleViewModeChange} // Parent still knows the mode
              onDayClick={handleDayClick} // For potential day clicks if month view is reached
              // onMonthSelect is removed
            />
          </div>

          {upcomingMissions.length > 0 && (
            <div className="mb-10">
              <h2 className="text-2xl font-bold mb-4">Upcoming Missions</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {upcomingMissions.map((event) => (
                  <AnticipatedEventCard key={event.id} event={event} />
                ))}
              </div>
            </div>
          )}

          {inProgressMissions.length > 0 && (
            <div className="mb-10">
              <h2 className="text-2xl font-bold mb-4">Current Missions</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {inProgressMissions.map((event) => (
                  <AnticipatedEventCard key={event.id} event={event} />
                ))}
              </div>
            </div>
          )}

          {completedMissions.length > 0 && (
            <div className="mb-10">
              <h2 className="text-2xl font-bold mb-4">Past Missions</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {completedMissions.map((event) => (
                  <AnticipatedEventCard key={event.id} event={event} />
                ))}
              </div>
            </div>
          )}
          
          {error && (
            <div className="text-center py-10">
              <p className="text-xl text-red-500">{error}</p>
            </div>
          )}

          {!isLoading && !error && allEventsForCalendar.length === 0 && (
            <div className="text-center py-10">
              <p className="text-xl text-gray-400">No missions found at the moment.</p>
            </div>
          )}
        </div>
      </main>

      {isSearchOpen && <CalendarSearchBar onClose={() => setIsSearchOpen(false)} events={allEventsForCalendar} />}
    </div>
  )
}
