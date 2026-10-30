# Stage 1: Build the React application with Vite
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

# Set API URL empty for relative API requests through Nginx proxy
ENV VITE_API_URL=""
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
