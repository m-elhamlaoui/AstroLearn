#Use official node image as the build encv
From node:18 as builder

#Specify the working directory
Workdir /app

#copy package files to be able to install modules
Copy package*.json ./

#install Modules
Run npm install --force

#Copy the rest of the project
Copy . .

#Build the project 
Run npm run build

#Use a lighter image for production
From node:18-alpine

#Specify working Dir
Workdir /app

#Copy built app from builder
Copy --from=builder /app ./

#Expose port 3000 for the Next.js app to run on
Expose 3000

#Start the FrontEnd
CMD ["npm", "start"]


