# ==========================
# DYNAMIC LINKS HTTPS
# ==========================
server {
    listen 443 ssl;
    server_name links.4dealx.com;
    ssl_certificate /etc/letsencrypt/live/dev.4dealx.com/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/dev.4dealx.com/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    # Fichiers de validation Android & iOS
    location /.well-known/assetlinks.json {
        alias /var/www/links/.well-known/assetlinks.json;
        add_header Content-Type application/json;
        add_header Access-Control-Allow-Origin "*";
        access_log off;
    }

    location /.well-known/apple-app-site-association {
        alias /var/www/links/.well-known/apple-app-site-association;
        add_header Content-Type application/json;
        add_header Access-Control-Allow-Origin "*";
        access_log off;
    }

    # Redirection des Dynamic Links
    location / {
        # Logique de redirection personnalisée
        if ($args ~* "type=post&id=([^&]+)") {
            return 302 https://app.4dealx.com/post/$1;
        }
        if ($args ~* "type=vendor&id=([^&]+)") {
            return 302 https://app.4dealx.com/vendor/$1;
        }
        if ($args ~* "type=category&id=([^&]+)") {
            return 302 https://app.4dealx.com/category/$1;
        }
        if ($args ~* "type=subcategory&id=([^&]+)") {
            return 302 https://app.4dealx.com/subcategory/$1;
        }
        if ($args ~* "type=map&id=([^&]+)") {
            return 302 https://app.4dealx.com/map?postId=$1;
        }

        # Redirection par défaut vers le site web
        return 302 https://4dealx.com;
    }
}

# ==========================
# APP DEEP LINKS HTTPS
# ==========================
server {
    listen 443 ssl;
    server_name app.4dealx.com;
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

    # Page de fallback pour les liens
    location / {
        root /var/www/landing-pages;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
