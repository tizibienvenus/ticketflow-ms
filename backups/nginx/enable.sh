sudo ln -s /etc/nginx/sites-available/4dealx.com /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/api.4dealx.com /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/redirect-http.conf /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/keycloak.4dealx.com /etc/nginx/sites-enabled/

sudo nginx -t
sudo systemctl reload nginx

sudo certbot certonly --nginx -d 4dealx.com -d www.4dealx.com -d dashboard.4dealx.com

sudo certbot certonly --nginx -d api.4dealx.com
sudo certbot certonly --nginx -d keycloak.4dealx.com

sudo nginx -t
sudo systemctl reload nginx

sudo ln -s api.4dealx.com /etc/nginx/sites-enabled/
