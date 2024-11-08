## Image Carousel

A JavaFX application that reads all images in the included `src/main/resources` folder
and displays them one by one at 3 second intervals.

To run, use `./gradlew run`. You can also use `./gradlew jlink` to create a script
in the `build/image/bin` folder which you can execute at the command line. Finally, 
you can use `./gradlew jpackage` to create installers for various platforms, but at
the moment the code uses the relative folder for the images so the result may not
find them.

The app does have a "full screen" option, which works, but when you "esc" from full
screen the result may not be sized correctly until you load a new image of a different
size.

The app was created for use in an course called _AI Integration_ at Trinity College
in Hartford, CT (https://trincoll.edu), to be used to AI generated images.

Ken Kousen
