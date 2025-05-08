"use client"

import { useState, useEffect } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { CourseCard } from "@/components/course-card"
import { Button } from "@/components/ui/button"
import { Search, ChevronRight, ArrowLeft } from "lucide-react"
import { BloomingStars } from "@/components/blooming-stars"

// Sample data - would be fetched from backend in production
const sampleCourses = [
  {
    id: 1,
    title: "Introduction to Astronomy",
    description: "Learn the fundamentals of astronomy, from celestial objects to the structure of the universe.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Dr. Elena Rodriguez",
    level: "Beginner",
    duration: "4 weeks",
    category: "Astronomy Basics",
    rating: 4.8,
    studentsCount: 1245,
    completed: false,
  },
  {
    id: 2,
    title: "Rocket Science Basics",
    description: "Understand the principles of rocketry, propulsion systems, and spacecraft design.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Prof. Marcus Chen",
    level: "Intermediate",
    duration: "6 weeks",
    category: "Space Engineering",
    rating: 4.6,
    studentsCount: 892,
    completed: false,
  },
  {
    id: 3,
    title: "Exoplanet Discovery and Analysis",
    description: "Explore methods for detecting exoplanets and analyzing their potential habitability.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Dr. Sophia Williams",
    level: "Advanced",
    duration: "8 weeks",
    category: "Planetary Science",
    rating: 4.9,
    studentsCount: 756,
    completed: false,
  },
  {
    id: 4,
    title: "The Solar System",
    description: "A comprehensive tour of our solar system, exploring each planet and major celestial body.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Dr. James Peterson",
    level: "Beginner",
    duration: "5 weeks",
    category: "Astronomy Basics",
    rating: 4.7,
    studentsCount: 1532,
    completed: true,
  },
  {
    id: 5,
    title: "Space Telescopes and Observatories",
    description: "Learn about the various space telescopes and their contributions to our understanding of the cosmos.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Dr. Amara Khan",
    level: "Intermediate",
    duration: "4 weeks",
    category: "Astronomy Basics",
    rating: 4.5,
    studentsCount: 678,
    completed: false,
  },
  {
    id: 6,
    title: "Spacecraft Systems Engineering",
    description: "Deep dive into the engineering principles behind spacecraft systems and subsystems.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Prof. Robert Lee",
    level: "Advanced",
    duration: "10 weeks",
    category: "Space Engineering",
    rating: 4.8,
    studentsCount: 423,
    completed: false,
  },
  {
    id: 7,
    title: "Mars Exploration",
    description: "Explore the history, current missions, and future plans for Mars exploration.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Dr. Sarah Johnson",
    level: "Intermediate",
    duration: "6 weeks",
    category: "Planetary Science",
    rating: 4.9,
    studentsCount: 912,
    completed: false,
  },
  {
    id: 8,
    title: "Astrophotography Fundamentals",
    description: "Learn techniques for capturing stunning images of celestial objects.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Michael Torres",
    level: "Beginner",
    duration: "3 weeks",
    category: "Astronomy Basics",
    rating: 4.7,
    studentsCount: 1087,
    completed: false,
  },
  {
    id: 9,
    title: "Space Mission Design",
    description: "Learn the process of designing space missions from concept to execution.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Dr. Emily Chen",
    level: "Advanced",
    duration: "8 weeks",
    category: "Space Engineering",
    rating: 4.6,
    studentsCount: 345,
    completed: false,
  },
  {
    id: 10,
    title: "Astrobiology",
    description: "Explore the study of life in the universe and the search for extraterrestrial life.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Prof. David Kim",
    level: "Intermediate",
    duration: "7 weeks",
    category: "Planetary Science",
    rating: 4.8,
    studentsCount: 678,
    completed: false,
  },
  {
    id: 11,
    title: "Orbital Mechanics",
    description: "Understand the mathematics and physics behind orbital dynamics and spacecraft trajectories.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Dr. Alan Foster",
    level: "Advanced",
    duration: "9 weeks",
    category: "Space Engineering",
    rating: 4.7,
    studentsCount: 412,
    completed: false,
  },
  {
    id: 12,
    title: "Cosmology and the Big Bang",
    description: "Explore theories about the origin and evolution of the universe.",
    image: "/placeholder.svg?height=300&width=500",
    instructor: "Prof. Lisa Wong",
    level: "Advanced",
    duration: "8 weeks",
    category: "Theoretical Astrophysics",
    rating: 4.9,
    studentsCount: 532,
    completed: false,
  },
]

