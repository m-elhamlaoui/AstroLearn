import { ProfileClient } from "@/components/profile/profile-client";
// Remove backend imports, data fetching moves to client

// Define Article type based on DTO (ensure consistency) - Keep for ProfileClient prop type if needed, or move definition
// interface Article { ... } 

// Define UserData type based on UserDTO and fetched lists (align with ProfileClient expectations) - Keep for ProfileClient prop type if needed, or move definition
// interface UserData { ... }
// Removed stray interface definitions below


// Server Component: Renders the Client Component, passing the ID
export default function ProfilePage({ params }: { params: { id: string } }) {
  // The actual data fetching will happen in ProfileClient
  // We just pass the profile ID we want to view
  const profileId = params.id; 

  // Basic validation for ID format if needed, though client will handle 404
  if (!profileId) {
     // Or redirect, or show a specific error component
     return <div>Invalid profile request.</div>; 
  }

  return <ProfileClient profileId={profileId} />;
}
