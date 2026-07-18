<img width="256" height="256" alt="ic_launcher-playstore" src="https://github.com/user-attachments/assets/8a652f3f-4fd0-4ada-a996-18978ec233d5" />

# TigerDine For Android
TigerDine for Android is a work-in-progress port of [TigerDine](https://github.com/NinjaCheetah/TigerDine), my unofficial app for getting information about dining locations found across the Rochester Institute of Technology campus, to Android. It's here to answer the question "what if TigerDine but for the other major smartphone demographic?"

## Features
TigerDine for Android doesn't have everything that the original iOS version has, but it does have the majority of the important features you'd expect.

Currently working features include:
- See hours and descriptions for all dining locations on campus.
- See daily specials served at each location.
- Check out what visiting chefs are on campus each day and where you can find them.
- View the menu at each location, with support for allergies and other dietary restrictions.
- See the nutrition facts for menu items.

Some other features not listed above are not available yet. These are more complex and require deeper integration with the OS than I've looked into yet. These features include:
- Get visiting chef notifications, so you can be notified day-of when and where your favorite visiting chef will be on campus.
- Home screen widgets so that the hours for your favorite dining locations can be easily accessible on your home screen(?)

## Where do I get it?
TigerDine for Android is available on the Play Store in open testing right now! The Play Store page can be found [here](play.google.com/store/apps/details?id=dev.ninjacheetah.tigerdine).

### What if I can't/don't want to use the Play Store?
Don't worry! Once I get everything sorted and publish the first production build of v1.0.0 to the Play Store, I'll also publish a signed APK for it on a GitHub release. I will also be doing this for each major release going forward. These will be Play Console signed APKs, so they should continue to function on all devices once Google [makes Android a closed platform](https://keepandroidopen.org/).

## Information Sources
- Dining locations, their descriptions, and their opening hours are sourced from the RIT student-run TigerCenter API. [Link](https://tigercenter.rit.edu/)
- Building occupancy information is sourced from the official RIT maps API (this feature is currently unavailable until I hear back from ITS). [Link](https://maps.rit.edu/)
- Menu and nutritional information is sourced from the data provided to FD MealPlanner by RIT Dining through the FD MealPlanner API (but the menus feature hasn't been implemented in TigerDine for Android yet). [Link](https://fdmealplanner.com/)

## Special Thanks
Special thanks to [Maple](https://github.com/DamiDoop) for doing a lot of the UI design! I may have the patience to fight with broken logic, but Maple has far more patience when it comes to fighting with UI stuff. The home screen location list, detail screen, visiting chef screen, etc. wouldn't look nearly as good without her help.
