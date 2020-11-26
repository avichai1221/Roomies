package com.example.rommies;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;

public class afterRegister extends AppCompatActivity
{

    private static final int CONTACT_PICKER_RESULT = 1001;
    private static final int REQUEST_READ_CONTACTS = 1;
    private static final int REQUEST_SEND_SMS = 2;
    private static final String DEBUG_TAG = "0";
    private Button create;
    private Button join;
    private EditText etAprName;
    private Dialog d;
    private Map<String, String> contacts = new HashMap<>();
    private ProgressBar pb;
    private String aprKey = "";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_register);
        join=(Button)findViewById(R.id.join); ///
        create = (Button)findViewById(R.id.create);
        join.setOnClickListener((v)-> {Intent intent=new Intent(this,joinToExistApartment.class); startActivity(intent);}); ///
        create.setOnClickListener((v)->
        {

            d = new Dialog(this);
            d.setContentView(R.layout.create_apartment);
            d.setTitle("Create apartment");
            d.setCancelable(true);
            d.show();
            pb = (ProgressBar)d.findViewById(R.id.progressBar);
            etAprName = (EditText)d.findViewById(R.id.apartmentName);
            ((Button)d.findViewById(R.id.createApr)).setOnClickListener((v1 ->
            {
                pb.setVisibility(View.VISIBLE);
                String aprName = etAprName.getText().toString();
                if(TextUtils.isEmpty(aprName))
                {
                    etAprName.setError("you must fill this field");
                    pb.setVisibility(View.GONE);
                    return;
                }
                if(!contacts.isEmpty())
                {
                    if(checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
                    }
                    else
                    {
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("/Apartments");
                            aprKey = dbRef.push().getKey();
                            dbRef.child("/"+aprKey+"/roommates").child("Uid").setValue(FirebaseAuth.getInstance().getUid());
                            dbRef.child("/"+aprKey+"/Manager").setValue(FirebaseAuth.getInstance().getUid());
                            dbRef.child("/"+aprKey+"/Name").setValue(aprName);
                            sendSms();
                    }
                }
                pb.setVisibility(View.GONE);
                Toast.makeText(this,"Apartment created successfully!",Toast.LENGTH_SHORT).show();

            }));

        });
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void handleAdd(View v)//check whether read contacts permission is enabled
    {
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        else
            LaunchContactPicker();
    }

    //handle the permission request results
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LaunchContactPicker();
                }
                break;
            }
            case REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSms();
                }
                break;
            }

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ResourceType")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)//read the details of selected contact
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case CONTACT_PICKER_RESULT:
                    Cursor phones = null, names = null;
                    String number = "", name = "";
                    try {
                        Uri result = data.getData();
                        Log.v(DEBUG_TAG, "Got a contact result: " + result.toString());
                        String id = result.getLastPathSegment();
                        phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
                        if(phones.moveToFirst())
                        {
                            name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            Log.v(DEBUG_TAG, "Got phone number: " + number);
                            Log.v(DEBUG_TAG, "Got User name: " + name);
                            contacts.put(name, number);

                            LinearLayout ll = d.findViewById(R.id.linearLayout);
                            RelativeLayout rl = new RelativeLayout(d.getContext());
                            //TextView 1
                            TextView tv1 = new TextView(d.getContext());
                            tv1.setText(name);
                            tv1.setTextAppearance(getApplicationContext(),R.style.TextAppearance_AppCompat_Medium);
                            tv1.setId(1);
                            //TextView 2 --> x "button"
                            GradientDrawable shape = new GradientDrawable();
                            shape.setShape(GradientDrawable.RECTANGLE);
                            shape.setColor(Color.RED);
                            shape.setStroke(5, Color.BLACK);
                            shape.setCornerRadius(15);
                            Button bt = new Button(d.getContext(),null,android.R.style.Widget_Material_Light_Button_Small);
                            bt.setText("X");
                            bt.setTextAppearance(getApplicationContext(),R.style.TextAppearance_AppCompat_Medium);
                            bt.setBackgroundColor(Color.RED);
                            bt.setTextColor(Color.WHITE);
                            bt.setBackground(shape);
                            bt.setPadding(10,5,10,5);
                            bt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ll.removeView(rl);
                                }
                            });
                            RelativeLayout.LayoutParams rl_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            RelativeLayout.LayoutParams tv_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            RelativeLayout.LayoutParams bt_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            tv_lp.setMargins(40, 0,0,0);
                            bt_lp.setMargins(30,0,0,0);
                            bt_lp.addRule(RelativeLayout.RIGHT_OF, 1);
                            rl.addView(tv1,tv_lp);
                            rl.addView(bt,bt_lp);
                            ll.addView(rl, rl_lp);
                        } else {
                            Log.w(DEBUG_TAG, "No results");
                        }

                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "Failed to get phone number data", e);
                    } finally {
                        if (phones != null) {
                            phones.close();
                        }
                        if (number.length() == 0) {
                            Toast.makeText(this, "No phone number found for contact.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                break;
            }
        }
        else {
            Log.w(DEBUG_TAG, "Warning: activity result not ok");
        }
    }

    private void LaunchContactPicker()//open contacts
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    private void sendSms()//send sms to selected roommates
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        ArrayList<PendingIntent> sentPI = new ArrayList<>();
        sentPI.add(PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0));
        ArrayList<PendingIntent> deliveredPI = new ArrayList<>();
        deliveredPI.add(PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0));

        for(Map.Entry<String,String> entry: contacts.entrySet())
        {
            String message = "hey " + entry.getKey() +"\n Come and join my apartment.\n Copy and paste this key in your Roomies app.\n "+ aprKey;
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(message);
            sms.sendMultipartTextMessage(entry.getValue(),null, parts,sentPI, deliveredPI );
            Log.v(DEBUG_TAG,message);
        }

    }

}
