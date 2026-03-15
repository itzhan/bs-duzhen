# ============================================================
# 前端 Dockerfile - 多阶段构建
# ============================================================

# 阶段1: 构建
FROM node:20-alpine AS builder
WORKDIR /app

# 启用 pnpm
RUN corepack enable && corepack prepare pnpm@latest --activate

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
# 跳过 TypeScript 类型检查，直接构建
RUN npx vite build

# 阶段2: Nginx 提供静态文件
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
