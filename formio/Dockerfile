FROM node:argon

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Install app dependencies
COPY package.json /usr/src/app/
RUN npm install

# Bundle app source
COPY . /usr/src/app

# ENV Settings
ARG NODE=default
ENV NODE_ENV ${NODE}

EXPOSE 3001
CMD [ "npm", "start" ]