export default function CoursesPage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [filteredCourses, setFilteredCourses] = useState(sampleCourses)
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null)

  // Get unique categories
  const categories = Array.from(new Set(sampleCourses.map((course) => course.category)))

  // Filter courses based on search query and selected category
  useEffect(() => {
    let filtered = sampleCourses

    if (searchQuery) {
      filtered = filtered.filter(
        (course) =>
          course.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          course.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
          course.instructor.toLowerCase().includes(searchQuery.toLowerCase()) ||
          course.category.toLowerCase().includes(searchQuery.toLowerCase()) ||
          course.level.toLowerCase().includes(searchQuery.toLowerCase()),
      )
    }

    if (selectedCategory) {
      filtered = filtered.filter((course) => course.category === selectedCategory)
    }

    setFilteredCourses(filtered)
  }, [searchQuery, selectedCategory])

  // Group courses by category
  const coursesByCategory = categories.reduce(
    (acc, category) => {
      // If a category is selected, only show courses from that category
      if (selectedCategory && category !== selectedCategory) {
        return acc
      }

      const coursesInCategory = filteredCourses.filter((course) => course.category === category)

      if (coursesInCategory.length > 0) {
        acc[category] = coursesInCategory
      }

      return acc
    },
    {} as Record<string, typeof sampleCourses>,
  )

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
              {selectedCategory ? (
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => setSelectedCategory(null)}
                    className="h-8 w-8 rounded-full bg-gray-800/50 border-gray-700 hover:bg-indigo-900/50 hover:border-indigo-600 text-gray-300 hover:text-indigo-400 backdrop-blur-sm"
                  >
                    <ArrowLeft className="h-4 w-4" />
                    <span className="sr-only">All Categories</span>
                  </Button>
                  <h1 className="text-3xl font-bold">{selectedCategory}</h1>
                </div>
              ) : (
                <h1 className="text-3xl font-bold">Space Exploration Courses</h1>
              )}
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

          {/* Courses by Category */}
          {Object.keys(coursesByCategory).length > 0 ? (
            Object.entries(coursesByCategory).map(([category, courses]) => (
              <div key={category} className="mb-12">
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-2xl font-bold">{category}</h2>
                  {!selectedCategory && (
                    <Button
                      variant="ghost"
                      onClick={() => setSelectedCategory(category)}
                      className="text-gray-400 hover:text-white flex items-center"
                    >
                      <span className="sr-only">Explore More</span>
                      <ChevronRight className="h-5 w-5" />
                    </Button>
                  )}
                </div>

                {/* Responsive Course Grid/Flex */}
                <div className="grid grid-cols-1 gap-6 md:flex md:overflow-x-auto md:pb-4 md:space-x-4 md:scrollbar-hide">
                  {courses.map((course) => (
                    // On medium screens and up, use flex-none and width; otherwise, let grid handle width
                    <div key={course.id} className="md:flex-none md:w-80">
                      <CourseCard course={course} />
                    </div>
                  ))}
                </div>
              </div>
            ))
          ) : (
            <div className="text-center py-12">
              <p className="text-gray-400 text-lg">No courses found matching your search criteria.</p>
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setSearchQuery("")
                  setSelectedCategory(null)
                }}
                className="mt-4 border-gray-700 text-gray-300 hover:bg-gray-800"
              >
                Clear Filters
              </Button>
            </div>
          )}
        </div>
      </main>
    </div>
  )
}
