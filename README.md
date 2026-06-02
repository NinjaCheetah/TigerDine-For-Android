# TigerDine For Android
TigerDine for Android is a heavily work-in-progress port of [TigerDine](https://github.com/NinjaCheetah/TigerDine), my unofficial app for getting information about dining locations found across the Rochester Institute of Technology campus, to Android.

Here to answer the question "what if TigerDine but for the other major smartphone demographic?"

## Features
TigerDine for Android is currently not nearly as feature complete as the original iOS version, but a lot of the core features are up and running!

Currently working features include:
- See hours and descriptions for all dining locations on campus.
- See daily specials served at each location.
- Check out what visiting chefs are on campus each day and where you can find them.

Features outside of that list have NOT been implemented in TigerDine for Android yet. These include:
- View the menu at each location, with support for allergies and other dietary restrictions.
- See the nutrition facts for menu items.
- Get visiting chef notifications, so you can be notified day-of when and where your favorite visiting chef will be on campus.
- Home screen widgets so that the hours for your favorite dining locations can be easily accessible on your home screen(?)

These missing features have been pulled directly from the TigerDine README, and I admittedly haven't looked into how difficult some of these may be. But as long as they remain on this list, I have plans to eventually try and implement them!

## So uh... where do I get it?
That's the fun part, there's not really a distribution channel right now. If you want to preview TigerDine for Android, you can enter Android Studio Hell to compile it yourself, but otherwise you'll have to track me down and get me to build it for you. (Which I will do, if you ask.)

In the near future, I plan to publish some APKs on GitHub releases as previews. Long-term, I intend on getting TigerDine for Android on the Play Store, just like how TigerDine is on the App Store.

## Information Sources
- Dining locations, their descriptions, and their opening hours are sourced from the RIT student-run TigerCenter API. [Link](https://tigercenter.rit.edu/)
- Building occupancy information is sourced from the official RIT maps API (this feature is currently unavailable until I hear back from ITS). [Link](https://maps.rit.edu/)
- Menu and nutritional information is sourced from the data provided to FD MealPlanner by RIT Dining through the FD MealPlanner API (but the menus feature hasn't been implemented in TigerDine for Android yet). [Link](https://fdmealplanner.com/)

## Special Thanks
Special thanks to [Maple](https://github.com/DamiDoop) for helping me with UI design! I may have the patience to fight with broken logic, but Maple has far more patience when it comes to fighting with UI stuff. The home screen location list wouldn't look nearly as good without her help.
