# ProteinPro Cloud Deployment Guide

This guide covers deploying the **ProteinPro** stack:
1. **Frontend (React SPA)**: Deployed to **Vercel**.
2. **Backend Microservices & Stateful Infrastructure**: Deployed to container cloud providers (**Railway**, **Render**, **Fly.io**, or **Docker on VPS/EC2/Cloud Run**).

---

## 1. Deploying the React Frontend to Vercel

The frontend is a Vite + React SPA located under `./frontend`.

### Method A: Deploy via Vercel CLI

Run the following command from the project root:

```powershell
# Navigate to the frontend directory and deploy
cd frontend
npx vercel
```

When prompted by Vercel:
- **Set up and deploy?**: `y`
- **Which scope?**: Select your account
- **Link to existing project?**: `n`
- **Project name**: `proteinpro-frontend`
- **Directory location**: `./`
- **Build Command**: `npm run build`
- **Output Directory**: `dist`

Set the Environment Variable in Vercel:
- `VITE_API_BASE_URL`: `https://your-api-gateway-url.com` (URL of your deployed API Gateway).

To deploy directly to production:
```powershell
npx vercel --prod
```

### Method B: Deploy via GitHub + Vercel Dashboard

1. Push your repository to GitHub.
2. Log in to [Vercel Dashboard](https://vercel.com/dashboard) and click **Add New Project**.
3. Import your GitHub repository.
4. Select Framework Preset: **Vite**.
5. Set Root Directory: `frontend`.
6. Under **Environment Variables**, add:
   - `VITE_API_BASE_URL`: `https://your-api-gateway-url.com`
7. Click **Deploy**.

`frontend/vercel.json` and root `vercel.json` are pre-configured with client-side SPA route rewrites (`/(.*)` -> `/index.html`).

---

## 2. Deploying Backend Microservices & Infrastructure

Vercel is designed for serverless frontends and cannot run long-running Java JVM Spring Boot containers, Kafka brokers, MySQL, or MongoDB. The backend services should be deployed to container-compatible platforms.

### Option A: Railway (Single-Command Docker Compose Hosting)

[Railway](https://railway.app) supports importing a `docker-compose.yml` directly:

1. Install Railway CLI or connect via GitHub:
   ```powershell
   npx @railway/cli login
   npx @railway/cli init
   npx @railway/cli up
   ```
2. Set Environment Variables in Railway matching `.env.example`:
   - `MYSQL_ROOT_PASSWORD`
   - `JWT_SECRET` (>= 32 chars)
   - `INTERNAL_API_KEY`
   - `AUTH_MYSQL_PASSWORD`
   - `USER_PROFILE_MYSQL_PASSWORD`

### Option B: Cloud VPS / EC2 / DigitalOcean Droplet (Docker Compose)

Deploy the entire stack with Docker Compose on any Linux VM:

```bash
# 1. Clone repository
git clone https://github.com/VishalDaimane/proteinProApp.git
cd proteinProApp

# 2. Configure .env
cp .env.example .env
nano .env

# 3. Build & start all 12 services
docker compose up --build -d

# 4. Verify stack health
docker compose ps
./scripts/e2e.sh
```

---

## 3. Topologies & Endpoint Verification

| Component | Production Port / Path | Public URL / Host |
|---|---:|---|
| **Vite React SPA** | `3000` | `https://proteinpro.vercel.app` |
| **API Gateway** | `8080` | `https://api.yourdomain.com` |
| **Eureka Server** | `8761` | Internal Network |
| **Config Server** | `8888` | Internal Network |
| **Authentication Service** | `8081` | Internal Network |
| **User Profile Service** | `8082` | Internal Network |
| **Protein Service** | `8083` | Internal Network |
| **Bookmark Service** | `8084` | Internal Network |
