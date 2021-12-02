package com.arun.cs225.sixfeettracing;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.arun.cs225.sixfeettracing.api.VolleyImpl;
import com.arun.cs225.sixfeettracing.database.DBStore;
import com.arun.cs225.sixfeettracing.firebase.Handler;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class TrackerActivity extends AppCompatActivity {

    public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    public static final String SERVICE_ID = "cs225";
    private String IDENTIFIER = String.valueOf(Math.random());
    private static NotificationManager notificationManager;
    private static NotificationChannel notificationChannel;
    private static NotificationCompat.Builder notification;


    //private static final String BASE_URL = "http://34.220.168.170:8080";
    public static final String BASE_URL = "https://192.168.1.175:8080";
    public static final String UPLOAD_API_URL = "/api/upload";
    public static final String UPDATE_USER_API_URL = "/api/user";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker_activity);

        validate();
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void init() {
        try {
            initNotifications();
            startAdvertising();
            startDiscovering();
            Handler handler = new Handler(getApplicationContext());
            handler.currentRegistrationToken();
        } catch (Exception e) {
            Log.e("Exception", " " + e.toString());
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initNotifications() {
        notificationChannel = new NotificationChannel("CS225",
                "CS225 Channel",
                NotificationManager.IMPORTANCE_HIGH);
        notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notification = new NotificationCompat.Builder(getApplicationContext(),
                "CS225");

    }
    public void validate() {
        try {
            validateBluetoothPermissions();
            validateLocationPermissions();
            validateWifiPermissions();
        } catch (Exception e) {
            Log.e("Exception", " " + e.toString());
        }
    }

    private void validateBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH
                    }, 1);
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN
                    }, 1);
        }
    }

    private void validateLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}
                    , 1);
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    , 1);
        }
    }

    private void validateWifiPermissions() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE}
                    , 1);
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CHANGE_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CHANGE_WIFI_STATE}
                    , 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    /*
    Advertiser Start
     */

    public void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder()
                        .setStrategy(STRATEGY)
                        .build();
        Toast.makeText(getApplicationContext(), "Advertiser Identifer = "+IDENTIFIER,
              Toast.LENGTH_SHORT).show();
        Nearby.getConnectionsClient(getApplicationContext())
                .startAdvertising(
                        IDENTIFIER, SERVICE_ID,
                        advertiserConnectionLifeCycleCallBack, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            System.out.println("Advertising Started");
                            Toast.makeText(this, "Advertising Started" ,
                                    Toast.LENGTH_SHORT).show();
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            Toast.makeText(this, "Error in advertising" ,
                                    Toast.LENGTH_SHORT).show();
                        });
    }

    private final ConnectionLifecycleCallback advertiserConnectionLifeCycleCallBack =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Toast.makeText(getApplicationContext(), "Advertiser - Accepting Connection = "+endpointId,
                            Toast.LENGTH_SHORT).show();
                    Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endpointId,
                            advertiserPayLoadCallBack);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            System.out.println("Advertiser END - Accepted!" + endpointId);
                            Toast.makeText(getApplicationContext(), "Advertiser END - Accepted!",
                                    Toast.LENGTH_SHORT).show();
                            sendPayLoad(endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            System.out.println("Advertiser END - Rejected " + endpointId);
                            Toast.makeText(getApplicationContext(), "Advertiser END - Rejected",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            System.out.println("Advertiser END  - Broke before operation "
                                    + endpointId);
                            Toast.makeText(getApplicationContext(), "Advertiser END  - Broke before operation"+endpointId,
                                    Toast.LENGTH_SHORT).show();
                            Nearby.getConnectionsClient(getApplicationContext())
                                    .disconnectFromEndpoint(endpointId);
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {

                }
            };
    private final PayloadCallback advertiserPayLoadCallBack = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            String receivedBytes = new String(payload.asBytes());

            System.out.println("Advertiser End - Payload Received = " + receivedBytes);
            Toast.makeText(getApplicationContext(), "Advertiser End - Payload Received = " + receivedBytes
                    , Toast.LENGTH_SHORT).show();
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getApplicationContext()
                    , Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext()
                    , Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //Dummy for now
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double latitude = 0d, longitude = 0d;
            if(location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Toast.makeText(getApplicationContext(), "Advertiser End - Lat Long = " + latitude
                                + " and " + longitude
                        , Toast.LENGTH_SHORT).show();
            }
            else{
//                Toast.makeText(getApplicationContext(), "Last known location is null",
//                        Toast.LENGTH_SHORT).show();
                latitude = 33.9737055;
                longitude = -117.3302531;
            }
            constructAndStoreLocalJSON(getApplicationContext(), latitude, longitude, getDeviceName(), receivedBytes);
            dumpIntoServer(getApplicationContext());

            Toast.makeText(getApplicationContext(), "Disconnecting",
                    Toast.LENGTH_SHORT).show();
            Nearby.getConnectionsClient(getApplicationContext())
                    .disconnectFromEndpoint(s);

        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {

        }
    };
    /*
    Advertiser End
     */

    /*
    Discovery Start
     */
    public void startDiscovering() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Toast.makeText(getApplicationContext(), "Discovery - Identifier = "+IDENTIFIER,
                Toast.LENGTH_SHORT).show();
        Nearby.getConnectionsClient(getApplicationContext())
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            System.out.println("Discovery Started");
                            Toast.makeText(getApplicationContext(), "Discovery Started",
                                    Toast.LENGTH_SHORT).show();
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            System.out.println("Error in discovery " + e);
                            Toast.makeText(getApplicationContext(), "Error in discovery",
                                    Toast.LENGTH_SHORT).show();
                        });
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    Nearby.getConnectionsClient(getApplicationContext())
                            .requestConnection(IDENTIFIER, endpointId,
                                    discoveryLifeCycleConnectionCallBack)
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                        System.out.println("Discovery End - Requested a connection" +endpointId);
                                        Toast.makeText(getApplicationContext(), "Discovery End - Requested a connection",
                                                Toast.LENGTH_SHORT).show();
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
                                        System.out.println("Discovery End - Error in requesting connection"+endpointId);
                                        Toast.makeText(getApplicationContext(), "Discovery End - Error in requesting connection"+endpointId,
                                                Toast.LENGTH_SHORT).show();
                                    });
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                }
    };
    private final ConnectionLifecycleCallback discoveryLifeCycleConnectionCallBack =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Toast.makeText(getApplicationContext(), "Discovery - Accepting Connection = "+endpointId,
                            Toast.LENGTH_SHORT).show();
                    Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endpointId,
                            discoveryPayLoadCallBack);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            System.out.println("Discovery End - Accepted!" +endpointId);
                            Toast.makeText(getApplicationContext(), "Discovery End - Accepted!"+endpointId,
                                    Toast.LENGTH_SHORT).show();
                            sendPayLoad(endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            System.out.println("Discovery End - Rejected " +endpointId);
                            Toast.makeText(getApplicationContext(), "Discovery End - Rejected"+endpointId,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            System.out.println("Discovery - Broke before operation " +endpointId);
                            Toast.makeText(getApplicationContext(), "Discovery - Broke before operation"+endpointId,
                                    Toast.LENGTH_SHORT).show();
                            Nearby.getConnectionsClient(getApplicationContext())
                                    .disconnectFromEndpoint(endpointId);
                            break;
                        default:
                            break;
                    }
                }
                @Override
                public void onDisconnected(String endpointId) {

                }
    };
    private final PayloadCallback discoveryPayLoadCallBack = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            String receivedBytes = new String(payload.asBytes());

            System.out.println("Discover End - Payload Received = " + receivedBytes);
            Toast.makeText(getApplicationContext(), "Discover End - Payload Received ="+receivedBytes,
                    Toast.LENGTH_SHORT).show();
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getApplicationContext()
                    , Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext()
                    , Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //Dummy
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double latitude = 0d, longitude = 0d;
            if(location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Toast.makeText(getApplicationContext(), "Discoverer End - Lat Long = " + latitude
                                + " and " + longitude
                        , Toast.LENGTH_SHORT).show();
            }
            else{
                //Toast.makeText(getApplicationContext(), "Last known location is null; Hence using temp values",
                      //  Toast.LENGTH_SHORT).show();
                latitude = 33.9737055;
                longitude = -117.3302531;
            }
            constructAndStoreLocalJSON(getApplicationContext(), latitude, longitude, getDeviceName(), receivedBytes);
            dumpIntoServer(getApplicationContext());

            Toast.makeText(getApplicationContext(), "Disconnecting",
                                Toast.LENGTH_SHORT).show();
                        Nearby.getConnectionsClient(getApplicationContext())
                                .disconnectFromEndpoint(s);
        }
        @Override
        public void onPayloadTransferUpdate(@NonNull String s,
                                            @NonNull PayloadTransferUpdate payloadTransferUpdate) {
        }
    };
    /*
    Discovery End
     */

    /*
    Common for Discovery and Advertiser process
     */
    private void sendPayLoad(String endPointId) {
        String userDeviceName = getDeviceName();
        Payload bytesPayload = Payload.fromBytes(userDeviceName.getBytes());
        Nearby.getConnectionsClient(this).sendPayload(endPointId, bytesPayload).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("Successfully Sent Payload");
                        Toast.makeText(getApplicationContext(), "Successfully Sent Payload",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Error in sending Payload");
            }
        });
    }
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + "_" + model;
    }
    private static void constructAndStoreLocalJSON(Context context, double latitude, double longitude,
                                           String sourceDevice, String destinationDevice) {
        long timeStamp = System.currentTimeMillis();
        try
        {
            JSONObject obj = new JSONObject();
            obj.put("latitude",latitude);
            obj.put("longitude",longitude);
            obj.put("sourceDevice",sourceDevice);
            obj.put("destinationDevice",destinationDevice);
            obj.put("timeStamp", timeStamp);

            DBStore dbStore = new DBStore(context);
            dbStore.write(obj);

            obj = new JSONObject();
            obj.put("latitude",latitude);
            obj.put("longitude",longitude);
            obj.put("sourceDevice",destinationDevice);
            obj.put("destinationDevice",sourceDevice);
            obj.put("timeStamp", timeStamp);
            dbStore.write(obj);
        }catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void dumpIntoServer(Context context) {
        VolleyImpl volley = new VolleyImpl(context, Request.Method.POST,
                BASE_URL + UPLOAD_API_URL);
        DBStore dbStore = new DBStore(context);
        String data = dbStore.read();
        //data = "[{\"latitude\":13.124,\"longitude\":-118.32323,\"sourceDevice\":\"test\",\"destinationDevice\":\"OnePlus_BE2011\",\"timeStamp\":12323242326},{\"latitude\":13.126,\"longitude\":-119.4999,\"sourceDevice\":\"OnePlus_BE2011\",\"destinationDevice\":\"test\",\"timeStamp\":12323242324}]";
        volley.postJSONArray(data);
        dbStore.flush();
    }

    public static void notifyUsers(Map<String, String> remoteMessage) {

        notification.setContentTitle("6FtTracing");
        notification.setContentText("COVID Contact-Trace notification");
        notification.setSmallIcon(R.drawable.ic_launcher_foreground);
        notification.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(remoteMessage.get("message")));
        notificationManager.notify(100,notification.build());
    }

    public void loadStats(View view){
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}