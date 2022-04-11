# PhoSort a Photo Sorting Program
My wife and I faced a common problem, we took too many photos of our family.  I always found it annoying with digital cameras you snap 20 pictures but only really want to keep the best 1 or 2.  I made a program that was specifically designed for us to handle the large volume of photos.  We would take up to several hundred pictures each month but really only need to keep a select few.  After about a few months we would upload them to our computer and put rough dates on them.  It may it hard to find the pictures we wanted not to mention 20 pictures of the same thing with only 1 or 2 we want.  So I made a program to help us deal with the images.  
The inspiration was fast sorting so that it was the program is designed to do.  A general walkthrough looks like this:

On first launch it creates a locally stored database.  The user can take a folder of images that was uploaded to the computer and choose it to starting a working sort folder.  The program takes all the images and moves them into a sorting directory organized by date (year, month).  It then displays the working folder on the base scene of the program.  The user can click on the folder they wish to sort to start sorting.

We found the fastest way to choose the best images was to have the thumbnails displayed on one side while a large version of a selected image is displayed on the other.  The user can then select single photos or multiple photos and rank them as follows: Favourite, Like, Keep, Delete.  (Automatically all are set to keep).  Once the entire folder has been sorted click the check mark and it moves the images to the sorted folder.  

To find images the user can go into grab mode where they create a new grab project.  They can select two dates and then a rank (Except deleted).  All the photos between those dates and of that rank will be displayed in the same way as when they were sorted.  The user can then delete photos from the list until they have the desired photos.  Then the checkmark button allows the photos to be copied to any directory for the user.  We personally use this for makign slide shows or printing.  Example, we make a yearly slide show for our kids birthday so I just select the dates from their last birthday to their current.  Pull up all the favourites first.  If I need more I add the likes.  Then I weed down to how many photos I need for the slide show.  The program save the grabs as a project as at times we needed to pull the same set of photos again.  

# Instructions to use phosort
It is an intelij project so open it in inelij and run it or create a jar file.  

# Mode 1: sort
In sort mode choose a folder that contains all your pictures and vidoes from your phone.  
It will go through them grab the date and rotation from the metadata and load into the phosort database (locally stored on your computer).  Oh and create thumbnails.  It detects duplicate photos by name and date.  If the name and date are the same it assumes duplicate photo.  Will not load it into the database and moves it to the dupe folder.
It will first move the photos to the phosort directoyry sorted by /year/month/photo.jpg.  It will move videos to your videos folder sorted the same way.
It creates a working folder that should now appear.  Click on it and you can see all the thumbnails on the left with a large version of the photo on the right.
You can click or navigate with arrows keys.  Shift and ctrl work much the same way windows shift and control do to capture multiple files.  
You can delete, like, favourite photos. 
Delete does not delete the photo right away just removes it from the database.  
Like and Favourite just mark the status of the photo as one or the other.  If it is not deleted, like or favourited it is set to keep as default.
Click the check mark once all the photos are done.  They will be moved to a sorted folder of your choosing in settings.  (default is /users/pictures/sorted)
The deleted photos are still not deleted but moved to a folder called deletes.  My wife was paranoid about deleting a file she did not want to.  

# Mode 2: grab
Grab modes lets you grab photos
Simple change modes and click new project
Give it a name and it makes a new table in the database
The calander choosers are now usable.  Select a from date and a to date.  Next click either like, favourite or keep.  It will pull all photos from the database that fall within those dates and were liked favrouited or kept.  The check mark now lets you make a copy of those photos to any directory you choose.  The originals are never copied and won't be touched to avoid degredation.  THere are a few other small features but you can figure them out.

# Warnings
There are still some issues with the program.  I have not done extensive testing.  It, for the most part, works for my phone, my wifes phone, and our digital camera.  We are both saumsung galaxy users.  It has also only been used on and is intended for Windows.  When we have gotten images from other sources like from visiting santa clause and we pay for the image there have been some display issues.  No photos have been harmed in the making of this program.

I am a self taught programmer.  I know there are likely much better ways to make this program.  My code could be compartementalized better, and my GUI could use work.  So far this program has worked for us to help sort photos.  It is a work in progress where I continue to add things to make our life easier.  

# TODO:
Add feature to migrate the entire database.  Our computer is running out of room and the database stores the file locations so I just cant move them off the computer.

Add Tags to photos.  Likely make them custom built and everytime the user makes a tag have some sort of quick way of assigning them to the photos.  My goal was to ween through photos quickly and I am afraid this may defeat that purpose.

Add ability to zoom in on the displayed photo.  Sometimes nice to see facves up close to determine which pictures are best

Create same sorting criteria for the videos.  Currently they just get sorted by date in the users video folder.  Ability to rank and pull them would also be useful.
