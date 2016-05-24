package wl.uniovi.es.wifilocate;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.ArraySet;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import acceso_datos.APRecycledAdapter;
import acceso_datos.PositionListener;
import acceso_datos.WiFiAP;

//Clase general para la visualización de la lista de puntos de acceso y su detalle
public class ListActivity extends AppCompatActivity implements ListFragment.Callbacks {

    public boolean GPSactivated = false;
    ListFragment lfrag;
    public static final String URL = "http://datos.gijon.es/doc/ciencia-tecnologia/zona-wifi.json";
    public static int Id = 1;
    public static ArrayList<WiFiAP> fav;
    public static final String key_fav="FAVOURITES";
    public static final String key_fav_intent="FAV";
    public static final String key_fav_sp="SP_FAV";
    PositionListener positionListener;
    boolean tPanes;


    //Método que determina si el GPS está activado o no
    public void GPS() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            GPSactivated = true;
            Toast t = Toast.makeText(this, "Activado", Toast.LENGTH_LONG);
            t.show();
        } else {
            GPSactivated = false;
            Toast t = Toast.makeText(this, "Desactivado", Toast.LENGTH_LONG);
            t.show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Se observa si existe alguna configuración guardada en el archivo de preferencias y se aplica al idioma
        SharedPreferences prefs =
                getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

        String pref=prefs.getString(Preferences.key_lang, "");
        if(pref!=null) {
            Locale l = new Locale(pref);
            Configuration config = new Configuration();
            config.locale = l;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        Intent intent=getIntent();

        setContentView(R.layout.main);

        GPS();

        //Se identifica si estan activados los dos paneles o no
        tPanes = false;
        if (findViewById(R.id.wap_details_cont) != null)
            tPanes = true;


        //Se crea una nueva ListFragment
        lfrag = ((ListFragment) getSupportFragmentManager().findFragmentById(R.id.wap_list_f)).newInstance(GPSactivated);
        //Se crea un nuevo PositionListener para actualizar la lista en base a la posición actual
        positionListener = new PositionListener(lfrag,this, null);
        //Se configura y activa el PositionListener
        positionListener.setFrag(lfrag);
        activaPositionListener();

        //Se obtiene la lista de favoritos, ya sea del intent,bundle,
        // del archivo de prefencias o bien se crea una nueva
        if(intent.hasExtra(ListActivity.key_fav_intent))
            fav=intent.getParcelableArrayListExtra(ListActivity.key_fav_intent);
        else {
            if (savedInstanceState != null)
                fav = savedInstanceState.getParcelableArrayList(key_fav);
            else {
                HashSet<String> h=(HashSet<String>)prefs.getStringSet(key_fav_sp, null);
                if(h!=null) {
                    for(String s:h) {
                        for (WiFiAP w : lfrag.list) {
                            if (w.getIdentificador() ==Long.parseLong(s))
                                fav.add(w);
                        }
                    }
                }
                else{
                    fav=new ArrayList<WiFiAP>(70);
                }
            }
        }




    }
    public void onListCompleted(ArrayList<WiFiAP> list){
        positionListener.setList(list);
    }

    //Se carga el menú correpondiente
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.changemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Opción para cambiar al mapa
        if(item.getItemId() == R.id.action_change_activity){
            Intent intent=new Intent(ListActivity.this,MapsActivity.class);
            intent.putParcelableArrayListExtra(key_fav_intent, fav);
            startActivity(intent);
            return true;
        }
        else{
            //Opción para cambiar al menú de configuración
            if(item.getItemId() == R.id.action_settings){
                Intent intent=new Intent(ListActivity.this,Preferences.class);
                startActivity(intent);
                return true;
            }
            else{
                //Opción para visualizar los favoritos
                if(item.getItemId() == R.id.action_fav){
                    Intent intent=new Intent(ListActivity.this, Favorites.class);
                    intent.putParcelableArrayListExtra(ListActivity.key_fav_intent,fav);
                    startActivity(intent);
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //Método callback de ListFragment
    //Se llama a la actividad de detalle o bien se crea el fragmento correspondiente
    @Override
    public void onWAPSelected(WiFiAP wap) {
        if (!tPanes) {
            Intent intent = new Intent(this, ListDetailsActivity.class);
            intent.putExtra(ListDetailsActivity.key_ap, (Parcelable) wap);
            intent.putParcelableArrayListExtra(ListActivity.key_fav_intent,fav);
            startActivity(intent);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction frag = fragmentManager.beginTransaction();
            frag.replace(R.id.wap_details_cont, ListDetailsFragment.newInstance(wap));
            frag.commit();
        }
    }

    //Método estatico para cambiar un adaptador del recyclerView y notificar el cambio
    public static void setAdapter(APRecycledAdapter apr, RecyclerView rw) {
        rw.setAdapter(apr);
        apr.notifyDataSetChanged();
    }

    //Método para activar el PositionListener
    public void activaPositionListener() {
        //Creación del PositionListener
        // Se debe adquirir una referencia al Location Manager del sistema
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Se obtiene el mejor provider de posición
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

// Se crea un listener de la clase que se va a definir luego


// Se registra el listener con el Location Manager para recibir actualizaciones

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            System.out.println("No hay permiso");
            return;
        }
        else{
            System.out.println("Hay permiso");
        }
        locationManager.requestLocationUpdates(provider, 0, 10, positionListener);

        // Comprobar si se puede obtener la posición ahora mismo
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {

            System.out.println("Latitud:" + location.getLatitude() + "    Longitud"+location.getLongitude());
        } else {
            // Actualmente no se puede obtener la posición
            Toast.makeText(this.getApplicationContext(), "Fallo al obtener la posición",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(key_fav,fav);
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
        editor.putStringSet(key_fav_sp,ids);
        editor.commit();
    }
}


