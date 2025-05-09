"use client"

import { useState, useEffect } from "react";
import axiosInstance from "@/lib/axiosInstance"; // Added
import { MinimalNavigation } from "@/components/minimal-navigation";
import { CourseCard } from "@/components/course-card";
import { Button } from "@/components/ui/button";
import { Search } from "lucide-react"; // Removed ChevronRight, ArrowLeft
import { BloomingStars } from "@/components/blooming-stars";

// Define interfaces for course data
interface BackendCourse {
  id: number;
  title: string;
  imageUrl: string;
  description: string;
  difficulty: string; // e.g., "BEGINNER", "INTERMEDIATE", "ADVANCED"
  totalLessons: number; // Available from DTO, not directly used in current UI mapping but fetched
  moduleIds: number[]; // Available from DTO, not directly used in current UI mapping but fetched
}

// Interface for data passed to CourseCard, matching CourseCard's expected props
interface DisplayCourse {
  id: number;
  title: string;
  description: string;
  image: string;
  instructor: string;
  level: string;
  duration: string;
  category: string;
  rating: number;
  studentsCount: number;
  completed: boolean;
}

export default function CoursesPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [allCourses, setAllCourses] = useState<BackendCourse[]>([]); // Holds all courses fetched from backend
  const [filteredDisplayCourses, setFilteredDisplayCourses] = useState<DisplayCourse[]>([]); // Holds courses for display after filtering and mapping
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // selectedCategory state and related logic are now fully removed.

  // Fetch courses from backend
  useEffect(() => {
    const fetchCourses = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await axiosInstance.get<BackendCourse[]>("/courses");
        setAllCourses(response.data);
      } catch (err) {
        console.error("Failed to fetch courses:", err);
        setError("Failed to load courses. Please try again later.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchCourses();
  }, []);

  // Filter courses based on search query and map to DisplayCourse
  useEffect(() => {
    let filteredBackendCourses = allCourses;

    if (searchQuery) {
      filteredBackendCourses = filteredBackendCourses.filter(
        (course) =>
          course.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          course.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
          course.difficulty.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    // Map BackendCourse to DisplayCourse
    const displayCourses = filteredBackendCourses.map((course): DisplayCourse => ({
      id: course.id,
      title: course.title,
      description: course.description,
      image: course.imageUrl || "/placeholder.svg?height=300&width=500",
      level: course.difficulty ? course.difficulty.charAt(0).toUpperCase() + course.difficulty.slice(1).toLowerCase() : "N/A",
      // Provide default/fallback values for fields not in BackendCourse/CourseDTO
      instructor: "N/A", // Not available from backend
      duration: "N/A", // Not available from backend (totalLessons could be used to infer but not directly)
      category: "N/A", // Not available from backend
      rating: 0, // Not available from backend
      studentsCount: 0, // Not available from backend
      completed: false, // Not available from backend, default to false
    }));

    setFilteredDisplayCourses(displayCourses);
  }, [searchQuery, allCourses]);

  // Category related logic has been removed.

  return (
    <div className="flex min-h-screen bg-black text-white relative">
      {/* Blooming Stars Animation */}
      <BloomingStars />
      
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300 relative z-10">
        <div className="container mx-auto">
          {/* Header with Search Bar */}
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
            <div>
              <h1 className="text-3xl font-bold">Space Exploration Courses</h1>
            </div>

            {/* Search Input with cleaner styling */}
            <div className="relative w-full md:w-64">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <input
                  type="text"
                  placeholder="Search courses..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-full py-2 pl-10 pr-10 text-white placeholder:text-gray-500 focus:outline-none focus:ring-1 focus:ring-indigo-400 focus:border-indigo-500"
                />
                {searchQuery && (
                  <button
                    onClick={() => setSearchQuery("")}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-white"
                  >
                    ×
                  </button>
                )}
              </div>
            </div>
          </div>

          {/* Display Courses, Loading, or Error Message */}
          {isLoading && (
            <div className="text-center py-12">
              <p className="text-gray-400 text-lg">Loading courses...</p>
              {/* Optional: Add a spinner or skeleton loader here */}
            </div>
          )}

          {error && (
            <div className="text-center py-12">
              <p className="text-red-500 text-lg">{error}</p>
              <Button
                variant="outline"
                size="sm"
                onClick={() => { // Attempt to refetch or clear error
                  // For simplicity, this example just clears search. A refetch function could be called.
                  setSearchQuery("");
                  // Consider adding a function to explicitly call fetchCourses() again
                }}
                className="mt-4 border-gray-700 text-gray-300 hover:bg-gray-800"
              >
                Try Again or Clear Search
              </Button>
            </div>
          )}

          {!isLoading && !error && filteredDisplayCourses.length > 0 && (
            <div className="mb-12">
              {/* Responsive Course Grid/Flex - No longer grouped by category */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {filteredDisplayCourses.map((course: DisplayCourse) => (
                  <div key={course.id}> {/* Removed md:flex-none md:w-80 for better grid behavior */}
                    <CourseCard course={course} />
                  </div>
                ))}
              </div>
            </div>
          )}

          {!isLoading && !error && filteredDisplayCourses.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gray-400 text-lg">
                {searchQuery ? "No courses found matching your search criteria." : "No courses available at the moment."}
              </p>
              {searchQuery && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setSearchQuery("");
                  }}
                  className="mt-4 border-gray-700 text-gray-300 hover:bg-gray-800"
                >
                  Clear Search
                </Button>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
