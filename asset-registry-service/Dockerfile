FROM node:20-alpine AS builder

WORKDIR /build

COPY asset-registry-service/package*.json ./
RUN npm install

COPY shared/proto ../shared/proto
COPY asset-registry-service/tsconfig.json ./
COPY asset-registry-service/src ./src

RUN npm run build

# Runtime Stage
FROM node:20-alpine

WORKDIR /app

COPY asset-registry-service/package*.json ./
RUN npm install --only=production

COPY --from=builder /build/dist ./dist
COPY shared/proto /shared/proto

ENV NODE_ENV=production
ENV PORT=4002
ENV GRPC_PORT=9092

EXPOSE 4002 9092

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4002/health || exit 1

CMD ["node", "dist/index.js"]
