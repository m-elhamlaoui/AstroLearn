// app/api/validate-content/route.ts
import { NextRequest, NextResponse } from 'next/server';
import { GoogleGenerativeAI, HarmCategory, HarmBlockThreshold } from '@google/generative-ai';

const MODEL_NAME = "gemini-1.5-flash";
const API_KEY = process.env.GOOGLE_API_KEY || "";

if (!API_KEY) {
  console.warn("GOOGLE_API_KEY is not set. Please set it in your .env file.");
}

const genAI = new GoogleGenerativeAI(API_KEY);
const model = genAI.getGenerativeModel({ model: MODEL_NAME });

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { title, summary, content } = body;

    if (!content) {
      return NextResponse.json({ error: 'Missing content in request body' }, { status: 400 });
    }

    // Create a prompt that instructs the AI model what to validate
    const prompt = `
      You are an AI validator for a space education website called AstroLearn. 
      Your task is to analyze the following article content and determine if:
      
      1. The content is primarily related to space exploration, astronomy, or related scientific fields
      2. The content does not contain misleading information that contradicts widely accepted scientific knowledge
      3. The content does not contain inappropriate language, hate speech, or inflammatory content
      
      Article Title: ${title}
      
      Article Summary: ${summary}
      
      Article Content: ${content}
      
      Please analyze the content and provide your validation decision.
      You must format your response EXACTLY as a valid JSON object with only these fields:
      {
        "isValid": true or false (boolean, not string),
        "reason": "A brief explanation of your decision"
      }
      
      Important: Your entire response must be only this JSON object, nothing else.
    `;

    const generationConfig = {
      temperature: 0.2, // Lower temperature for more deterministic responses
      topK: 32,
      topP: 0.95,
      maxOutputTokens: 1024,
    };

    const safetySettings = [
      { category: HarmCategory.HARM_CATEGORY_HARASSMENT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE },
      { category: HarmCategory.HARM_CATEGORY_HATE_SPEECH, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE },
      { category: HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE },
      { category: HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, threshold: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE },
    ];

    const chat = model.startChat({
      generationConfig,
      safetySettings,
      history: [],
    });

    const result = await chat.sendMessage(prompt);
    const response = result.response;
    const text = response.text();

    // Try to parse the response as JSON
    try {
      // Look for a JSON object in the response
      const jsonMatch = text.match(/\{[\s\S]*\}/);
      const jsonString = jsonMatch ? jsonMatch[0] : text;
      const validationResult = JSON.parse(jsonString);

      // Ensure the result has the expected format
      if (typeof validationResult.isValid !== 'boolean' || typeof validationResult.reason !== 'string') {
        throw new Error('Invalid validation result format');
      }

      return NextResponse.json(validationResult);
    } catch (parseError) {
      console.error('Failed to parse validation response:', text);
      
      // Fallback logic - make a best effort to determine validity
      if (text.includes('not relevant') || 
          text.includes('unrelated') || 
          text.includes('inappropriate') || 
          text.toLowerCase().includes('invalid')) {
        return NextResponse.json({
          isValid: false,
          reason: 'Content appears to be inappropriate or unrelated to space exploration.'
        });
      } else {
        return NextResponse.json({
          isValid: true,
          reason: 'Content appears to be related to space exploration.'
        });
      }
    }
  } catch (error) {
    console.error('Error in /api/validate-content:', error);
    let errorMessage = 'Internal Server Error';
    if (error instanceof Error) {
      errorMessage = error.message;
    }
    return NextResponse.json({ 
      isValid: false,
      reason: `Failed to validate content: ${errorMessage}`
    }, { status: 500 });
  }
}
