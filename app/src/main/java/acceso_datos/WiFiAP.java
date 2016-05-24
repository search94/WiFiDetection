package acceso_datos;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by search on 11/02/2016.
 */
//Clase base para el almacenamiento de la información de puntos de acceso
public class WiFiAP implements Parcelable,Serializable {

    String nombre;
    String send;
    long identificador;
    String direccion;
    String tipo;
    LatLng localizacion;
    double distKm;

    public WiFiAP(int id,String nom,String sen,String dir,String t,LatLng loc){
        nombre=nom;
        send=sen;
        identificador=id;
        tipo=t;
        localizacion=new LatLng(loc.latitude,loc.longitude);
        direccion=dir;
        distKm=-1;

    }
    public WiFiAP(){
        nombre="";
        send="";
        identificador=-1;
        tipo="";
        direccion="";
        localizacion=new LatLng(0,0);
        distKm=-1;

    }
    public WiFiAP(Parcel parcel){readFromParcel(parcel);}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSend() {
        return send;
    }

    public void setSend(String send) {
        this.send = send;
    }

    public long getIdentificador() {
        return identificador;
    }

    public void setIdentificador(long identificador) {
        this.identificador = identificador;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LatLng getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(LatLng localizacion) {
        this.localizacion = localizacion;
    }

    public double getDistKm() {return distKm;}

    public void setDistKm(double distKm) {this.distKm = distKm;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(nombre);
        dest.writeString(send);
        dest.writeLong(identificador);
        dest.writeString(direccion);
        dest.writeString(tipo);
        dest.writeDouble(localizacion.latitude);
        dest.writeDouble(localizacion.longitude);
        dest.writeDouble(distKm);

    }

    public void readFromParcel(Parcel dest){
        nombre=dest.readString();
        send=dest.readString();
        identificador=dest.readLong();
        direccion=dest.readString();
        tipo=dest.readString();
        localizacion=new LatLng(dest.readDouble(),dest.readDouble());
        distKm=dest.readDouble();
    }

    public static final Parcelable.Creator<WiFiAP> CREATOR=new Parcelable.Creator<WiFiAP>(){
        @Override
        public WiFiAP createFromParcel(Parcel in) {
            return new WiFiAP(in);
        }
        public WiFiAP[] newArray(int size){
            return new WiFiAP[size];
        }
    };
}
