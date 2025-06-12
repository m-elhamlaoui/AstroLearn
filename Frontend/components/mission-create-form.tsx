"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import axiosInstance from "@/lib/axiosInstance"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { format } from "date-fns"
import { Calendar as CalendarIcon, Loader2 } from "lucide-react"
import { useToast } from "@/components/ui/use-toast"
import { cn } from "@/lib/utils"

interface MissionCreateFormProps {
  isOpen: boolean
  onClose: () => void
  onSuccess: () => void
}

export function MissionCreateForm({ isOpen, onClose, onSuccess }: MissionCreateFormProps) {
  const router = useRouter()
  const { toast } = useToast()
  
  const [formData, setFormData] = useState({
    name: "",
    agency: "",
    description: "",
    launchDate: new Date(),
    missionImage: "",
    liveStreamUrl: "",
    status: "UPCOMING"
  })
  
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }
  
  const handleStatusChange = (value: string) => {
    setFormData(prev => ({ ...prev, status: value }))
  }
  
  const handleDateChange = (date: Date | undefined) => {
    if (date) {
      setFormData(prev => ({ ...prev, launchDate: date }))
    }
  }
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)
    setError(null)
    
    try {
      const userId = localStorage.getItem("userId")
      if (!userId) {
        throw new Error("User ID not found. Please log in again.")
      }
      
      // Format the date to ISO string
      const formattedData = {
        ...formData,
        launchDate: formData.launchDate.toISOString()
      }
      
      // Send the mission creation request
      const response = await axiosInstance.post(`/missions?creatorUserId=${userId}`, formattedData)
      
      toast({
        title: "Mission Created",
        description: "Your space mission has been successfully created.",
        duration: 5000
      })
      
      onSuccess()
      onClose()
    } catch (err: any) {
      console.error("Failed to create mission:", err)
      setError(err.response?.data?.message || err.message || "Failed to create mission. Please try again.")
      
      toast({
        variant: "destructive",
        title: "Error",
        description: "Failed to create mission. Please try again.",
        duration: 5000
      })
    } finally {
      setIsSubmitting(false)
    }
  }
  
  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="bg-gray-900 border-gray-800 text-white max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Create New Space Mission</DialogTitle>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-6 py-4">
          {error && (
            <div className="bg-red-900/30 border border-red-700 text-red-300 p-3 rounded-md">
              {error}
            </div>
          )}
          
          <div className="space-y-4">
            <div>
              <label htmlFor="name" className="block text-sm font-medium mb-1">
                Mission Name <span className="text-red-500">*</span>
              </label>
              <Input
                id="name"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="Enter mission name"
                className="bg-gray-800 border-gray-700"
                required
              />
            </div>
            
            <div>
              <label htmlFor="agency" className="block text-sm font-medium mb-1">
                Agency <span className="text-red-500">*</span>
              </label>
              <Input
                id="agency"
                name="agency"
                value={formData.agency}
                onChange={handleInputChange}
                placeholder="NASA, SpaceX, ESA, etc."
                className="bg-gray-800 border-gray-700"
                required
              />
            </div>
            
            <div>
              <label htmlFor="description" className="block text-sm font-medium mb-1">
                Description <span className="text-red-500">*</span>
              </label>
              <Textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                placeholder="Describe the mission objectives and details"
                className="bg-gray-800 border-gray-700 min-h-[120px]"
                required
              />
            </div>
            
            <div>
              <label htmlFor="launchDate" className="block text-sm font-medium mb-1">
                Launch Date <span className="text-red-500">*</span>
              </label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn(
                      "w-full justify-start text-left font-normal bg-gray-800 border-gray-700",
                      !formData.launchDate && "text-gray-400"
                    )}
                  >
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {formData.launchDate ? format(formData.launchDate, "PPP") : "Select a date"}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0 bg-gray-800 border-gray-700">
                  <Calendar
                    mode="single"
                    selected={formData.launchDate}
                    onSelect={handleDateChange}
                    initialFocus
                    className="bg-gray-800 text-white"
                  />
                </PopoverContent>
              </Popover>
            </div>
            
            <div>
              <label htmlFor="status" className="block text-sm font-medium mb-1">
                Status <span className="text-red-500">*</span>
              </label>
              <Select value={formData.status} onValueChange={handleStatusChange}>
                <SelectTrigger className="w-full bg-gray-800 border-gray-700">
                  <SelectValue placeholder="Select status" />
                </SelectTrigger>
                <SelectContent className="bg-gray-800 border-gray-700">
                  <SelectItem value="UPCOMING">Upcoming</SelectItem>
                  <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
                  <SelectItem value="COMPLETED">Completed</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <div>
              <label htmlFor="missionImage" className="block text-sm font-medium mb-1">
                Mission Image URL
              </label>
              <Input
                id="missionImage"
                name="missionImage"
                value={formData.missionImage}
                onChange={handleInputChange}
                placeholder="https://example.com/image.jpg"
                className="bg-gray-800 border-gray-700"
              />
              <p className="text-xs text-gray-400 mt-1">
                Provide a URL to an image representing this mission
              </p>
            </div>
            
            <div>
              <label htmlFor="liveStreamUrl" className="block text-sm font-medium mb-1">
                Live Stream URL
              </label>
              <Input
                id="liveStreamUrl"
                name="liveStreamUrl"
                value={formData.liveStreamUrl}
                onChange={handleInputChange}
                placeholder="https://example.com/livestream"
                className="bg-gray-800 border-gray-700"
              />
            </div>
          </div>
          
          <div className="flex justify-end gap-3 pt-4">
            <Button 
              type="button" 
              variant="outline" 
              onClick={onClose}
              className="border-gray-700 text-gray-300 hover:bg-gray-800"
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button 
              type="submit" 
              className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Creating...
                </>
              ) : (
                "Create Mission"
              )}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}
