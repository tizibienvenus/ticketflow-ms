# ==========================
# FRONTEND (React) HTTPS
# ==========================
server {
    listen 443 ssl;
    server_name 4dealx.com www.4dealx.com;
    
    client_max_body_size 64M;
    add_header Strict-Transport-Security "max-age=31536000";

    # Augmenter la taille des headers
    large_client_header_buffers 8 64k;
    client_header_buffer_size 32k;
    ssl_certificate /etc/letsencrypt/live/dev.4dealx.com/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/dev.4dealx.com/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    # Fichiers de validation
    location /.well-known/assetlinks.json {
        alias /var/www/links/.well-known/assetlinks.json;
        add_header Content-Type application/json;
        access_log off;
    }

    location /.well-known/apple-app-site-association {
        alias /var/www/links/.well-known/apple-app-site-association;
        add_header Content-Type application/json;
        access_log off;
    }

    location / {
        proxy_pass http://localhost:3000/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_set_header Authorization $http_authorization;
        proxy_cache_bypass $http_upgrade;

        # Buffers côté proxy
        proxy_buffer_size 256k;
        proxy_buffers 4 256k;
        proxy_busy_buffers_size 256k;
    }

}
