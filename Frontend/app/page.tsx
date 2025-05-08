import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { ArrowRight, Calendar, BookOpen, MessageSquare, FileText, Sparkles } from "lucide-react"
import Link from "next/link"
import { AuthButtons } from "@/components/auth/auth-buttons"
import { BloomingStars } from "@/components/blooming-stars"

export default function HomePage() {
  return (
    <main className="min-h-screen bg-black text-white">
      {/* Hero Section with Blooming Stars Background */}
      <section className="relative h-screen flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0 z-0">
          <BloomingStars />
        </div>

        <div className="container relative z-10 px-4 mx-auto text-center">
          <h1 className="text-5xl md:text-7xl font-bold mb-6 animate-fade-in">
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-blue-400 to-teal-400">
              AstroLearn
            </span>
          </h1>
          <p className="text-xl md:text-2xl mb-8 max-w-3xl mx-auto text-gray-300 animate-fade-in-delay">
            Explore the cosmos through community-driven knowledge, courses, and real-time space mission updates
          </p>
          <div className="flex flex-wrap justify-center gap-4 animate-fade-in-delay-2">
            <Link href="/articles">
              <Button className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-full">
                Explore Articles
              </Button>
            </Link>
            <Link href="/chatbot">
              <Button
                variant="outline"
                className="border-indigo-600 text-indigo-400 hover:bg-indigo-950 px-6 py-3 rounded-full"
              >
                Try AI Chatbot
              </Button>
            </Link>
            <AuthButtons />
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-gradient-to-b from-black to-gray-900">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-16">Discover the Universe Together</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Articles Feature */}
            <FeatureCard
              icon={<FileText className="h-8 w-8 text-purple-400" />}
              title="User-Generated Articles"
              description="Share your knowledge and insights about space exploration. Our AI verification system ensures high-quality, reliable content."
              link="/articles"
            />

            {/* Missions Calendar Feature */}
            <FeatureCard
              icon={<Calendar className="h-8 w-8 text-blue-400" />}
              title="Space Events Calendar"
              description="Stay updated with upcoming space missions, launches, and astronomical events with our interactive calendar."
              link="/missions"
            />

            {/* Chatbot Feature */}
            <FeatureCard
              icon={<MessageSquare className="h-8 w-8 text-teal-400" />}
              title="Space Exploration Chatbot"
              description="Get answers to your space-related questions from our AI assistant trained on space exploration knowledge."
              link="/chatbot"
            />

            {/* Courses Feature */}
            <FeatureCard
              icon={<BookOpen className="h-8 w-8 text-indigo-400" />}
              title="Space Exploration Courses"
              description="Access educational courses on space science with videos, quizzes, and interactive materials."
              link="/courses"
            />

            {/* Recommendation System Feature */}
            <FeatureCard
              icon={<Sparkles className="h-8 w-8 text-pink-400" />}
              title="Article Recommendations"
              description="Discover content tailored to your interests with our AI-based recommendation algorithms."
              link="/articles"
            />

            {/* Community Feature */}
            <FeatureCard
              icon={<ArrowRight className="h-8 w-8 text-amber-400" />}
              title="Join the Community"
              description="Connect with fellow space enthusiasts, discuss articles, and collaborate on space exploration topics."
              link="/profile"
            />
          </div>
        </div>
      </section>

      {/* Featured Articles Section */}
      <section className="py-20 bg-gray-900">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-4">Featured Articles</h2>
          <p className="text-center text-gray-400 mb-12 max-w-2xl mx-auto">
            Explore the latest community-contributed articles about space exploration, verified by our AI system
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Article placeholders - will be replaced with actual content */}
            <ArticleCard
              title="The Future of Mars Colonization"
              excerpt="Exploring the challenges and possibilities of establishing human settlements on the Red Planet."
              author="Space Explorer"
              date="3 days ago"
              link="/articles/1"
            />

            <ArticleCard
              title="James Webb's Latest Discoveries"
              excerpt="A deep dive into the groundbreaking observations from NASA's most powerful space telescope."
              author="Astronomy Enthusiast"
              date="1 week ago"
              link="/articles/2"
            />

            <ArticleCard
              title="Understanding Black Holes"
              excerpt="A comprehensive guide to one of the universe's most mysterious phenomena."
              author="Astrophysics Lover"
              date="2 weeks ago"
              link="/articles/3"
            />
          </div>

          <div className="text-center mt-12">
            <Link href="/articles">
              <Button className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-full">
                View All Articles
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Upcoming Missions Section */}
      <section className="py-20 bg-gradient-to-b from-gray-900 to-black">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-4">Upcoming Space Missions</h2>
          <p className="text-center text-gray-400 mb-12 max-w-2xl mx-auto">
            Stay informed about the latest space exploration endeavors from agencies around the world
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Mission placeholders - will be replaced with actual content */}
            <MissionCard
              title="Artemis III"
              agency="NASA"
              date="2025"
              description="The first crewed lunar landing mission of the Artemis program, aiming to land the first woman and next man on the Moon."
              link="/missions/events/1"
            />

            <MissionCard
              title="Europa Clipper"
              agency="NASA"
              date="October 2024"
              description="Mission to conduct detailed reconnaissance of Jupiter's moon Europa and investigate whether it could harbor conditions suitable for life."
              link="/missions/events/2"
            />

            <MissionCard
              title="ExoMars Rover"
              agency="ESA/Roscosmos"
              date="2028"
              description="Mission to search for signs of past or present life on Mars and investigate the Martian atmosphere."
              link="/missions/events/8"
            />
          </div>

          <div className="text-center mt-12">
            <Link href="/missions">
              <Button className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-full">
                View Full Calendar
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Courses Section */}
      <section className="py-20 bg-black">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-4">Space Exploration Courses</h2>
          <p className="text-center text-gray-400 mb-12 max-w-2xl mx-auto">
            Expand your knowledge with our comprehensive educational content
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Course placeholders - will be replaced with actual content */}
            <CourseCard
              title="Introduction to Astronomy"
              level="Beginner"
              duration="4 weeks"
              description="Learn the fundamentals of astronomy, from celestial objects to the structure of the universe."
              link="/courses/1"
            />

            <CourseCard
              title="Rocket Science Basics"
              level="Intermediate"
              duration="6 weeks"
              description="Understand the principles of rocketry, propulsion systems, and spacecraft design."
              link="/courses/2"
            />

            <CourseCard
              title="Exoplanet Discovery and Analysis"
              level="Advanced"
              duration="8 weeks"
              description="Explore methods for detecting exoplanets and analyzing their potential habitability."
              link="/courses/3"
            />
          </div>

          <div className="text-center mt-12">
            <Link href="/courses">
              <Button className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-full">
                Browse All Courses
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-indigo-900 via-purple-900 to-indigo-900">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-3xl md:text-4xl font-bold mb-6">Ready to Explore the Cosmos?</h2>
          <p className="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
            Join our community of space enthusiasts and start your journey through the universe
          </p>
          <Link href="/articles">
            <Button className="bg-white text-indigo-900 hover:bg-gray-200 px-8 py-4 rounded-full text-lg font-medium">
              Start Exploring
            </Button>
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-12 bg-black border-t border-gray-800">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="mb-6 md:mb-0">
              <h3 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-blue-400 to-teal-400">
                AstroLearn
              </h3>
              <p className="text-gray-400 mt-2">Collaborative Space Exploration Hub</p>
            </div>

            <div className="flex flex-wrap gap-8">
              <div>
                <h4 className="text-white font-medium mb-3">Platform</h4>
                <ul className="space-y-2 text-gray-400">
                  <li>
                    <Link href="/articles" className="hover:text-white transition-colors">
                      Articles
                    </Link>
                  </li>
                  <li>
                    <Link href="/missions" className="hover:text-white transition-colors">
                      Missions
                    </Link>
                  </li>
                  <li>
                    <Link href="/courses" className="hover:text-white transition-colors">
                      Courses
                    </Link>
                  </li>
                  <li>
                    <Link href="/chatbot" className="hover:text-white transition-colors">
                      Chatbot
                    </Link>
                  </li>
                </ul>
              </div>

              <div>
                <h4 className="text-white font-medium mb-3">Company</h4>
                <ul className="space-y-2 text-gray-400">
                  <li>
                    <Link href="#" className="hover:text-white transition-colors">
                      About
                    </Link>
                  </li>
                  <li>
                    <Link href="#" className="hover:text-white transition-colors">
                      Team
                    </Link>
                  </li>
                  <li>
                    <Link href="#" className="hover:text-white transition-colors">
                      Careers
                    </Link>
                  </li>
                  <li>
                    <Link href="#" className="hover:text-white transition-colors">
                      Contact
                    </Link>
                  </li>
                </ul>
              </div>

              <div>
                <h4 className="text-white font-medium mb-3">Legal</h4>
                <ul className="space-y-2 text-gray-400">
                  <li>
                    <Link href="#" className="hover:text-white transition-colors">
                      Privacy
                    </Link>
                  </li>
                  <li>
                    <Link href="#" className="hover:text-white transition-colors">
                      Terms
                    </Link>
                  </li>
                  <li>
                    <Link href="#" className="hover:text-white transition-colors">
                      Cookie Policy
                    </Link>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <div className="mt-12 pt-8 border-t border-gray-800 text-center text-gray-500">
            <p>© {new Date().getFullYear()} AstroLearn. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </main>
  )
}

