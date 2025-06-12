"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import axiosInstance from "@/lib/axiosInstance"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { BloomingStars } from "@/components/blooming-stars"
import { useAuthRedirect } from "@/lib/useAuthRedirect"
// @ts-ignore
import { Bar, Pie, Line } from "react-chartjs-2"
// @ts-ignore
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
  const { isLoading: authLoading } = useAuthRedirect()
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalArticles: 0,
    totalCourses: 0,
    totalComments: 0,
    pendingVerifications: 0,
    userLevels: { NOVICE: 0, EXPLORER: 0, ASTRONAUT: 0, GALACTIC: 0 },
    articlesByMonth: Array(6).fill(0),
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  // Fetch dashboard statistics
  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true)
        
        // Fetch users count with error handling
        const usersResponse = await axiosInstance.get("/users")
        let users = []
        if (usersResponse.data && usersResponse.data.content) {
          users = Array.isArray(usersResponse.data.content) ? usersResponse.data.content : []
        } else if (Array.isArray(usersResponse.data)) {
          users = usersResponse.data
        }
        console.log("Users data:", users)
        
        // Fetch articles count
        const articlesResponse = await axiosInstance.get("/articles")
        let articles = []
        if (articlesResponse.data && articlesResponse.data.content) {
          articles = Array.isArray(articlesResponse.data.content) ? articlesResponse.data.content : []
        } else if (Array.isArray(articlesResponse.data)) {
          articles = articlesResponse.data
        }
        console.log("Articles data:", articles)
        
        // Fetch courses (ensuring it's an array)
        const coursesResponse = await axiosInstance.get("/courses")
        const courses = Array.isArray(coursesResponse.data) ? coursesResponse.data : []
        console.log("Courses data:", courses)
        
        // Calculate user levels distribution (with safety checks)
        const levels = { NOVICE: 0, EXPLORER: 0, ASTRONAUT: 0, GALACTIC: 0 }
        if (Array.isArray(users)) {
          users.forEach(user => {
            if (user && user.level && levels.hasOwnProperty(user.level)) {
              // Use type assertion to handle the indexing
              levels[user.level as keyof typeof levels]++
            }
          })
        }

        // Count pending verification requests (with safety checks)
        let pendingVerifications = 0
        if (Array.isArray(users)) {
          pendingVerifications = users.filter(user => 
            user && user.verificationStatus === "PENDING"
          ).length
        }

        // Calculate stats
        setStats({
          totalUsers: users.length,
          totalArticles: articles.length,
          totalCourses: courses.length,
          totalComments: articles.reduce((sum: number, article: any) => sum + (article.commentCount || 0), 0),
          pendingVerifications,
          userLevels: levels,
          articlesByMonth: getArticlesByMonth(articles),
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
  const getArticlesByMonth = (articles: any[]) => {
    const now = new Date()
    const sixMonthsAgo = new Date(now.getFullYear(), now.getMonth() - 5, 1)
    const monthCounts = Array(6).fill(0)
    
    if (!Array.isArray(articles)) return monthCounts
    
    articles.forEach(article => {
      if (!article || !article.createdAt) return
      
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

  // This function has been removed as requested

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
      <div className="flex min-h-screen bg-black text-white items-center justify-center">
        <MinimalNavigation />
        <p className="text-xl">Loading dashboard statistics...</p>
        <div className="ml-4 w-16 h-16 border-4 border-t-indigo-500 border-r-transparent border-b-indigo-500 border-l-transparent rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-screen bg-black text-white items-center justify-center p-6">
        <MinimalNavigation />
        <div className="text-center">
          <p className="text-xl text-red-500">Error: {error}</p>
          <Button
            variant="outline"
            onClick={() => window.location.reload()}
            className="mt-4 border-gray-700 text-gray-300 hover:bg-gray-800"
          >
            Try Again
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen bg-black text-white relative">
      {/* Blooming Stars Animation */}
      <BloomingStars />
      
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300 relative z-10">
        <div className="container mx-auto space-y-8">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Admin Dashboard</h1>
            <p className="text-gray-400 mt-2">Overview of platform statistics and metrics</p>
          </div>

          {/* Stats Overview Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <Card className="bg-gray-800/70 border-gray-700 backdrop-blur-sm hover:bg-gray-800/90 transition-colors">
              <CardHeader className="pb-2">
                <CardTitle className="text-lg text-gray-200">Total Users</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold text-indigo-400">{stats.totalUsers}</div>
              </CardContent>
            </Card>
            
            <Card className="bg-gray-800/70 border-gray-700 backdrop-blur-sm hover:bg-gray-800/90 transition-colors">
              <CardHeader className="pb-2">
                <CardTitle className="text-lg text-gray-200">Total Articles</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold text-indigo-400">{stats.totalArticles}</div>
              </CardContent>
            </Card>
            
            <Card className="bg-gray-800/70 border-gray-700 backdrop-blur-sm hover:bg-gray-800/90 transition-colors">
              <CardHeader className="pb-2">
                <CardTitle className="text-lg text-gray-200">Courses</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold text-indigo-400">{stats.totalCourses}</div>
              </CardContent>
            </Card>
            
            <Card className="bg-gray-800/70 border-gray-700 backdrop-blur-sm hover:bg-gray-800/90 transition-colors">
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
            <Card className="bg-gray-800/70 border-gray-700 backdrop-blur-sm hover:bg-gray-800/90 transition-colors">
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
                            color: '#e2e8f0',
                            font: {
                              size: 12
                            }
                          }
                        },
                        tooltip: {
                          backgroundColor: 'rgba(15, 23, 42, 0.9)',
                          titleColor: '#e2e8f0',
                          bodyColor: '#e2e8f0',
                          borderColor: '#334155',
                          borderWidth: 1,
                          padding: 10,
                          displayColors: true,
                          callbacks: {
                            label: function(context) {
                              const label = context.label || '';
                              const value = context.raw || 0;
                              // Safely calculate total and percentage
                              let total = 0;
                              if (context.chart.data.datasets[0].data) {
                                total = context.chart.data.datasets[0].data.reduce((a: any, b: any) => (a || 0) + (b || 0), 0);
                              }
                              const percentage = total > 0 ? Math.round((Number(value) / total) * 100) : 0;
                              return `${label}: ${value} (${percentage}%)`;
                            }
                          }
                        }
                      }
                    }} 
                  />
                </div>
              </CardContent>
            </Card>

            {/* Articles Published By Month */}
            <Card className="bg-gray-800/70 border-gray-700 backdrop-blur-sm hover:bg-gray-800/90 transition-colors">
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
                        },
                        tooltip: {
                          backgroundColor: 'rgba(15, 23, 42, 0.9)',
                          titleColor: '#e2e8f0',
                          bodyColor: '#e2e8f0',
                          borderColor: '#334155',
                          borderWidth: 1,
                          padding: 10
                        }
                      }
                    }} 
                  />
                </div>
              </CardContent>
            </Card>
          </div>

        </div>
      </main>
    </div>
  )
}
