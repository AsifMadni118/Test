# Multi-stage Dockerfile for React Frontend
FROM node:22-alpine AS build
WORKDIR /app
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
COPY package.json pnpm-lock.yaml ./
RUN npm install --global pnpm@11.19.0 \
    && (pnpm install || (pnpm approve-builds --all && pnpm install))
COPY . .
RUN pnpm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
