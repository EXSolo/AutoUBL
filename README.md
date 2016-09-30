# AutoUBL Plugin for Sponge

A Sponge Plugin that parses the [/r/ultrahardcore ban list](https://docs.google.com/spreadsheet/ccc?key=0AjACyg1Jc3_GdEhqWU5PTEVHZDVLYWphd2JfaEZXd2c).

The plugin parses the ban list and saves it to a local backup file. 
Each time a player attempts to join, the plugin checks if the player's UUID exists in the backup.
After every server restart, an update is scheduled in order to get the latest version of the UBL.

Besides the updates on restart, a scheduler can be started, which runs in intervals. The duration
between the updates can be changed by adjusting the property `auto-check-interval` in the main config file.

## configs

There are three config files (all are using the HOCON format):

- **autoubl.conf**
  This is the main configuration file where one can adjust settings like the scheduler interval or the ban list URL.
  For more information check the comments in the config file.
- **exempts.conf**
  When one exempts an UBL'd player, the UUID of that player will be stored in this file.
- **bans.conf**
  All the bans loaded from the URL are stored in this file. In order to minimize the file size,
  the duration of the ban is not stored.

## commands

- **/ubl exempt \<name>**
  If you want an UBL'd player to be able to join your server, you need to exempt them first using this command.
- **/ubl unexempt \<name>**
  This command unexempts a player that was exempted before.
- **/ubl reload**
  Despite it not being recommended, it is possible to edit the configuration files instead of using the commands.
  Execute this command to reload exempts and bans from the config.
- **/ubl update**
  Using this command you can reload the UBL from the ban list URL.