import React from "react";

// Component for feature cards
interface FeatureCardProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  link: string;
}
function FeatureCard({ icon, title, description, link }: FeatureCardProps) {
  return (
    <Link href={link}>
      <Card className="bg-gray-800 border-gray-700 p-6 rounded-xl hover:bg-gray-750 transition-all duration-300 hover:shadow-lg hover:shadow-indigo-900/20 group h-full">
        <div className="mb-4">{icon}</div>
        <h3 className="text-xl font-bold mb-3 group-hover:text-indigo-400 transition-colors">{title}</h3>
        <p className="text-gray-400">{description}</p>
      </Card>
    </Link>
  )
}

// Component for article cards
interface ArticleCardProps {
  title: string;
  excerpt: string;
  author: string;
  date: string;
  link: string;
}
function ArticleCard({ title, excerpt, author, date, link }: ArticleCardProps) {
  return (
    <Link href={link}>
      <Card className="bg-gray-800 border-gray-700 overflow-hidden rounded-xl hover:shadow-lg hover:shadow-indigo-900/20 transition-all duration-300 h-full">
        <div className="h-48 bg-gray-700 relative">
          {/* Image placeholder */}
          <div className="absolute inset-0 flex items-center justify-center text-gray-500">
            <span>Image Placeholder</span>
          </div>
        </div>
        <div className="p-6">
          <h3 className="text-xl font-bold mb-3 hover:text-indigo-400 transition-colors">{title}</h3>
          <p className="text-gray-400 mb-4">{excerpt}</p>
          <div className="flex justify-between items-center text-sm text-gray-500">
            <span>{author}</span>
            <span>{date}</span>
          </div>
        </div>
      </Card>
    </Link>
  )
}

