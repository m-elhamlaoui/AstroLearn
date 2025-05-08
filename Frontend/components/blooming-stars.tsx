"use client"

import { useEffect, useState, useRef } from "react"

const TOTAL_STARS = 350; // Total number of stars in the static field
const STARS_TO_BLOOM_PER_TICK = 20; // Number of stars to start blooming each tick
const BLOOM_TICK_INTERVAL = 250; // ms, how often we try to make new stars bloom
const MIN_BLOOM_DURATION = 800; // ms
const MAX_BLOOM_DURATION = 2000; // ms
const BASE_STAR_OPACITY = 0.4;
const BLOOMING_STAR_OPACITY = 0.9;
const BASE_STAR_SIZE_MIN = 4; // px for font-size
const BASE_STAR_SIZE_MAX = 8; // px for font-size
const BLOOM_SCALE_MULTIPLIER = 1.8; // How much bigger a star gets when blooming

interface Star {
  id: string;
  x: number; // percentage
  y: number; // percentage
  baseSize: number; // pixels
  rotation: number; // degrees
  isBlooming: boolean;
  currentBloomDuration: number; // ms, duration for the current bloom cycle
}

export function BloomingStars() {
  const [stars, setStars] = useState<Star[]>([]);
  const containerRef = useRef<HTMLDivElement>(null);
  // To store timeout IDs for individual star bloom completion
  const bloomTimeoutsRef = useRef<Map<string, NodeJS.Timeout>>(new Map());

  // Effect for initial star creation
  useEffect(() => {
    const initialStars: Star[] = [];
    for (let i = 0; i < TOTAL_STARS; i++) {
      initialStars.push({
        id: `star-${i}`,
        x: Math.random() * 100,
        y: Math.random() * 100,
        baseSize: BASE_STAR_SIZE_MIN + Math.random() * (BASE_STAR_SIZE_MAX - BASE_STAR_SIZE_MIN),
        rotation: Math.random() * 360,
        isBlooming: false,
        currentBloomDuration: 0,
      });
    }
    setStars(initialStars);
  }, []);

  // Effect for managing blooming logic
  useEffect(() => {
    if (stars.length === 0) return; // Don't run if stars not initialized

    const bloomIntervalId = setInterval(() => {
      setStars(prevStars => {
        const nonBloomingStars = prevStars.filter(star => !star.isBlooming);
        const starsToSelectCount = Math.min(STARS_TO_BLOOM_PER_TICK, nonBloomingStars.length);
        
        // Shuffle non-blooming stars to pick randomly
        for (let i = nonBloomingStars.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1));
          [nonBloomingStars[i], nonBloomingStars[j]] = [nonBloomingStars[j], nonBloomingStars[i]];
        }

        const newlyBloomingStarIds = new Set<string>();
        for (let i = 0; i < starsToSelectCount; i++) {
          newlyBloomingStarIds.add(nonBloomingStars[i].id);
        }

        return prevStars.map(star => {
          if (newlyBloomingStarIds.has(star.id)) {
            // Clear any existing bloom timeout for this star, in case it's re-selected quickly
            const existingTimeoutId = bloomTimeoutsRef.current.get(star.id);
            if (existingTimeoutId) {
              clearTimeout(existingTimeoutId);
            }

            const bloomDuration = MIN_BLOOM_DURATION + Math.random() * (MAX_BLOOM_DURATION - MIN_BLOOM_DURATION);
            
            const timeoutId = setTimeout(() => {
              setStars(currentStars =>
                currentStars.map(s =>
                  s.id === star.id ? { ...s, isBlooming: false } : s
                )
              );
              bloomTimeoutsRef.current.delete(star.id);
            }, bloomDuration);
            bloomTimeoutsRef.current.set(star.id, timeoutId);

            return { ...star, isBlooming: true, currentBloomDuration: bloomDuration };
          }
          return star;
        });
      });
    }, BLOOM_TICK_INTERVAL);

    return () => {
      clearInterval(bloomIntervalId);
      // Clear all active bloom timeouts when component unmounts or effect re-runs
      bloomTimeoutsRef.current.forEach(timeoutId => clearTimeout(timeoutId));
      bloomTimeoutsRef.current.clear();
    };
  }, [stars.length]); // Re-run if TOTAL_STARS changes, though it's const here. Mainly for initial setup.

  return (
    <div
      ref={containerRef}
      className="absolute inset-0 pointer-events-none overflow-hidden z-0"
      aria-hidden="true"
    >
      {stars.map(star => (
        <div
          key={star.id}
          className="star-item text-indigo-400" // Base class for all stars, added text color
          style={{
            left: `${star.x}%`,
            top: `${star.y}%`,
            fontSize: `${star.baseSize}px`, // Use baseSize for font-size
            transform: `rotate(${star.rotation}deg) scale(${star.isBlooming ? BLOOM_SCALE_MULTIPLIER : 1})`,
            opacity: star.isBlooming ? BLOOMING_STAR_OPACITY : BASE_STAR_OPACITY,
            transitionProperty: 'opacity, transform',
            transitionDuration: `${star.isBlooming ? star.currentBloomDuration : MIN_BLOOM_DURATION / 2}ms`, // Faster fade out
            transitionTimingFunction: 'ease-in-out',
          }}
        >
          ✦
        </div>
      ))}
      <style jsx>{`
        .star-item {
          position: absolute;
          transform-origin: center;
          line-height: 1; /* Ensure character is centered well */
        }
      `}</style>
    </div>
  );
}
