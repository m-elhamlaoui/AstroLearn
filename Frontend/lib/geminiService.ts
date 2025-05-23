"use client";

import axios from 'axios';

// We'll use the existing Next.js API route for the Gemini API

/**
 * Interface for content validation results
 */
export interface ContentValidationResult {
  isValid: boolean;
  reason: string;
}

/**
 * Validates the content of an article using Gemini AI to ensure it's related to space 
 * exploration and doesn't contain harmful or misleading content
 */
/**
 * Check if content contains space-related keywords
 */
function isSpaceRelated(text: string): boolean {
  const spaceKeywords = [
    'space', 'astronomy', 'planet', 'galaxy', 'star', 'cosmos', 'universe', 
    'nasa', 'esa', 'rocket', 'satellite', 'telescope', 'moon', 'mars', 'jupiter',
    'saturn', 'asteroid', 'comet', 'astronaut', 'orbit', 'solar', 'lunar',
    'nebula', 'constellation', 'gravity', 'earth', 'spacecraft'
  ];
  
  const lowerText = text.toLowerCase();
  return spaceKeywords.some(keyword => lowerText.includes(keyword));
}

/**
 * Basic content validation as a fallback
 */
function performBasicValidation(title: string, summary: string, content: string): ContentValidationResult {
  const combinedText = `${title} ${summary} ${content}`.toLowerCase();
  
  // Check if content is space-related
  if (!isSpaceRelated(combinedText)) {
    return {
      isValid: false,
      reason: 'Content does not appear to be related to space exploration or astronomy.'
    };
  }
  
  // Check for potentially inappropriate content
  const inappropriateWords = ['fuck', 'shit', 'damn', 'bitch', 'ass', 'dick', 'pussy', 'bastard'];
  for (const word of inappropriateWords) {
    if (combinedText.includes(word)) {
      return {
        isValid: false,
        reason: 'Content contains inappropriate language.'
      };
    }
  }
  
  return {
    isValid: true,
    reason: 'Content appears to be related to space exploration and appropriate for the site.'
  };
}

export const validateArticleContent = async (
  title: string, 
  summary: string, 
  content: string
): Promise<ContentValidationResult> => {
  try {
    // First try to validate with API
    try {
      // Make API call to the dedicated validation API route
      const response = await fetch('/api/validate-content', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title,
          summary,
          content,
        }),
      });

      if (response.ok) {
        const result = await response.json();
        if (result && typeof result.isValid === 'boolean' && typeof result.reason === 'string') {
          return result;
        }
      }
      
      // If we get here, API validation didn't work as expected
      console.warn('API validation failed, falling back to basic validation');
    } catch (apiError) {
      console.error('Error with API validation:', apiError);
      // Continue to fallback
    }
    
    // Fallback to basic validation if API fails
    return performBasicValidation(title, summary, content);
    
  } catch (error) {
    console.error('Error validating article content:', error);
    // Ultimate fallback - assume content is valid to not block submissions
    return {
      isValid: true,
      reason: 'Content validation bypassed due to technical issues. Your article has been accepted.'
    };
  }
};
