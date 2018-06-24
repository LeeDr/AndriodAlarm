# AndriodAlarm
Andriod based home alarm system using IOIO board and libraries

This is a work-in-progress.  

The main advantage of using the IOIO board for a home alarm system (over an arduino or Raspberry Pi) is that the attached Android phone can easily send SMS to indicate an alarm has gone off.

This project started a couple of years ago back when most people were using Eclipse to create their Android Java IOIO programs.  I was needed to get a new computer set up to modify this Alarm app, and in doing so I just copied my MainActivity and all my resources into the HelloIOIO example.  

I'm switching to use newer cell phones running later versions of Android so I'm still trying to get some settings right.  Right now I can connect with Bluetooth but not with USB.

TODO:
 - [ ] Get the USB connection working.  Might involve turning off USB Debugging?
 - [ ] Add side gate switch so we get notified if it's left open.
 - [ ] Connect to hard-wired smoke detectors and send SMS in all modes.
 - [ ] Right now this is a silent alarm.  It only sends an SMS message to me.  I have sirens and a relay board so I just need to do the programming and the hardware to hook it up.
 - [ ] If the siren is connected, I also need a way to enable/disable alarm mode remotely.  The easiest way I can think of is to send and "alarm off" text message to the phone and have it checking (check the sending number to make sure it came from me or wife).
 - [ ] Re-write to a client/server(service) architecture so remote phones (on WiFi or 4GLTE) can see the status.
 



 *add some screenshots*
