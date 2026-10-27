
package com.example.scretcalculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.provider.MediaStore;
import android.media.MediaPlayer;
import android.location.Location;
import android.location.LocationManager;
import android.content.pm.PackageManager;
import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends Activity
        implements SensorEventListener {

    EditText angka1, angka2;
    TextView hasil;

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private TextView sensorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        angka1 = findViewById(R.id.angka1);
        angka2 = findViewById(R.id.angka2);
        hasil = findViewById(R.id.hasil);

        Button tambah = findViewById(R.id.tambah);
        Button kurang = findViewById(R.id.kurang);
        Button kali = findViewById(R.id.kali);
        Button bagi = findViewById(R.id.bagi);

        tambah.setOnClickListener(v -> hitung("+"));
        kurang.setOnClickListener(v -> hitung("-"));
        kali.setOnClickListener(v -> hitung("*"));
        bagi.setOnClickListener(v -> hitung("/"));

        sensorManager =
                (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        gyroscope = sensorManager.getDefaultSensor(
                Sensor.TYPE_GYROSCOPE);
    }

    private void hitung(String operator) {
        String a = angka1.getText().toString().trim();
        String b = angka2.getText().toString().trim();

        if (a.isEmpty() || b.isEmpty()) {
            Toast.makeText(this, "Masukkan kedua angka!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double x = Double.parseDouble(a);
            double y = Double.parseDouble(b);
            double nilai;

            switch (operator) {
                case "+":
                    nilai = x + y;
                    break;
                case "-":
                    nilai = x - y;
                    break;
                case "*":
                    nilai = x * y;
                    break;
                case "/":
                    if (y == 0) {
                        Toast.makeText(this, "Tidak bisa dibagi nol!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    nilai = x / y;
                    break;
                default:
                    return;
            }

            hasil.setText("Hasil: " + nilai);
            cekRahasia(nilai);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Angka tidak valid!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void cekRahasia(double nilai) {
        if (nilai == 0) {
            bukaKamera();
        } else if (nilai == 3) {
            putarAudio();
        } else if (nilai == 8) {
            bukaGambar();
        } else if (nilai == 7) {
            bukaGPS();
        } else if (nilai == 9) {
            bukaSensor();
        }
    }

    private void bukaKamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Aplikasi kamera tidak tersedia",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void putarAudio() {
        MediaPlayer player = MediaPlayer.create(this, R.raw.suara);

        if (player != null) {
            player.setOnCompletionListener(mp -> mp.release());
            player.start();
        } else {
            Toast.makeText(this, "Audio gagal dimuat",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void bukaGambar() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivity(intent);
    }

    private void bukaGPS() {
        LocationManager lm =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 100);
            return;
        }

        try {
            Location lokasi = lm.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);

            if (lokasi != null) {
                String pesan = "Latitude: " + lokasi.getLatitude()
                        + "\nLongitude: " + lokasi.getLongitude();

                new AlertDialog.Builder(this)
                        .setTitle("Lokasi GPS")
                        .setMessage(pesan)
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                Toast.makeText(this,
                        "Lokasi belum tersedia. Aktifkan GPS.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Izin lokasi belum tersedia",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void bukaSensor() {
        if (accelerometer == null && gyroscope == null) {
            Toast.makeText(this, "Sensor tidak tersedia di HP",
                    Toast.LENGTH_LONG).show();
            return;
        }

        sensorText = new TextView(this);
        sensorText.setTextSize(16);
        sensorText.setPadding(32, 24, 32, 24);
        sensorText.setText("Membaca sensor...");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Accelerometer & Gyroscope")
                .setView(sensorText)
                .setPositiveButton("Tutup", (d, w) -> {
                    sensorManager.unregisterListener(this);
                })
                .create();

        dialog.setOnDismissListener(d ->
                sensorManager.unregisterListener(this));

        dialog.show();

        if (accelerometer != null) {
            sensorManager.registerListener(
                    this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (gyroscope != null) {
            sensorManager.registerListener(
                    this, gyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensorText == null) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        String data = String.format(
                java.util.Locale.US,
                "%.2f, %.2f, %.2f", x, y, z);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorText.setText(
                    "ACCELEROMETER\nX: " + data.split(", ")[0]
                            + "\nY: " + data.split(", ")[1]
                            + "\nZ: " + data.split(", ")[2]
                            + "\n\nGerakkan atau miringkan HP untuk melihat perubahan.");
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorText.append(
                    "\n\nGYROSCOPE\nX: " + x
                            + "\nY: " + y
                            + "\nZ: " + z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Tidak perlu tindakan tambahan.
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}
