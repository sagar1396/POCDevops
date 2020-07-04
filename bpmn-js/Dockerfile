FROM nginx

# Install Node.js and other dependencies
RUN apt-get update && \
    apt-get -y install curl git ruby && \
    curl -sL https://deb.nodesource.com/setup_10.x | bash - && \
    apt-get -y install nodejs

# Copy angular files
COPY . /tmp
RUN cd /tmp
WORKDIR /tmp

RUN npm install
RUN npm install -g grunt
RUN npm install --force -g grunt-cli

# ENV
ARG BUILD_PROFILE=build
ENV BUILD_PROFILE ${BUILD_PROFILE}

# Building
RUN grunt ${BUILD_PROFILE} --force
RUN cp -R /tmp/dist/. /usr/share/nginx/html
RUN rm -rf /tmp
WORKDIR /usr/share/nginx/html

# Open port and start nginx
EXPOSE 80
