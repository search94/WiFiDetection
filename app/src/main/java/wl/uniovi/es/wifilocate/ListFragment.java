package wl.uniovi.es.wifilocate;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import acceso_datos.APRecycledAdapter;
import acceso_datos.DownloadWiFiAPLoader;
import acceso_datos.WiFiAP;

/**
 * Created by search on 07/03/2016.
 */
//Fragmento que contendrá la lista de los puntos de acceso
public class ListFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<WiFiAP>>{

    public Callbacks callback;
    APRecycledAdapter adaptador;
    RecyclerView recyclerView;
    SeekBar sb;
    EditText etSearch;
    TextView tValue;
    public ArrayList<WiFiAP> list;
    boolean GPSactivated;
    static String key_list_bundle = "wl.uniovi.es.wifilocate.lbundle";

    //Métodos para obtener la lista a partir del Loader
    @Override
    public Loader<ArrayList<WiFiAP>> onCreateLoader(int id, Bundle args) {
        DownloadWiFiAPLoader download = new DownloadWiFiAPLoader(this.getActivity(), ListActivity.URL);
        download.forceLoad();
        return download;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<WiFiAP>> loader, ArrayList<WiFiAP> data) {
        //Una vez obtenida la lista se actualiza el adaptador
        list = data;
        adaptador = new APRecycledAdapter(list, false, this);
        ListActivity.setAdapter(adaptador, recyclerView);
        callback.onListCompleted(data);

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<WiFiAP>> loader) {
        //ListActivity.setAdapter(null, recyclerView);
    }

    //Callbacks
    public interface Callbacks {
        void onWAPSelected(WiFiAP wap); //Llamar a ListActivity para mostrar el detalle de un punto de acceso
        void onListCompleted(ArrayList<WiFiAP> list); //Actualizar la lista cotenida en PositionListener
    }

    public ListFragment() {
    }

    public static ListFragment newInstance(boolean GPS) {

        Bundle args = new Bundle();

        ListFragment fragment = new ListFragment();

        fragment.setArguments(args);
        fragment.GPSactivated = GPS;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

        View rootView;
        rootView = inflater.inflate(R.layout.fragment_list_wap, container, false);
        getActivity().getSupportLoaderManager().initLoader(ListActivity.Id, null, this);
        etSearch=(EditText)rootView.findViewById(R.id.etSearch);
        sb=(SeekBar)rootView.findViewById(R.id.sBar);
        tValue=(TextView)rootView.findViewById(R.id.tvDist);
        sb.setProgress(10000);
        tValue.setText("" + 10000);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tValue.setText("" + progress);
                filtra(etSearch.getText().toString(), progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        final ListFragment lf=this;
        TextWatcher tw=new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lf.filtra(etSearch.getText().toString(),sb.getProgress());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        etSearch.addTextChangedListener(tw);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adaptador = new APRecycledAdapter(new ArrayList<WiFiAP>(2), GPSactivated, this);
        adaptador.addAP(new WiFiAP(0, "Descargando...", "", "Descargando...", "", new LatLng(0, 0)));
        recyclerView.setAdapter(adaptador);


        return rootView;
    }

    public void updateAdapter(ArrayList<WiFiAP> listWap){
        list = listWap;
        adaptador = new APRecycledAdapter(list, false, this);
        adaptador.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement Callbacks");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(key_list_bundle, list);
    }

    public void filtra(String search,int value){
        adaptador.getFilter(search,value);
    }
}
