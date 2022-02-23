# PhoSort
Photo Sorting by me
I am a complete ameteur programmer.  I have no idea what I am doing.  My wife and I have millions of photos of our kids and it was getting out of hand.  I decided to try and make our lives easier.  I made a photo sorting program for ourselves to use.  I highly doubt anyone will read this but me.  If you want a photo sorting program that kind of works if you use it just right on a windows pc be my guest.

# Instructions to use phosort
It is an intelij project so open it in inelij or build a jar.  I don't care.
There ar 2 modes
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
There are still some issues with the program.  I have not done extensive testing.  It, for the most part, works for my phone, my wifes phone, and our digital camera.  We are both saumsung galaxy users.  I have no idea if that matters.
So far no photos were harmed in the making of this program.

I am sure the way I programmed this was completley wrong and it could be 1000x better.  

Thanks to all the stack overflow and libraries I used to make this happen.  It was alot.

I do not believe in comments or testing.  Good luck with that.
