package acceso_datos;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.lang.Math;

import wl.uniovi.es.wifilocate.ListFragment;

/**
 * Created by search on 14/03/2016.
 */
//Clase para la obtención de las coordenadas a partir del GPS
public class PositionListener extends AppCompatActivity implements LocationListener {
    public Context context;
    public ArrayList<WiFiAP> list;
    public static final double radioTierra=6378.7;
    ListFragment frag;

    public PositionListener(){}

    public PositionListener(ListFragment lf,Context c,ArrayList<WiFiAP> ls){
        context=c;
        if(ls==null)
            list=new ArrayList<WiFiAP>(10);
        else
            list=ls;
        frag=lf;
    }

    public void  setFrag(ListFragment listFragment){
        frag=listFragment;
    }

    //Método para actualiación de la información de la lista de puntos de acceso una vez se han obtenido las coordenadas
    @Override
    public void onLocationChanged(Location location) {

        if(list!=null) {
            //Se obtiene longitud y latitud
            double latiGPS = location.getLatitude();
            double longiGPS = location.getLongitude();
            WiFiAP currentWap;
            double cosDistance;
            double desvLong;
            //Para cada elemento, se aplica una formula para el cálculo de la distancia en km y se actualiza el mismo punto de acceso
            for (int i = 0; i < list.size(); i++) {
                currentWap = list.get(i);
                desvLong=currentWap.getLocalizacion().longitude - longiGPS;
                cosDistance = (Math.sin(latiGPS) * Math.sin(currentWap.getLocalizacion().latitude)) + (Math.cos(latiGPS) * Math.cos(currentWap.getLocalizacion().latitude) * Math.cos(desvLong));
                currentWap.setDistKm(Math.acos(cosDistance)*111.32);  //111.32 KM/º
            }

            frag.updateAdapter(list);
            Toast.makeText(context, "Obtenida posción", Toast.LENGTH_SHORT).show();
            System.out.println("Latitud:" + location.getLatitude() + "    Longitud"+location.getLongitude());

        }


    }

    // Se llama cuando cambia el estado
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    // Se llama cuando se activa el provider
    @Override
    public void onProviderEnabled(String provider) {
    }

    // Se llama cuando se desactiva el provider
    @Override
    public void onProviderDisabled(String provider) {
    }

    public interface UpdateDistances{
        public void onUpdate(ArrayList<WiFiAP> listWap);
    }

    public void setList(ArrayList<WiFiAP> WAPlist){
        list=WAPlist;
    }
}