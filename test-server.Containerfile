FROM docker.io/caddy:2.9-alpine
    MAINTAINER Linus Kirkwood <linuskirkwood@gmail.com>

COPY test-server.Caddyfile /etc/caddy/Caddyfile
