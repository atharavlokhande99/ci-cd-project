FROM node:18-alpine
WORKDIR /usr/src/app
COPY app/package*.json ./
RUN npm ci --only=production
COPY app/ .
ENV PORT=3000
EXPOSE 3000
CMD ["node", "index.js"]
