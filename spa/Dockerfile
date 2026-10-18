# ===================================================================
# Dockerfile para la SPA en React (Multi-Stage Build con Nginx)
# ===================================================================

# --- Etapa 1: Compilación de los estáticos con Node ---
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci || npm install

COPY . .

# Argumento para inyectar la URL de la API durante la compilación
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# --- Etapa 2: Servidor Web Nginx liviano para producción ---
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
