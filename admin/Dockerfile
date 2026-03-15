# ============================================================
# 管理端 Dockerfile - 多阶段构建
# ============================================================

# 阶段1: 构建
FROM node:20-alpine AS builder
WORKDIR /app

# 启用 pnpm
RUN corepack enable && corepack prepare pnpm@latest --activate

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
# 使用 Docker 专用环境配置，跳过类型检查直接构建
RUN cp .env.docker .env.production
# 自动修复 ESLint/Prettier 格式问题
RUN npx eslint --fix --ext .js,.ts,.vue ./src || true
RUN npx vite build --mode production

# 阶段2: Nginx 提供静态文件
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
