=====================
 N64SaveConverterGui
=====================

 Author: Daniel Falk
 Version: 1.0

===========================
 About N64SaveConverterGui
===========================

 N64SaveConverterGui is a tool to convert N64 saves files for transfer between emulators and/or real N64 hardware.

===========================
 N64SaveConverterGui Usage
===========================

To convert:
 * Select a save file to convert
 * Select an input source (from where did this save file originate?)
 * Select an input type
 * Select an output target (what emulator/platform do you want to convert this file to?)
 * Select an output type
 * Click Convert button. If conversion succeeded, a success messsage will appear.
 * Some save files do not need to be converted (i.e. Wii .eep/.mpk to PJ64 .eep/.mpk and vice versa). These scenarios are therefore not a selectable option. But it is still recommended to do a resize to ensure compatibility between emulators

To resize:
 * You can pad/trim your save file to the standard file type size (see below) in order to increase compatibility across emulators
 * Click the pad/trim checkbox
 * Select an input type
 * Click Resize button. If resize succeeded, a success messsage will appear.
  
Lookup list:
 * Check the file extension of the file you selected to determine input/output type. Retroarch is always (.srm)
 * Otherwise, use the Lookup list under the Help menu.

============================
 Info about N64 Saves Files
============================
 
 * The Nintendo 64 has 5 save formats: 4Kbit EEPROM (.eep), 16Kbit EEPROM (.eep), SRAM (.sra), FlashRAM (.fla), and Controller Pak (.mpk)
 * The exact file sizes that N64 hardware generates for these save types are as follows:
   - 4Kbit EEPROM: 512 bytes (.5 kilobytes)
   - 16Kbit EEPROM: 2048 bytes (2 kilobytes)
   - SRAM: 32,768 bytes (32 kilobytes)
   - FlashRAM: 131,072 bytes (128 kilobytes)
   - Controller Pak: 32,768 bytes (32 kilobytes)
 * Different emulators and hardware (Wii64/WiiVC/WiiUVC/PJ64/Mupern64/Retroarch/Everdrive64/etc) all have slightly different format requirements for these save files to be compatible including byteswapping and size requirements.
 * The Retroarch emulator has a unique size for all its games saves (regardless of console): 290 kilobytes as far as I can tell. Native N64 save files are padded to this size. And some of them store the actual save content at strange offsets. (Both front and back padding)
 * This is why I have created a "Standard Size" for these save types. The standard size is set to be the smallest file size to be compatible across all emulators/hardware. 

===============================
 Standard Save File Type Sizes
===============================

 * All N64 Save file conversions will output the converted save file to the standard file type size for maximum compatibility
 * Here are the Standard Save File Sizes:
   - 4Kbit EEPROM (.eep): 2048 bytes (padded to 16Kbit EEPROM size)
   - 16Kbit EEPROM (.eep): 2048 bytes (Same as actual N64 hardware size. Therefore, 4Kbit and 16Kbit EEPs are indistinguishable for the purposes of this application)
   - SRAM (.sra): 32,768 bytes (Same as actual N64 hardware size)
   - FlashRAM (.fla): 131,072 bytes (Same as actual N64 hardware size)
   - Controller Pak (.mpk): 131,072 bytes (Padded to 4x the actual N64 hardware size to simulate 4 paks for the 4 controllers)
   - Retroarch Save (.srm): 290 kilobytes (Same as the real Retroarch save)

==================
 Save File Notes
==================

 * SRAM and FLA saves need to be byteswapped when converting between PC emulators and Wii/WiiU/N64
 * Controller Pak and EEPROM saves do NOT need to be byteswapped when converting between PC Emulators and Wii/WiiU/N64. But good practice to at least do a resize to ensure compatibility.
 * Retroarch sets all save files to 290 kilobytes. The save file content is front-padded at strange offsets for SRAM, FLA, and Controller Pak. EEPROM has no offset, just back-padding only. No worries, this app takes care of all that for you.
 * For Wii Virtual console games:
   - Use Savegame Manager GX or FE100 import/export saves (restore/backup)
   - The save file extracted with Savegame Manager GX and FE100 have no file extension. But that's ok, file extension isn't needed with this app.
   - To import a save to Wii, first use this app and convert the save file (set output target to Wii/WiiU/Everdrive64). Rename to [Wii file name] with no extension. Then use Savegame Manager GX or FE100 to import to Wii
   - To export a save from Wii, first use Savegame Manager GX or FE100 to export the save. Then use this app and convert the save file (set input source to Wii/WiiU/Everdrive64)
after a save file has been converted with this program (output target set to Wii/WiiU/Everdrive64), you must use Savegame Manager GX or FE100 to pack the save to the data.bin to be read by the Wii virtual console games. 
 * For Wii U Virtual console games:
   - Use SaveMii Mod to import/export saves (restore/backup)
   - First start the vc game on the Wii U to create the necessary folder structure on your SD card.
   - Then turn off the WiiU while still in the VC game. (This is needed in order to delete the restore point save state .rs2 file)
   - Put SD card into PC and find the .sav file. This is the real save file (delete any .rs2 file if it appears there).
   - To import a save to WiiU, first use this app and convert the save file (set output target to Wii/WiiU/Everdrive64). Rename to [WiiU file name].sav. Then use SaveMii Mod to import to WiiU
   - To export a save from WiiU, first use SaveMii Mod to export the save. Then use this app and convert the save file (set input source to Wii/WiiU/Everdrive64)
 * I noticed exported SRAM saves from WiiU are 128 kilobytes. Just ignore it. Importing SRAM saves like LoZ:OoT at the standard size of 32 kilobytes works fine.
 * I noticed that SRAM saves created by Everdrive64 use the extention (.srm) instead of (.sra). No big deal. Just don't confuse those (.srm) saves with Retroarch saves which also use the (.srm) extension.
 * Wii64/not64 cannot read 4Kbit EEPROM saves at .5 kilobytes. The 4Kbit EEPROM must be padded to 2 kilobytes (i.e. the same size as 16Kbit EEPROM). Also Wii64 cannot read Controller Pak saves at 32 kilobytes. They must be padded to 128 kilobytes (i.e. 4x the size. It assumes a controller pak for each of the 4 controllers? Just my guess). This app takes care of all that for you.





