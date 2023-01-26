package fi.lab.myplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SaveFragment saveFragment = new SaveFragment();
        MapFragment mapFragment = new MapFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flFragmentSave,saveFragment).commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flFragmentMap,mapFragment).commit();

        dbHandler = new DBHandler(this);


    }

    @Override
    protected void onDestroy() {
        dbHandler.close();
        super.onDestroy();
    }

}