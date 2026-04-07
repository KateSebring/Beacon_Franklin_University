# frontend/Dockerfile
FROM node:22.20.0-alpine

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json first (for caching)
COPY package*.json ./

# Install all dependencies including devDependencies
RUN npm install

# Copy the rest of the source code
COPY . .

# Expose Next.js dev server port
EXPOSE 3000

# Enable file watching in Docker
ENV CHOKIDAR_USEPOLLING=true
ENV CHOKIDAR_INTERVAL=100

# Run Next.js dev server
CMD ["npm", "run", "dev"]
