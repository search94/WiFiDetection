package wl.uniovi.es.wifilocate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashSet;

import acceso_datos.WiFiAP;

/**
 * Created by search on 07/03/2016.
 */
//Actividad para la gestión de la pantalla de detalle de cada Punto de Acceso Wifi
public class ListDetailsActivity extends AppCompatActivity {

    static String key_ap="wl.uniovi.es.wifilocate.WAP";
    static ArrayList<WiFiAP> fav;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_list);

        Intent intent=getIntent();

        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction frag=fragmentManager.beginTransaction();
        //Se obtiene el punto de acceso wifi en cuestión
        WiFiAP w=intent.getParcelableExtra(key_ap);
        //Se obtiene la lista favoritos de intent o bundle, o si crea de nuevo si no existe
        if(intent.hasExtra(ListActivity.key_fav_intent))
            fav=intent.getParcelableArrayListExtra(ListActivity.key_fav_intent);
        else {
            if (savedInstanceState != null)
                fav = savedInstanceState.getParcelableArrayList(ListActivity.key_fav);
            else
                fav = new ArrayList<WiFiAP>(100);
        }

        //Se sustituye el fragmento
        frag.replace(R.id.wap_details_f, ListDetailsFragment.newInstance(w));
        frag.commit();

    }

    //Método para detectar la pulsación del boton home y volver a ListActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                Intent i=new Intent(ListDetailsActivity.this,ListActivity.class);
                //Se le pasa la lista de favoritos
                i.putParcelableArrayListExtra(ListActivity.key_fav_intent,fav);
                startActivity(i);
                break;

        }

        return true;
    }

    //Método para saber si un punto de acceso Wifi está o no en favoritos
    public static boolean searchFav(WiFiAP w){
        if(fav!=null && fav.size()!=0) {
            for (WiFiAP wiFiAP : fav) {

                if (w!=null && w.getIdentificador() == wiFiAP.getIdentificador())
                    return true;
            }
        }
        return false;
    }

    //Método para eliminar un punto de acceso de favoritos
    public static void removeFav(WiFiAP w){

        for(int i=0;i<fav.size();i++){
            if(w.getIdentificador()==fav.get(i).getIdentificador())
                fav.remove(i);

        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ListActivity.key_fav,fav);
    }

    //Se guardan los favoritos en el archivo de preferencias
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs =
                getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        HashSet<String> ids=new HashSet<String>();
        for(int i=0;i<fav.size();i++){
            ids.add(""+fav.get(i).getIdentificador());
        }
        editor.putStringSet(ListActivity.key_fav_sp, ids);
        editor.commit();
    }
}
