"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation" 
import axiosInstance from "@/lib/axiosInstance"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { CalendarView } from "@/components/calendar-view"
import { AnticipatedEventCard } from "@/components/anticipated-event-card" 
import { CalendarSearchBar } from "@/components/calendar-search-bar"
import { Button } from "@/components/ui/button"
import { ChevronLeft, Search, Calendar as CalendarIcon, Plus } from "lucide-react" 
import { BloomingStars } from "@/components/blooming-stars"
import { useAuthRedirect } from "@/lib/useAuthRedirect" // Import the hook
import { MissionCreateForm } from "@/components/mission-create-form" // Import the mission creation form
import { useToast } from "@/components/ui/use-toast"

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

export default function MissionsPage() {
  useAuthRedirect(); // Apply the auth redirect hook
  const router = useRouter();
  const { toast } = useToast();
  
  const [upcomingMissions, setUpcomingMissions] = useState<Event[]>([])
  const [inProgressMissions, setInProgressMissions] = useState<Event[]>([])
  const [completedMissions, setCompletedMissions] = useState<Event[]>([])
  const [allEventsForCalendar, setAllEventsForCalendar] = useState<Event[]>([])

  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  const [viewMode, setViewMode] = useState<"year" | "month" | "day">("year"); 
  const [isSearchOpen, setIsSearchOpen] = useState(false)
  const [isCreateFormOpen, setIsCreateFormOpen] = useState(false)
  const [isVerifiedUser, setIsVerifiedUser] = useState(false)

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

  // Check if the current user is verified
  useEffect(() => {
    const checkVerificationStatus = async () => {
      try {
        const userId = localStorage.getItem("userId");
        if (userId) {
          const response = await axiosInstance.get(`/users/${userId}`);
          setIsVerifiedUser(response.data.verificationStatus === "VERIFIED");
        }
      } catch (err) {
        console.error("Error checking verification status:", err);
      }
    };
    
    checkVerificationStatus();
  }, []);

  // Fetch mission data
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
  
  // Function to handle mission creation success
  const handleMissionCreated = () => {
    // Refresh the mission data
    setIsLoading(true);
    axiosInstance.get<Page<SpaceMissionDTO>>("/missions/status/UPCOMING?page=0&size=20")
      .then(response => {
        const mapped = response.data.content.map(dtoToEvent);
        setUpcomingMissions(mapped);
        // Update the calendar view as well
        setAllEventsForCalendar(prev => [
          ...prev.filter(event => event.status !== "UPCOMING"),
          ...mapped
        ]);
        toast({
          title: "Mission Added",
          description: "Your mission has been added to the calendar.",
          duration: 5000
        });
      })
      .catch(err => {
        console.error("Failed to refresh missions after creation:", err);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  const pageTitle = "Space Missions Calendar"; 

  const handleDayClick = (date: Date, eventsOnDay: Event[]) => {
    if (eventsOnDay.length === 1) {
      router.push(`/missions/events/${eventsOnDay[0].id}`);
    } else if (eventsOnDay.length > 1) {
      console.log(`Multiple events on ${date.toDateString()}:`, eventsOnDay);
    }
  };
  
  const handleViewModeChange = (newMode: "year" | "month" | "day") => {
    setViewMode(newMode);
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
              <h1 className="text-3xl font-bold">{pageTitle}</h1>
            </div>
            <div className="flex gap-2">
              {/* Create Mission Button - Only visible to verified users */}
              {isVerifiedUser && (
                <Button
                  onClick={() => setIsCreateFormOpen(true)}
                  className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600 flex items-center gap-2"
                >
                  <Plus className="h-4 w-4" />
                  Create Mission
                </Button>
              )}
              <Button
                variant="outline"
                size="icon"
                onClick={() => {
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
              events={allEventsForCalendar} 
              initialViewMode="year" 
              onViewModeChange={handleViewModeChange} 
              onDayClick={handleDayClick} 
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
      
      {/* Mission Creation Form */}
      {isCreateFormOpen && (
        <MissionCreateForm 
          isOpen={isCreateFormOpen} 
          onClose={() => setIsCreateFormOpen(false)} 
          onSuccess={handleMissionCreated} 
        />
      )}
    </div>
  )
}
