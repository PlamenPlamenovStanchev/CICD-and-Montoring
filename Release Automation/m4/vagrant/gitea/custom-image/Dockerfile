FROM gitea/act_runner:latest

RUN apk add --no-cache nodejs npm git curl libc6-compat && \
      curl -LO https://dl.k8s.io/release/v1.34.4/bin/linux/amd64/kubectl && \
      chmod +x ./kubectl && \
      mv ./kubectl /usr/local/bin/kubectl
