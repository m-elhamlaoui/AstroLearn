"use client"

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import axiosInstance from "@/lib/axiosInstance";
import { MinimalNavigation } from "@/components/minimal-navigation";
import { CalendarView } from "@/components/calendar-view"; // Reusing CalendarView
import { AnticipatedEventCard } from "@/components/anticipated-event-card";
import { Button } from "@/components/ui/button";
import { ChevronLeft } from "lucide-react";
import { BloomingStars } from "@/components/blooming-stars";

// Interfaces (can be shared or defined locally if specific)
interface Event {
  id: number;
  title: string;
  description: string;
  date: string; // ISO string
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

const dtoToEvent = (dto: SpaceMissionDTO): Event => ({
  id: dto.id,
  title: dto.name,
  description: dto.description,
  date: dto.launchDate,
  agency: dto.agency,
  location: "Space Event", // Placeholder
  image: dto.missionImage || "/placeholder.svg?height=300&width=500",
  tags: dto.name.toLowerCase().split(" ").slice(0, 2),
  status: dto.status,
});

export default function MonthMissionsPage() {
  const router = useRouter();
  const params = useParams();
  const { year, month } = params; // year and month are strings

  const [missions, setMissions] = useState<Event[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [currentDisplayDate, setCurrentDisplayDate] = useState(() => {
    if (year && month && typeof year === 'string' && typeof month === 'string') {
      const numericYear = parseInt(year, 10);
      const numericMonth = parseInt(month, 10); // Month from URL is 1-12
      if (!isNaN(numericYear) && !isNaN(numericMonth) && numericMonth >= 1 && numericMonth <= 12) {
        return new Date(numericYear, numericMonth - 1); // month is 1-12, Date constructor needs 0-11 for month
      }
    }
    // Fallback if params are initially undefined or invalid. Error will be set in useEffect.
    return new Date(); 
  });

  useEffect(() => {
    if (year && month && typeof year === 'string' && typeof month === 'string') {
      const numericYear = parseInt(year, 10);
      const numericMonth = parseInt(month, 10); 

      if (!isNaN(numericYear) && !isNaN(numericMonth) && numericMonth >= 1 && numericMonth <= 12) {
        const newDate = new Date(numericYear, numericMonth - 1);
        // Ensure currentDisplayDate is updated if params change or initial parse failed
        if (currentDisplayDate.getFullYear() !== newDate.getFullYear() || currentDisplayDate.getMonth() !== newDate.getMonth()) {
            setCurrentDisplayDate(newDate);
        }
        
        const fetchMonthMissions = async () => {
          setIsLoading(true);
          setError(null);
          try {
            // Note: The DTO from /missions/month/{year}/{month} is List<SpaceMissionDTO>, not Page<SpaceMissionDTO>
            const response = await axiosInstance.get<SpaceMissionDTO[]>(`/missions/month/${numericYear}/${numericMonth}`);
            const mappedMissions = response.data.map(dtoToEvent);
            setMissions(mappedMissions);
          } catch (err) {
            console.error(`Failed to fetch missions for ${numericYear}-${numericMonth}:`, err);
            setError(`Failed to load missions for ${numericYear}-${numericMonth}. Please try again later.`);
          } finally {
            setIsLoading(false);
          }
        };
        fetchMonthMissions();
      } else {
        setError("Invalid year or month in URL.");
        setIsLoading(false);
      }
    } else {
        setError("Year or month parameter missing.");
        setIsLoading(false);
    }
  }, [year, month]);

  const handleDayClickInMonthPage = (date: Date, eventsOnDay: Event[]) => {
    if (eventsOnDay.length === 1) {
      router.push(`/missions/events/${eventsOnDay[0].id}`);
    } else if (eventsOnDay.length > 1) {
      console.log("Multiple events on this day:", eventsOnDay);
      // Potentially show a modal or list for multiple events
    }
  };
  
  const pageTitle = currentDisplayDate ? 
    `${currentDisplayDate.toLocaleString('default', { month: 'long' })} ${currentDisplayDate.getFullYear()}`
    : "Missions";

  if (isLoading) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p className="text-xl">Loading missions for {pageTitle}...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center p-8 text-center">
        <div>
          <p className="text-xl text-red-500 mb-4">{error}</p>
          <Button variant="outline" onClick={() => router.push('/missions')}>Back to Main Calendar</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-black text-white relative">
      <BloomingStars />
      <MinimalNavigation />
      <main className="flex-1 p-6 ml-12 transition-all duration-300 relative z-10">
        <div className="container mx-auto">
          <div className="flex items-center gap-2 mb-8">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => router.push('/missions')}
              className="text-gray-400 hover:text-white"
            >
              <ChevronLeft className="h-5 w-5" />
              <span className="sr-only">Back to Main Calendar</span>
            </Button>
            <h1 className="text-3xl font-bold">{pageTitle}</h1>
          </div>

          <div className="bg-transparent rounded-xl mb-10">
            <CalendarView
              events={missions} 
              initialViewMode="month" 
              initialMonth={currentDisplayDate.getMonth() + 1} 
              initialYear={currentDisplayDate.getFullYear()} // Explicitly pass the year
              onDayClick={handleDayClickInMonthPage}
              // onViewModeChange could be used if this page needs to react to view mode changes within CalendarView
              // onMonthSelect is not applicable here as this page is already month-specific
            />
          </div>

          {missions.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {missions.map((event) => (
                <AnticipatedEventCard key={event.id} event={event} />
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-400 text-xl">No missions scheduled for {pageTitle}.</p>
          )}
        </div>
      </main>
    </div>
  );
}
