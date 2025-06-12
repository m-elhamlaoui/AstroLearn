// lib/message-formatting.tsx
import React, { JSX } from 'react';

/**
 * Parses inline markdown-like formatting (bold and italic) within a text string.
 * **text** becomes <strong>text</strong>
 * *text* becomes <em>text</em>
 * @param text The string to parse.
 * @returns An array of strings and JSX elements.
 */
export function parseInlineReact(text: string): (string | JSX.Element)[] {
    if (typeof text !== 'string') {
        return []; // Return empty array for non-string input
    }

    let key = 0;
    const finalElements: (string | JSX.Element)[] = [];

    // Regex to split by **bold** or *italic*, capturing the delimiters and content.
    // Uses non-greedy match .*?
    // This regex will capture segments like "**content**" or "*content*".
    text.split(/(\*\*.*?\*\*|\*.*?\*)/g).filter(Boolean).forEach(segment => {
        // Check for bold: **content**
        // segment.length >= 4 means at least "**" and "**" for an empty bold, e.g., "****"
        if (segment.startsWith('**') && segment.endsWith('**') && segment.length >= 4) {
            finalElements.push(<strong key={`fmt-bold-${key++}`}>{segment.slice(2, -2)}</strong>);
        }
        // Check for italic: *content*
        // segment.length >= 2 means at least "*" and "*" for an empty italic, e.g., "**"
        else if (segment.startsWith('*') && segment.endsWith('*') && segment.length >= 2) {
            finalElements.push(<em key={`fmt-italic-${key++}`}>{segment.slice(1, -1)}</em>);
        }
        // Plain text segment
        else {
            finalElements.push(segment);
        }
    });
    
    // Handle cases where text was not empty but parsing resulted in no elements or only empty strings
    if (finalElements.length === 0 && text.length > 0) {
        return [text]; // Return original text if no formatting was applied or resulted in empty
    }
    if (finalElements.every(el => typeof el === 'string' && el === '') && text.length > 0) {
        return [text]; // If all parts are empty strings (e.g. from "**" or "****")
    }

    return finalElements;
}

/**
 * Formats the entire message content, handling line breaks, bullet points,
 * indented lines, and inline formatting.
 * @param content The raw message string from the assistant.
 * @returns An array of JSX elements, each representing a formatted line.
 */
export function formatMessageContent(content: string): JSX.Element[] {
  if (typeof content !== 'string') {
    // Fallback for non-string content, though ideally this shouldn't happen.
    return [<div key="error-content" style={{ whiteSpace: 'pre-wrap' }}>Invalid message format</div>];
  }

  const lines = content.split('\n');
  const elements: JSX.Element[] = [];
  let lineKey = 0; // Unique key for each line element

  lines.forEach((line) => {
    let lineElement: JSX.Element | null = null;

    // Preserve empty lines for spacing
    if (line.trim() === "") {
      elements.push(<div key={`empty-${lineKey++}`} style={{ height: '0.75em' }} />);
      return;
    }

    // 1. Check for bullet points (lines starting with "* " or "- ")
    const bulletMatch = line.match(/^(\* |- )(.+)/s); // /s allows . to match newline characters if any
    if (bulletMatch) {
      const bulletContent = bulletMatch[2];
      lineElement = (
        <div key={`line-${lineKey++}`} style={{ display: 'flex', alignItems: 'flex-start', marginLeft: '1.5em', paddingLeft: '0.5em' }}>
          <span style={{ marginRight: '0.5em', lineHeight: 'inherit', userSelect: 'none' }}>•</span>
          <span style={{ whiteSpace: 'pre-wrap' }}>{parseInlineReact(bulletContent)}</span>
        </div>
      );
    } 
    // 2. Check for "tabbed" or indented lines (lines starting with "** ")
    else if (line.startsWith('** ')) {
      const indentedContent = line.substring(3); // Remove the leading "** "
      lineElement = (
        <div key={`line-${lineKey++}`} style={{ marginLeft: '2em', whiteSpace: 'pre-wrap' }}>
          {parseInlineReact(indentedContent)}
        </div>
      );
    } 
    // 3. Regular line (may contain inline bold/italic or be a heading like "**Title:**")
    else {
      lineElement = (
        <div key={`line-${lineKey++}`} style={{ whiteSpace: 'pre-wrap' }}>
          {parseInlineReact(line)}
        </div>
      );
    }
    
    if (lineElement) {
      elements.push(lineElement);
    }
  });

  return elements;
}