// Component for mission cards
interface MissionCardProps {
  title: string;
  agency: string;
  date: string;
  description: string;
  link: string;
}
function MissionCard({ title, agency, date, description, link }: MissionCardProps) {
  return (
    <Link href={link}>
      <Card className="bg-gray-800 border-gray-700 p-6 rounded-xl hover:shadow-lg hover:shadow-indigo-900/20 transition-all duration-300 h-full">
        <div className="flex justify-between items-start mb-4">
          <h3 className="text-xl font-bold hover:text-indigo-400 transition-colors">{title}</h3>
          <span className="px-3 py-1 bg-indigo-900 text-indigo-300 rounded-full text-xs">{date}</span>
        </div>
        <p className="text-gray-300 text-sm mb-2">{agency}</p>
        <p className="text-gray-400">{description}</p>
      </Card>
    </Link>
  )
}

// Component for course cards
interface CourseCardProps {
  title: string;
  level: string;
  duration: string;
  description: string;
  link: string;
}
function CourseCard({ title, level, duration, description, link }: CourseCardProps) {
  return (
    <Link href={link}>
      <Card className="bg-gray-800 border-gray-700 overflow-hidden rounded-xl hover:shadow-lg hover:shadow-indigo-900/20 transition-all duration-300 h-full">
        <div className="h-48 bg-gray-700 relative">
          {/* Image placeholder */}
          <div className="absolute inset-0 flex items-center justify-center text-gray-500">
            <span>Course Image Placeholder</span>
          </div>
        </div>
        <div className="p-6">
          <h3 className="text-xl font-bold mb-3 hover:text-indigo-400 transition-colors">{title}</h3>
          <div className="flex gap-3 mb-3">
            <span className="px-3 py-1 bg-indigo-900 text-indigo-300 rounded-full text-xs">{level}</span>
            <span className="px-3 py-1 bg-purple-900 text-purple-300 rounded-full text-xs">{duration}</span>
          </div>
          <p className="text-gray-400">{description}</p>
        </div>
      </Card>
    </Link>
  )
}
