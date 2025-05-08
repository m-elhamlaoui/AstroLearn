// lib/gemini-service.ts

export interface ChatMessage {
  role: "user" | "assistant" | "system";
  content: string;
}

export interface Reference {
  id: number;
  title: string;
  summary: string;
}

export interface ChatResponse {
  message: ChatMessage;
  references: Reference[];
}

// Define the structure for the history items expected by the API route
interface ApiHistoryItem {
  role: "user" | "model"; // Gemini API uses 'model' for assistant
  parts: Array<{ text: string }>;
}

export async function sendChatMessage(message: string, history: ChatMessage[]): Promise<ChatResponse> {
  try {
    // Format the history for the API
    // The Gemini API expects roles 'user' or 'model'
    const limitedHistory = history.slice(-10);
    const formattedHistory: ApiHistoryItem[] = limitedHistory.map(msg => ({
      role: msg.role === 'assistant' ? 'model' : (msg.role === 'system' ? 'user' : msg.role), // Convert 'assistant' to 'model' and 'system' to 'user'
      parts: [{ text: msg.content }]
    }));

    // Make the API request to our Next.js backend
    const response = await fetch(`/api/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        chat: message,
        history: formattedHistory // Send the formatted history
      }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.error || `API request failed with status ${response.status}`);
    }

    const data = await response.json();

    // The API route currently only returns { text: "..." }
    // We will return an empty array for references for now.
    const references: Reference[] = [];

    return {
      message: {
        role: "assistant",
        content: data.text,
      },
      references: references,
    };
  } catch (error) {
    console.error('Error sending chat message:', error);
    // Re-throw the error so the component can handle it
    throw error;
  }
}
