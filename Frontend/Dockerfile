# Use official Node image as the build environment
FROM node:18-alpine AS builder

# Set working directory
WORKDIR /app

# Copy package files first for better caching
COPY package*.json ./
COPY pnpm-lock.yaml ./

# Install dependencies with legacy peer deps to handle React 19 compatibility
RUN npm install --force

# Copy only necessary files for the build
COPY . .

# Build the Next.js app with standalone output
RUN npm run build

# Production image, copy standalone directory
FROM node:18-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# Copy only necessary files from builder
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

# Expose port 3000
EXPOSE 3000

# Start the application
CMD ["node", "server.js"]


