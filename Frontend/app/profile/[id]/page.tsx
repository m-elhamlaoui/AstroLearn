import { ProfileClient } from "@/components/profile/profile-client";

// Define Article type based on sample data (needed for UserData type)
interface Article {
  id: number;
  title: string;
  summary: string;
  image: string;
  publishDate: string;
  votes: number;
  tags: string[];
}

// Define UserData type explicitly (needed for return type of getUserById and prop type)
interface UserData {
  id: number;
  username: string;
  bio: string;
  profileImage: string;
  coverImage: string;
  xp: number;
  joinDate: string;
  isCurrentUser: boolean;
  articles: Article[];
  upvotedArticles: Article[];
  downvotedArticles: Article[];
}

// Sample user data fetching function - stays in the Server Component
// This would typically fetch from a database or API
const getUserById = (id: string, isCurrentUser: boolean): Promise<UserData> => {
  // Simulate async data fetching
  return new Promise((resolve) => {
    setTimeout(() => {
      // This is placeholder data, replace with actual fetching logic
      resolve({
        id: Number(id) || 1,
        username: "CosmicExplorer",
        bio: "Space enthusiast and amateur astronomer. Fascinated by the mysteries of the cosmos and dedicated to sharing knowledge about our universe.",
        profileImage: "/placeholder.svg?height=200&width=200",
        coverImage: "/placeholder.svg?height=400&width=1200",
        xp: 7500,
        joinDate: "2022-06-15T00:00:00Z",
        isCurrentUser: isCurrentUser,
        articles: [
          { id: 1, title: "The Future of Mars Colonization", summary: "Exploring challenges...", image: "/placeholder.svg", publishDate: "2023-11-15T14:30:00Z", votes: 128, tags: ["Mars"] },
          { id: 2, title: "Understanding Black Holes", summary: "A comprehensive guide...", image: "/placeholder.svg", publishDate: "2023-10-05T16:45:00Z", votes: 189, tags: ["Astrophysics"] },
        ],
        upvotedArticles: [
          { id: 3, title: "James Webb's Latest Discoveries", summary: "A deep dive...", image: "/placeholder.svg", publishDate: "2023-11-10T09:15:00Z", votes: 245, tags: ["JWST"] },
        ],
        downvotedArticles: [
           { id: 5, title: "Conspiracy Theories About Space", summary: "Debunking common misconceptions...", image: "/placeholder.svg", publishDate: "2023-09-18T08:30:00Z", votes: -42, tags: ["Misconceptions"] },
        ],
      } as UserData);
    }, 50);
  });
};

// Server Component: Fetches data and renders the Client Component
export default async function ProfilePage({ params }: { params: { id: string } }) {
  const isCurrentUser = params.id === "me";
  const userIdToFetch = isCurrentUser ? "1" : params.id; // Use default ID "1" if "me"

  try {
    const user: UserData = await getUserById(userIdToFetch, isCurrentUser);

    // Pass the fetched data to the Client Component
    return <ProfileClient initialUser={user} />;

  } catch (error) {
    console.error("Failed to fetch user data:", error);
    // Handle error state, e.g., show an error message or a 404 component
    return <div>Error loading profile. User not found or server error.</div>;
  }
}
