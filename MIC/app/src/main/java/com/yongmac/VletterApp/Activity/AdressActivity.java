package com.yongmac.VletterApp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yongmac.VletterApp.Adapter.Adress;
import com.yongmac.VletterApp.Adapter.AdressListAdapter;
import com.yongmac.VletterApp.R;

import java.util.ArrayList;

public class AdressActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adress);


        listView = (ListView) findViewById(R.id.listview1);
        LoadContactsAyscn lca = new LoadContactsAyscn();
        lca.execute();
    }

    class LoadContactsAyscn extends AsyncTask<Void, Void, ArrayList<Adress>>{
        ProgressDialog pd;
        ArrayList<Adress> adresses;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = ProgressDialog.show(AdressActivity.this, "Loding Contacts", "Please Wait");
        }

        @Override
        protected ArrayList<Adress> doInBackground(Void... voids) {
            ArrayList<Adress> contacts = new ArrayList<Adress>();

            Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            while(c.moveToNext()){
                String contactName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ;
                String phNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contacts.add(new Adress(phNumber,contactName));
            }
            c.close();
            return contacts;
        }

        @Override
        protected void onPostExecute(ArrayList<Adress> contacts) {
            super.onPostExecute(contacts);
            pd.cancel();
            final ArrayList<Adress> adresses = contacts;
            AdressListAdapter adapter = new AdressListAdapter(getApplicationContext(), R.layout.layout, adresses );
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), LetterActivity.class);

                    intent.putExtra("name",adresses.get(position).getName());
                    intent.putExtra("phoneNumber",adresses.get(position).getPhoneNumber());

                    startActivity(intent);
                    finish();
                }
            });

        }
    }
}
