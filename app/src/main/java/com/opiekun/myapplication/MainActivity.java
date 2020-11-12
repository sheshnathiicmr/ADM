package com.opiekun.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.amazon.device.messaging.ADM;
import com.amazon.device.messaging.development.ADMManifest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    /** Tag for logs. */
    private final static String TAG = "ADMMessenger";

    /** Catches intents sent from the onMessage() callback to update the UI. */
    private BroadcastReceiver msgReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /* Register app with ADM. */
        //register();

        boolean available = false;
        try {
            Class.forName( "com.amazon.device.messaging.ADM" );
            available = true ;
        }
        catch (ClassNotFoundException e) {
            // Handle the exception.
        }
        if(available) {
            ADMManifest.checkManifestAuthoredProperly(getApplicationContext());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /** {@inheritDoc} */
    @Override
    public void onResume()
    {
        /* String to access message field from data JSON. */
        final String msgKey = getString(R.string.json_data_msg_key);

        /* String to access timeStamp field from data JSON. */
        final String timeKey = getString(R.string.json_data_time_key);

        /* Intent action that will be triggered in onMessage() callback. */
        final String intentAction = getString(R.string.intent_msg_action);

        /* Intent category that will be triggered in onMessage() callback. */
        final String msgCategory = getString(R.string.intent_msg_category);

        final Intent nIntent = getIntent();
        if(nIntent != null)
        {
            /* Extract message from the extras in the intent. */
            final String msg = nIntent.getStringExtra(msgKey);
            final String srvTimeStamp = nIntent.getStringExtra(timeKey);

            /* If msgKey and timeKey extras exist then we're coming from clicking a notification intent. */
            if (msg != null && srvTimeStamp != null)
            {
                Log.i(TAG, msg);

                /* Clear notifications if any. */

            }
        }

        /* Listen for messages coming from SampleADMMessageHandler onMessage() callback. */
        msgReceiver = createBroadcastReceiver(msgKey, timeKey);
        final IntentFilter messageIntentFilter= new IntentFilter(intentAction);
        messageIntentFilter.addCategory(msgCategory);
        this.registerReceiver(msgReceiver, messageIntentFilter);
        super.onResume();
    }

    /**
     * Create a {@link BroadcastReceiver} for listening to messages from ADM.
     *
     * @param msgKey String to access message field from data JSON.
     * @param timeKey String to access timeStamp field from data JSON.
     * @return {@link BroadcastReceiver} for listening to messages from ADM.
     */
    private BroadcastReceiver createBroadcastReceiver(final String msgKey,
                                                      final String timeKey)
    {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
        {

            /** {@inheritDoc} */
            @Override
            public void onReceive(final Context context, final Intent broadcastIntent)
            {
                if(broadcastIntent != null){

                    /* Extract message from the extras in the intent. */
                    final String msg = broadcastIntent.getStringExtra(msgKey);
                    final String srvTimeStamp = broadcastIntent.getStringExtra(timeKey);

                    if (msg != null && srvTimeStamp != null)
                    {
                        Log.i(TAG, msg);


                    }


                }
            }
        };
        return broadcastReceiver;
    }

    /** {@inheritDoc} */
    @Override
    public void onPause()
    {
        //this.unregisterReceiver(msgReceiver);
        super.onPause();
    }

    /**
     * Register the app with ADM and send the registration ID to your server
     */
    private void register()
    {
        final ADM adm = new ADM(this);
        if (adm.isSupported())
        {
            if(adm.getRegistrationId() == null)
            {
                adm.startRegister();
            } else {
                /* Send the registration ID for this app instance to your server. */
                /* This is a redundancy since this should already have been performed at registration time from the onRegister() callback */
                /* but we do it because our python server doesn't save registration IDs. */
                final MyServerMsgHandler srv = new MyServerMsgHandler();
                srv.registerAppInstance(getApplicationContext(), adm.getRegistrationId());
            }
        }
    }

    /**
     * Unregister the app with ADM.
     * Your server will get notified from the SampleADMMessageHandler:onUnregistered() callback
     */
    private void unregister()
    {
        final ADM adm = new ADM(this);
        if (adm.isSupported())
        {
            if(adm.getRegistrationId() != null)
            {
                adm.startUnregister();
            }
        }

    }
}
