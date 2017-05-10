package nazibulhasanraju.smart_lock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.UUID;


public class ControllerActivity extends AppCompatActivity {

    Button btnDis;
    TextView lumn, lock_state_text;
    ImageView lock_state_img;
    ToggleButton toggleButtonLight1,toggleButtonLight2,toggleButtonFan, toggleButtonDoor;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent newint = getIntent();
        address = newint.getStringExtra(Device_ListActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl

        setContentView(R.layout.activity_controller);


        //call the widgtes

        btnDis = (Button) findViewById(R.id.button4);

        lumn = (TextView) findViewById(R.id.textView2);

        lock_state_img = (ImageView) findViewById(R.id.lock_state_img);

        lock_state_text = (TextView) findViewById(R.id.lock_state_text);


        toggleButtonLight1=(ToggleButton)findViewById(R.id.toggleLight1);
        toggleButtonLight2=(ToggleButton)findViewById(R.id.toggleLight2);
        toggleButtonFan=(ToggleButton)findViewById(R.id.toggleFan);
        toggleButtonDoor=(ToggleButton)findViewById(R.id.toggleDoor);


        setOnToggleBtnClickListener();
        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); //close connection
            }
        });

    }


    private void setOnToggleBtnClickListener() {
        toggleButtonLight1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sendSignal("a");
                    Toast.makeText(getApplicationContext(),"ON",Toast.LENGTH_SHORT).show();
                }
                else {
                    sendSignal("b");
                    Toast.makeText(getApplicationContext(),"OFF",Toast.LENGTH_SHORT).show();
                }
            }
        });

        toggleButtonLight2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sendSignal("c");
                    Toast.makeText(getApplicationContext(),"ON",Toast.LENGTH_SHORT).show();
                }
                else {
                    sendSignal("d");
                    Toast.makeText(getApplicationContext(),"OFF",Toast.LENGTH_SHORT).show();
                }

            }
        });

        toggleButtonFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sendSignal("e");
                    Toast.makeText(getApplicationContext(),"On: ",Toast.LENGTH_SHORT).show();
                }
                else {
                    sendSignal("f");
                    Toast.makeText(getApplicationContext(),"OFF",Toast.LENGTH_SHORT).show();
                }

            }
        });

        toggleButtonDoor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sendSignal("g");
                    lock_state_text.setText("Lock State: UNLOCKED"); // Changes the lock state text
                    lock_state_img.setImageResource(R.drawable.unlocked_icon); //Changes the lock state icon
                    //Toast.makeText(getApplicationContext(),"light 1 State: ",Toast.LENGTH_SHORT).show();
                }
                else {
                    sendSignal("h");
                    lock_state_text.setText("Lock State: LOCKED");
                    lock_state_img.setImageResource(R.drawable.locked_icon);
                }
                //Toast.makeText(getApplicationContext(),"Fan State: "+isChecked,Toast.LENGTH_SHORT);
            }
        });
    }

    private void sendSignal ( String number ) {
        if ( btSocket != null ) {
            try {

                btSocket.getOutputStream().write(number.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }



    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {

            try {

                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout

    }




    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ControllerActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}

