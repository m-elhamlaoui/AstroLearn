"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import axiosInstance from "@/lib/axiosInstance"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Bar, Pie, Line } from "react-chartjs-2"
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  PointElement,
  LineElement,
} from "chart.js"

// Register ChartJS components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  PointElement,
  LineElement
)

export default function AdminDashboardPage() {
  const router = useRouter()
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalArticles: 0,
    totalCourses: 0,
    totalComments: 0,
    pendingVerifications: 0,
    userLevels: { NOVICE: 0, EXPLORER: 0, ASTRONAUT: 0, GALACTIC: 0 },
    articlesByMonth: Array(6).fill(0),
    courseCompletionRate: 0,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  // Fetch dashboard statistics
  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true)
        
        // Fetch users count
        const usersResponse = await axiosInstance.get("/users")
        const users = usersResponse.data
        
        // Fetch articles count
        const articlesResponse = await axiosInstance.get("/articles")
        const articles = articlesResponse.data.content || []
        
        // Fetch courses (assuming there's an endpoint)
        const coursesResponse = await axiosInstance.get("/courses")
        const courses = coursesResponse.data || []
        
        // Calculate user levels distribution
        const levels = { NOVICE: 0, EXPLORER: 0, ASTRONAUT: 0, GALACTIC: 0 }
        users.forEach(user => {
          if (user.level in levels) {
            levels[user.level]++
          }
        })

        // Count pending verification requests
        const pendingVerifications = users.filter(user => 
          user.verificationStatus === "PENDING"
        ).length

        // Calculate stats
        setStats({
          totalUsers: users.length,
          totalArticles: articles.length,
          totalCourses: courses.length,
          totalComments: articles.reduce((sum, article) => sum + (article.commentCount || 0), 0),
          pendingVerifications,
          userLevels: levels,
          articlesByMonth: getArticlesByMonth(articles),
          courseCompletionRate: calculateCourseCompletionRate(courses, users),
        })
      } catch (err) {
        console.error("Error fetching dashboard statistics:", err)
        setError("Failed to load dashboard statistics")
      } finally {
        setLoading(false)
      }
    }

    fetchStats()
  }, [])

  // Helper function to calculate articles by month
  const getArticlesByMonth = (articles) => {
    const now = new Date()
    const sixMonthsAgo = new Date(now.getFullYear(), now.getMonth() - 5, 1)
    const monthCounts = Array(6).fill(0)
    
    articles.forEach(article => {
      const createdAt = new Date(article.createdAt)
      if (createdAt >= sixMonthsAgo) {
        const monthIndex = (now.getMonth() - createdAt.getMonth() + 12) % 12
        if (monthIndex < 6) {
          monthCounts[5 - monthIndex]++
        }
      }
    })
    
    return monthCounts
  }

  // Helper function to calculate course completion rate
  const calculateCourseCompletionRate = (courses, users) => {
    if (!courses.length || !users.length) return 0
    
    // This is a placeholder calculation
    // In a real implementation, you'd need to fetch course progress data
    const completedCourses = Math.floor(Math.random() * 100)
    const totalEnrollments = courses.length * users.length / 3
    
    return totalEnrollments > 0 ? (completedCourses / totalEnrollments) * 100 : 0
  }

  // Prepare chart data
  const userLevelsData = {
    labels: Object.keys(stats.userLevels),
    datasets: [
      {
        data: Object.values(stats.userLevels),
        backgroundColor: [
          'rgba(75, 192, 192, 0.6)',
          'rgba(54, 162, 235, 0.6)',
          'rgba(153, 102, 255, 0.6)',
          'rgba(255, 99, 132, 0.6)',
        ],
        borderWidth: 1,
      },
    ],
  }

  const articlesByMonthData = {
    labels: getLast6MonthsLabels(),
    datasets: [
      {
        label: 'Articles Published',
        data: stats.articlesByMonth,
        backgroundColor: 'rgba(153, 102, 255, 0.6)',
        borderColor: 'rgba(153, 102, 255, 1)',
        borderWidth: 1,
      },
    ],
  }

  // Helper function to get month labels for the last 6 months
  function getLast6MonthsLabels() {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
    const result = []
    const now = new Date()
    for (let i = 5; i >= 0; i--) {
      const monthIndex = (now.getMonth() - i + 12) % 12
      result.push(months[monthIndex])
    }
    return result
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="w-16 h-16 border-4 border-t-indigo-500 border-r-transparent border-b-indigo-500 border-l-transparent rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="text-center py-10">
        <h2 className="text-2xl font-bold text-red-500">Error</h2>
        <p className="mt-2">{error}</p>
        <button 
          onClick={() => window.location.reload()}
          className="mt-4 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 rounded-md text-white"
        >
          Retry
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Admin Dashboard</h1>
        <p className="text-gray-400 mt-2">Overview of platform statistics and metrics</p>
      </div>

      {/* Stats Overview Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="bg-gray-800 border-gray-700">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg text-gray-200">Total Users</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-indigo-400">{stats.totalUsers}</div>
          </CardContent>
        </Card>
        
        <Card className="bg-gray-800 border-gray-700">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg text-gray-200">Total Articles</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-indigo-400">{stats.totalArticles}</div>
          </CardContent>
        </Card>
        
        <Card className="bg-gray-800 border-gray-700">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg text-gray-200">Courses</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-indigo-400">{stats.totalCourses}</div>
          </CardContent>
        </Card>
        
        <Card className="bg-gray-800 border-gray-700">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg text-gray-200">Pending Verifications</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-amber-500">{stats.pendingVerifications}</div>
          </CardContent>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* User Levels Distribution */}
        <Card className="bg-gray-800 border-gray-700">
          <CardHeader>
            <CardTitle className="text-lg text-gray-200">User Levels Distribution</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-80">
              <Pie 
                data={userLevelsData} 
                options={{ 
                  maintainAspectRatio: false,
                  plugins: {
                    legend: {
                      position: 'bottom',
                      labels: {
                        color: '#e2e8f0'
                      }
                    }
                  }
                }} 
              />
            </div>
          </CardContent>
        </Card>

        {/* Articles Published By Month */}
        <Card className="bg-gray-800 border-gray-700">
          <CardHeader>
            <CardTitle className="text-lg text-gray-200">Articles Published (Last 6 Months)</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-80">
              <Bar 
                data={articlesByMonthData} 
                options={{ 
                  maintainAspectRatio: false,
                  scales: {
                    y: {
                      beginAtZero: true,
                      ticks: {
                        color: '#e2e8f0'
                      },
                      grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                      }
                    },
                    x: {
                      ticks: {
                        color: '#e2e8f0'
                      },
                      grid: {
                        display: false
                      }
                    }
                  },
                  plugins: {
                    legend: {
                      labels: {
                        color: '#e2e8f0'
                      }
                    }
                  }
                }} 
              />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Course Completion Rate Card */}
      <Card className="bg-gray-800 border-gray-700">
        <CardHeader>
          <CardTitle className="text-lg text-gray-200">Course Completion Rate</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center">
            <div className="w-full bg-gray-700 rounded-full h-4">
              <div 
                className="bg-green-500 h-4 rounded-full" 
                style={{ width: `${stats.courseCompletionRate}%` }}
              ></div>
            </div>
            <span className="ml-4 text-lg font-medium text-gray-200">
              {stats.courseCompletionRate.toFixed(1)}%
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
