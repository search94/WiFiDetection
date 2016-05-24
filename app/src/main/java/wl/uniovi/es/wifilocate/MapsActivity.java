package wl.uniovi.es.wifilocate;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;

import acceso_datos.DownloadWiFiAPLoader;
import acceso_datos.PositionListener;
import acceso_datos.WiFiAP;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<WiFiAP>>, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;


    FloatingActionButton fab;
    String[] email_dest;
    Marker current;
    //Lista de punto de acceso WiFi
    ArrayList<WiFiAP> list;
    static String key_list_bundle = "wl.uniovi.es.wifilocate.lbundle";
    public static final String key_fav="FAVOURITES";
    static LatLng Gijon = null;
    //Objeto para obtener el posicionamiento
    static PositionListenerMap positionListener;
    //Lista de favoritos
    public static ArrayList<WiFiAP> fav;

    static int REQUEST_SEND = 1;

    //Clase interna,que hereda de PositionListener, la cual devuelve unicamente las coordenadas de la localización
    public class PositionListenerMap extends PositionListener {
        MapsActivity activity;
        public PositionListenerMap(Context c,MapsActivity act) {
            context = c;
            activity=act;
        }

        @Override
        public void onLocationChanged(Location location) {
            activity.MarcaPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }

    }

    //Método para marcar la posción, si se ha obtenido una localización por GPS
    public void MarcaPosition(LatLng pos) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.clear();
        marca(list);
        mMap.setMyLocationEnabled(true);
        mMap.addCircle(new CircleOptions().center(pos).radius(20).fillColor(Color.RED));
    }

    //Clase interna para implementar la ventana de información de los marcadores
    public class InfoWindowMarker implements GoogleMap.InfoWindowAdapter, OnInfoWindowClickListener {

        View v;
        ToggleButton ib;
        public InfoWindowMarker(){
            v=getLayoutInflater().inflate(R.layout.info_window,null);

        }


        @Override
        public View getInfoWindow(Marker marker) {
            return null;

        }

        //Metodo para mostrar la información de la ventana del marcador
        @Override
        public View getInfoContents(Marker marker) {
            final WiFiAP wap=list.get(findIndex(marker));
            TextView name=(TextView)v.findViewById(R.id.tvName);
            TextView coord=(TextView)v.findViewById(R.id.tvCoord);
            ib=(ToggleButton)v.findViewById(R.id.imageStar);
            //Se mira si el punto de acceso esta ya en favoritos o no,
            // y se marca en caso de que esté en favoritos
            if(searchFav(wap))ib.setChecked(true);
            else ib.setChecked(false);
            ib.setPressed(true);
            //Se muestra el nombre y las coordenadas
            name.setText(wap.getNombre());
            coord.setText("" + wap.getLocalizacion().latitude + ";" + wap.getLocalizacion().longitude);
            return v;
        }

        //En caso de pulsar la ventana de información, cambiará el botón de favoritos
        @Override
        public void onInfoWindowClick(Marker marker) {
            final WiFiAP wap=list.get(findIndex(marker));
            ib.callOnClick();
            //Dependiendo de su estado anterior, se marca al estado nuevo y se añade/elimina de la lista de favoritos
            if(!ib.isChecked()){
                ib.setChecked(true);
                fav.add(wap);
            }

            else{
                ib.setChecked(false);
                removeFav(wap);
            }
            ib.setPressed(true);

            //Se reinicia la ventana
            marker.hideInfoWindow();
            marker.showInfoWindow();

        }
    }

    //Método para mostrar en el mapa todos los puntos de acceso encontrados mediante un marcador
    public void marca(ArrayList<WiFiAP> lista){
        if(lista!=null) {
            for (int i = 0; i < lista.size(); i++) {
                try {
                    WiFiAP elm = lista.get(i);
                    // System.out.println(elm.getLocalizacion().latitude + ";" + elm.getLocalizacion().longitude);
                    if (elm.getLocalizacion() != null && elm.getLocalizacion().latitude != 0 && elm.getLocalizacion().longitude != 0) {
                        Marker mapMarker = mMap.addMarker(new MarkerOptions().position(elm.getLocalizacion()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                        mapMarker.setTitle(elm.getNombre());


                    }
                } catch (NullPointerException e) {
                    System.out.println("Null");
                }
            }
        }

    }

    //Método para mostrar el menú correspondiente
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.changemenu, menu);
        return true;
    }

    //Método para detectar la pulsación de una de las opciones del menú
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Opción de la lista
        if(item.getItemId() == R.id.action_change_activity){
            Intent intent=new Intent(MapsActivity.this,ListActivity.class);
            intent.putParcelableArrayListExtra(ListActivity.key_fav_intent,fav);
            startActivity(intent);
            return true;
        }
        else{
            //Opción de configuración
            if(item.getItemId() == R.id.action_settings){
                Intent intent=new Intent(MapsActivity.this,Preferences.class);
                startActivity(intent);
                return true;
            }
            else{
                //Opción de visualización de favoritos
                if(item.getItemId() == R.id.action_fav){
                    Intent intent=new Intent(MapsActivity.this, Favorites.class);
                    intent.putParcelableArrayListExtra(ListActivity.key_fav_intent, fav);
                    startActivity(intent);
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Create");
        Intent intent=getIntent();

        //Si la lista no se ha guardado en el bundle anteriomente se obtiene del loader
        if(savedInstanceState==null)
            getSupportLoaderManager().initLoader(ListActivity.Id, null, this);
        else {
            list = savedInstanceState.getParcelableArrayList(key_list_bundle);
        }

        //Se obtiene la lista de favoritos, si han sido guardados anteriormente en el intent o en el bundle,
        //en caso contrario se crea una nueva lista
        if(intent.hasExtra(ListActivity.key_fav_intent))
            fav=intent.getParcelableArrayListExtra(ListActivity.key_fav_intent);
        else {
            if (savedInstanceState != null)
                fav = savedInstanceState.getParcelableArrayList(key_fav);
            else
                fav = new ArrayList<WiFiAP>(100);
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Se configura el FloatingActionButton(Correo)
        fab=(FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        final Activity c=this;
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current != null)
                    enviar(c, email_dest, "Info WiFiLocate", list.get(findIndex(current)));
            }
        });

        positionListener=new PositionListenerMap(this.getBaseContext(),this);
        activaPositionListener();


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //En caso de hacer click sobre un marcador se hace visible el botón del correo
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                current = null;
                fab.setVisibility(View.INVISIBLE);
            }
        });
        mMap.setOnMarkerClickListener(this);

        //Se configura la ventana de información de los marcadores
        InfoWindowMarker iwm=new InfoWindowMarker();
        mMap.setInfoWindowAdapter(iwm);
        mMap.setOnInfoWindowClickListener(iwm);

        //Se rellena el mapa y se situa en la posicion adecuada (Gijón)
        fillMap();
        double lat=43.5322015;
        double lon=-5.6611195;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lon)).zoom(11).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));






        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


    }

    //Método para rellenar el mapa->Sitúa los marcadores y captura la excepción
    public void fillMap(){
            try {
              marca(list);
            }
            catch(NullPointerException e){

        }
    }

    //Métodos sobrescritos para la gestión del loader
    @Override
    public android.support.v4.content.Loader<ArrayList<WiFiAP>> onCreateLoader(int id, Bundle args) {
        DownloadWiFiAPLoader download=new DownloadWiFiAPLoader(this,ListActivity.URL);
        download.forceLoad();
        return download;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<ArrayList<WiFiAP>> loader, ArrayList<WiFiAP> data) {
        //al acabar la carga de datos se almacenan en la variable list y se rellena el mapa
        list=data;
        fillMap();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<ArrayList<WiFiAP>> loader) {

    }

    //Método para el envio por correo electrónico de la información de un punto de acceso Wifi
    public static void enviar(Activity act,String[] to, String asunto, WiFiAP wap) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setType("text/plain");
        emailIntent.setData(Uri.parse("mailto:srgsearch@hotmail.com"));
        //String[] to = direccionesEmail;
        //String[] cc = copias;
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        String mensaje="Nombre de la red:"+wap.getNombre()+";Distancia"+wap.getDistKm();
        emailIntent.putExtra(Intent.EXTRA_TEXT, mensaje);

        act.startActivityForResult(Intent.createChooser(emailIntent, "Enviar info por email"), REQUEST_SEND);
    }


    //Se hace visible el boton de compartir por correo y se muestra la ventana de información del marcador correspondiente
    @Override
    public boolean onMarkerClick(Marker marker) {
        fab.setVisibility(View.VISIBLE);
        current=marker;
        marker.showInfoWindow();
        return true;

    }

    //Método para localizar un punto de acceso Wifi de la lista dado el marcador del mapa
    public int findIndex(Marker m){
        for(int i=0;i<list.size();i++){
            if(m.getTitle().equals(list.get(i).getNombre()))return i;
        }
        return -1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(key_list_bundle, list);
        outState.putParcelableArrayList(key_fav,fav);
    }

    @Override
    public void onResume(){
        super.onResume();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.onResume();
    }

    //Método para activar el Position Listener
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

    //Método para buscar un punto de acceso wifi en los favoritos
    public static boolean searchFav(WiFiAP w){
        if(fav.size()!=0) {
            for (WiFiAP wiFiAP : fav) {

                if (w!=null && w.getIdentificador() == wiFiAP.getIdentificador())
                    return true;
            }
        }
        return false;
    }

    //Método para eliminar un punto de acceso de los favoritos
    public static void removeFav(WiFiAP w){

        for(int i=0;i<fav.size();i++){
            if(w.getIdentificador()==fav.get(i).getIdentificador())
                fav.remove(i);

        }
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
        editor.putStringSet(ListActivity.key_fav_sp,ids);
        editor.commit();
    }


}
