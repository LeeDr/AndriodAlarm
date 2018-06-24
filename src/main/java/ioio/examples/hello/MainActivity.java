package ioio.examples.hello;

import java.text.SimpleDateFormat;
import android.telephony.SmsManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.graphics.Color;
import android.view.View;


import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ToggleButton;


/**
 * This is the main activity of the HelloIOIO example application.
 *
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 *
 *
 *
 * https://cloud.google.com/console/project

 Project name = Alarm
 Project ID = alarm-leed

 Project ID: alarm-leed
 Project Number: 185433612197
 *
 *
 *
 * may not need this
 * Client ID 	185433612197.apps.googleusercontent.com
 Email address 	185433612197@developer.gserviceaccount.com
 *
 *
 *
 *
 * but this IS important!
 *
 * Key for server applications API key 	AIzaSyCkljZXYzHHZC2q_qFPqmAB_Q1Uwfl1xA4
 IPs

 0.0.0.0/0

 Activation date 	Feb 25, 2014 7:55 PM
 Activated by 	leed.austex@gmail.com (you)
 *
 *
 *
 *
 */

/**
 * This is the main activity of the HelloIOIO example application.
 *
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class MainActivity extends IOIOActivity {
	private ToggleButton button_;
	private RadioGroup radioGrp;
	private RadioButton selectedRadioButton;

	private  SoundPool mSoundPool;
	private  AudioManager  mAudioManager;
	// private  int mStream1 = 0;
	final static int LOOP_1_TIME = 0;


	final static int FRONT = 0;
	final static int BACK = 1;
	final static int GARAGE = 2;
	final static int TONI = 3;
	final static int LEE = 4;
	int soundRefCount = 0;


	// each door needs a state holder and a timestamp when that state last changed
	class Door {
		public boolean closed;
		public long timestamp;
		public String openStr;
		public String closedStr;
		public int diPin;
		public DigitalInput digIn;
		public TextView textView;
		public int soundOpenId;
		public int soundClosedId;
		protected long announceInterval;

		Door() {
			timestamp = 0L;
			announceInterval = 10L; // MINUTES until we announce that it's still open
		}

		public void setDigitalInput(DigitalInput in) {
			this.digIn = in;
		}

		public void playSound(boolean state){

		}
	}

	class GarageDoor extends Door {
		public int doPin;
		public DigitalOutput digOut;
		public ToggleButton tglBtn;
		public boolean operateDoor;

		public void setDigitalOutput(DigitalOutput openDigitalOutput) {
			// TODO: Auto-generated method stub
			this.digOut = openDigitalOutput;
		}
	}

	final Door[] doorArray = new Door[5];

	final Door frontDoor = new Door();
	final Door backDoor = new Door();
	final Door garageDoor = new Door();
	final GarageDoor toniDoor = new GarageDoor();
	final GarageDoor leeDoor = new GarageDoor();

	int radioSelection;


	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		button_ = (ToggleButton) findViewById(R.id.button);
		radioGrp = (RadioGroup) findViewById(R.id.radioGroup1);

		//set up our audio player
		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

		doorArray[FRONT] = frontDoor;
		doorArray[BACK] = backDoor;
		doorArray[GARAGE] = garageDoor;
		doorArray[TONI] = toniDoor;
		doorArray[LEE] = leeDoor;
		frontDoor.announceInterval = 10000L; // (MINUTES) front door is different than the rest in that we often leave it open

		doorArray[FRONT].closed = true;
		doorArray[FRONT].openStr = "Front Door Open";
		doorArray[FRONT].closedStr = "Front Door Closed";
		doorArray[FRONT].soundOpenId = mSoundPool.load(this, R.raw.fdopen, 1);
		doorArray[FRONT].soundClosedId = mSoundPool.load(this, R.raw.fdclosed, 1);
		doorArray[FRONT].textView = (TextView) findViewById(R.id.frontDoor);
		doorArray[FRONT].diPin = 37;

		doorArray[BACK].closed = true;
		doorArray[BACK].openStr = "Back Door Open";
		doorArray[BACK].closedStr = "Back Door Closed";
		doorArray[BACK].soundOpenId = mSoundPool.load(this, R.raw.bdopen, 1);
		doorArray[BACK].soundClosedId = mSoundPool.load(this, R.raw.bdclosed, 1);
		doorArray[BACK].textView = (TextView) findViewById(R.id.backDoor);
		doorArray[BACK].diPin = 38;

		doorArray[GARAGE].closed = true;
		doorArray[GARAGE].openStr = "Garage Door Open";
		doorArray[GARAGE].closedStr = "Garage Door Closed";
		doorArray[GARAGE].soundOpenId = mSoundPool.load(this, R.raw.gopen, 1);
		doorArray[GARAGE].soundClosedId = mSoundPool.load(this, R.raw.gclosed, 1);
		doorArray[GARAGE].textView = (TextView) findViewById(R.id.garageDoor);
		doorArray[GARAGE].diPin = 39;

		doorArray[TONI].closed = true;
		doorArray[TONI].openStr = "Toni's Garage Open";
		doorArray[TONI].closedStr = "Toni's Garage Closed";
		doorArray[TONI].soundOpenId = mSoundPool.load(this, R.raw.topen, 1);
		doorArray[TONI].soundClosedId = mSoundPool.load(this, R.raw.tclosed, 1);
//        doorArray[TONI].textView = (TextView) findViewById(R.id.tonisGarage);
		doorArray[TONI].diPin = 40;
		((GarageDoor)doorArray[TONI]).doPin = 34;
		((GarageDoor)doorArray[TONI]).tglBtn = (ToggleButton) findViewById(R.id.operateTonisGarage);
		((GarageDoor)doorArray[TONI]).tglBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// set this boolean to true to cause us to operate the Discrete Output in the looper
				((GarageDoor)doorArray[TONI]).operateDoor = true;
				// Perform action on click
				//button1_.setChecked(true);
				// a discrete input should determine the text and state of this toggle button (either Open or Close)
				// and also the color?
				//((GarageDoor)doorArray[TONI]).tglBtn.setText("Toni");
//            	button1_.setChecked(false);
				//text1_.setText("Closed Lee's Garage at " + System.currentTimeMillis());
			}
		});



		doorArray[LEE].closed = true;
		doorArray[LEE].openStr = "Lee's Garage Open";
		doorArray[LEE].closedStr = "Lee's Garage Closed";
		doorArray[LEE].soundOpenId = mSoundPool.load(this, R.raw.lopen, 1);
		doorArray[LEE].soundClosedId = mSoundPool.load(this, R.raw.lclosed, 1);
//        doorArray[LEE].textView = (TextView) findViewById(R.id.leesGarage);
		doorArray[LEE].diPin = 41;
		((GarageDoor)doorArray[LEE]).doPin = 35;
		((GarageDoor)doorArray[LEE]).tglBtn = (ToggleButton) findViewById(R.id.operateLeesGarage);
		((GarageDoor)doorArray[LEE]).tglBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// set this boolean to true to cause us to operate the Discrete Output in the looper
				((GarageDoor)doorArray[LEE]).operateDoor = true;
			}
		});

	}


	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led_;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 *
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 *
		 * @see ioio.lib.util.IOIOLooper#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			showVersions(ioio_, "IOIO connected!");
			led_ = ioio_.openDigitalOutput(0, true);
//			enableUi(true);

			for (int i=0; i < doorArray.length; i++) {
				doorArray[i].setDigitalInput(ioio_.openDigitalInput(doorArray[i].diPin, DigitalInput.Spec.Mode.PULL_UP));
				if (doorArray[i].getClass() == GarageDoor.class) {
					((GarageDoor)doorArray[i]).setDigitalOutput(ioio_.openDigitalOutput(((GarageDoor)doorArray[i]).doPin, false));
				}

			}

		}

		/**
		 * Called repetitively while the IOIO is connected.
		 *
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * @throws InterruptedException
		 * 				When the IOIO thread has been interrupted.
		 *
		 * @see ioio.lib.util.IOIOLooper#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			led_.write(!button_.isChecked());
			Thread.sleep(100);

			int selectedId = radioGrp.getCheckedRadioButtonId();
			selectedRadioButton = (RadioButton) findViewById(selectedId);

			try {
				//setText(frontDoor.digIn.read(), backDoor.digIn.read());
				for (int i=0; i < doorArray.length; i++) {
					updateDoorStatus(i, doorArray[i].digIn.read(), selectedRadioButton.getText());
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int i=0; i < doorArray.length; i++) {
				if (doorArray[i].getClass() == GarageDoor.class) {
					if (((GarageDoor)doorArray[i]).operateDoor) {
						((GarageDoor)doorArray[i]).operateDoor = false;
						((GarageDoor)doorArray[i]).digOut.write(true);
						Thread.sleep(400);
//						sleep(400);  // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< garage door button press time
						((GarageDoor)doorArray[i]).digOut.write(false);
					}
				}
			}
//			sleep(200);
		}



		private void updateDoorStatus(final int index, final boolean bIsOpen, final CharSequence verbosity) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					//float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					//streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
					//mSoundPool.stop(mStream1);


					// if the value is not the same as the previous state or the timestamp was never set (initialize it)
					if ((bIsOpen == doorArray[index].closed) || doorArray[index].timestamp == 0L) {
						doorArray[index].closed = !bIsOpen;
						doorArray[index].timestamp = System.currentTimeMillis();
						// and if that new value is true=open
						if (bIsOpen) {

							// Always adjust the display for state changes
							if (doorArray[index].getClass() == GarageDoor.class) {
								((GarageDoor)doorArray[index]).tglBtn.setText(doorArray[index].openStr);
								((GarageDoor)doorArray[index]).tglBtn.setBackgroundColor(Color.RED);;
							} else {
								doorArray[index].textView.setText(doorArray[index].openStr);
								doorArray[index].textView.setBackgroundColor(Color.RED);
								//doorArray[index].textView.setTextColor(Color.RED);
								//doorArray[index].textView.setDrawingCacheBackgroundColor(Color.RED);

							}

							// sounds based on verbosity setting
							if (verbosity.toString().contains("Announce") || verbosity.toString().contains("Alarm")){
								//mStream1= mSoundPool.play(doorArray[index].soundOpenId, streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
								playSound(doorArray[index].soundOpenId);
							}
							if (verbosity.toString().contains("Alarm")) {
								// if alarm send any "open" messages to text sms
								try {
									SmsManager smsManager = SmsManager.getDefault();
//									smsManager.sendTextMessage("+1<phone number here>", null, doorArray[index].openStr + new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()), null, null);
									Toast.makeText(getApplicationContext(), "SMS Sent!",
											Toast.LENGTH_SHORT).show();
								} catch (Exception e) {
									Toast.makeText(getApplicationContext(),
											e.getLocalizedMessage(),
											Toast.LENGTH_LONG).show();
									e.printStackTrace();
								}
							}
						} else {
							// the new value is false=closed
							doorArray[index].timestamp = System.currentTimeMillis();
							if (doorArray[index].getClass() == GarageDoor.class) {
								((GarageDoor)doorArray[index]).tglBtn.setText(doorArray[index].closedStr);
								((GarageDoor)doorArray[index]).tglBtn.setBackgroundColor(Color.GREEN);;
							} else {
								doorArray[index].textView.setText(doorArray[index].closedStr);
								doorArray[index].textView.setBackgroundColor(Color.GREEN);
								//doorArray[index].textView.setTextColor(Color.GREEN);
							}
							if (verbosity.toString().contains("Announce") || verbosity.toString().contains("Alarm")){
								//mStream1= mSoundPool.play(doorArray[index].soundClosedId, streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
								playSound(doorArray[index].soundClosedId);
							}
						}

					}


					// if a door is open, and we're in "Normal" verbosity,
					//   if and previous state was closed,
					//     set the previous state to open
					//     set the timestamp,
					//   else
					//     if the state hasn't changed in announceInterval MINUTES,
					//        then speak it
					//        and reset the timestmap
					if (bIsOpen && verbosity.toString().contains("Normal")){
						if (doorArray[index].closed) {
							doorArray[index].closed = !bIsOpen;
							doorArray[index].timestamp = System.currentTimeMillis();
						} else {
							if ((System.currentTimeMillis() - doorArray[index].timestamp)/(1000*60) > doorArray[index].announceInterval){
								playSound(doorArray[index].soundOpenId);
								doorArray[index].timestamp = System.currentTimeMillis();
							}
						}
					}


				}
			});
		}

		public void playSound(int soundIndex) {
			//soundRefCount++;
			//sleep((soundRefCount-1) * 2000);
			float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			mSoundPool.play(soundIndex, streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
			//soundRefCount--;
		}




		/**
		 * Called when the IOIO is disconnected.
		 *
		 * @see ioio.lib.util.IOIOLooper#disconnected()
		 */
		@Override
		public void disconnected() {
			enableUi(false);
			toast("IOIO disconnected");
		}

		/**
		 * Called when the IOIO is connected, but has an incompatible firmware version.
		 *
		 * @see ioio.lib.util.IOIOLooper#incompatible(IOIO)
		 */
		@Override
		public void incompatible() {
			showVersions(ioio_, "Incompatible firmware version!");
		}
}

	/**
	 * A method to create our IOIO thread.
	 *
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void showVersions(IOIO ioio, String title) {
		toast(String.format("%s\n" +
				"IOIOLib: %s\n" +
				"Application firmware: %s\n" +
				"Bootloader firmware: %s\n" +
				"Hardware: %s",
				title,
				ioio.getImplVersion(VersionType.IOIOLIB_VER),
				ioio.getImplVersion(VersionType.APP_FIRMWARE_VER),
				ioio.getImplVersion(VersionType.BOOTLOADER_VER),
				ioio.getImplVersion(VersionType.HARDWARE_VER)));
	}

	private void toast(final String message) {
		final Context context = this;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	private int numConnected_ = 0;

	private void enableUi(final boolean enable) {
		// This is slightly trickier than expected to support a multi-IOIO use-case.
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (enable) {
					if (numConnected_++ == 0) {
						button_.setEnabled(true);
					}
				} else {
					if (--numConnected_ == 0) {
						button_.setEnabled(false);
					}
				}
			}
		});
	}
}