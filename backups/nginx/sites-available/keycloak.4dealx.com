server {
    listen 443 ssl http2;
    server_name keycloak.4dealx.com;
    
    ssl_certificate /etc/letsencrypt/live/keycloak.4dealx.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/keycloak.4dealx.com/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    location / {
        proxy_pass http://127.0.0.1:8085;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # Important for Keycloak to detect proxy properly
        proxy_set_header Forwarded "for=$remote_addr;proto=$scheme;host=$host";
        
        # Important for WebSocket connections
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # Buffer settings
        proxy_buffering off;
        proxy_request_buffering off;
        
        # Timeout settings
        proxy_connect_timeout 90s;
        proxy_send_timeout 90s;
        proxy_read_timeout 90s;
    }
}
