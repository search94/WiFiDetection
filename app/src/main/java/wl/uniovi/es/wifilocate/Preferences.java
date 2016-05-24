package wl.uniovi.es.wifilocate;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

//Clase para gestionar las configuraciones de la aplciación
public class Preferences extends PreferenceActivity  implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_SYNC_CONN = "pref_syncConnectionType";
    public static final String key_lang="LANGUAGE";
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            SharedPreferences preference=PreferenceManager.getDefaultSharedPreferences(this);
            preference.registerOnSharedPreferenceChangeListener(this);  //Se registra el listener para gestionar el cambio de alguna configuración
            //Se obtiene la configuración almacenada como archivo de prefencias si lo hubiese
            SharedPreferences prefs =
                    getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

            String pref=prefs.getString(key_lang,"");
            if(pref!=""){
               if(pref.equals("en")) preference.edit().putString(KEY_PREF_SYNC_CONN,getResources().getString(R.string.l_en));
                else preference.edit().putString(KEY_PREF_SYNC_CONN,getResources().getString(R.string.l_es));
        }

        }

    //Se muestra el menu correspondiente a la ventana de configuración
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.settingsmenu, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return super.onOptionsItemSelected(item);
    }

    //Método para detectar el cambio en la configuración
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Se obtiene la única configuración posible (idioma) y se cambia el idioma de la aplicación
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String s = sharedPref.getString(KEY_PREF_SYNC_CONN, "");
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
        Locale l=new Locale(s);
        Locale.setDefault(l);
        Configuration config = new Configuration();
        config.locale = l;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        System.out.println("************************return:" + s);
        // Set summary to be the user-description for the selected value


        //Se guarda la nueva configuración en el archivo de preferencias
        SharedPreferences prefs =
                getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key_lang,s);
        editor.commit();


    }
}
