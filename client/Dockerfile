FROM nginx

# Install Node.js and other dependencies
RUN apt-get update && \
    apt-get -y install curl git ruby ruby-dev rubygems && \
    curl -sL https://deb.nodesource.com/setup_10.x | bash - && \
    apt-get -y install python build-essential nodejs

# Copy angular files
COPY . /tmp
RUN cd /tmp
WORKDIR /tmp

RUN npm install npm -g
RUN npm install -g bower
#RUN bower install -g
RUN npm install -g grunt
RUN npm install --force -g grunt-cli
RUN gem install sass
RUN gem install compass
#RUN npm cache clean
RUN bower install --allow-root --config.interactive=false
RUN npm install

ARG BUILD_PROFILE=build
ENV BUILD_PROFILE ${BUILD_PROFILE}

# Building
RUN grunt ${BUILD_PROFILE} --force
RUN cp -R /tmp/dist/. /usr/share/nginx/html
RUN rm -rf /tmp
WORKDIR /usr/share/nginx/html

# Open port and start nginx
EXPOSE 80
