import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Star, Users, Clock, CheckCircle } from "lucide-react"
import Image from "next/image"
import Link from "next/link"

interface Course {
  id: number
  title: string
  description: string
  image: string
  instructor: string
  level: string
  duration: string
  category: string
  rating: number
  studentsCount: number
  completed: boolean
}

interface CourseCardProps {
  course: Course
}

export function CourseCard({ course }: CourseCardProps) {
  return (
    <Link href={`/courses/${course.id}`}>
      <Card className="bg-gray-900 border-gray-800 overflow-hidden rounded-xl hover:shadow-lg hover:shadow-white/5 transition-all duration-300 h-full flex flex-col">
        {/* Course Image */}
        <div className="relative h-40">
          <Image src={course.image || "/placeholder.svg"} alt={course.title} fill className="object-cover" />
          {course.completed && (
            <div className="absolute top-2 right-2">
              <Badge className="bg-green-600 text-white">
                <CheckCircle className="h-3 w-3 mr-1" /> Completed
              </Badge>
            </div>
          )}
        </div>

        {/* Course Content */}
        <div className="p-4 flex-1 flex flex-col">
          <div className="mb-2 flex items-center gap-1">
            <Badge className="bg-gray-800 text-white">{course.level}</Badge>
            <Badge className="bg-gray-800 text-white">{course.duration}</Badge>
          </div>

          <h3 className="text-lg font-bold mb-2 hover:text-gray-300 transition-colors line-clamp-2">{course.title}</h3>

          <p className="text-gray-400 text-sm mb-3 line-clamp-2">{course.description}</p>

          <div className="text-sm text-gray-500 mb-2">Instructor: {course.instructor}</div>

          <div className="mt-auto flex justify-between items-center">
            <div className="flex items-center gap-1 text-yellow-500">
              <Star className="h-4 w-4 fill-yellow-500 text-yellow-500" />
              <span className="text-white">{course.rating}</span>
            </div>

            <div className="flex items-center gap-1 text-gray-400">
              <Users className="h-4 w-4" />
              <span>{course.studentsCount.toLocaleString()}</span>
            </div>

            <div className="flex items-center gap-1 text-gray-400">
              <Clock className="h-4 w-4" />
              <span>{course.duration}</span>
            </div>
          </div>
        </div>
      </Card>
    </Link>
  )
}
