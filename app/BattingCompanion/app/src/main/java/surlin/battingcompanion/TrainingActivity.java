package surlin.battingcompanion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class TrainingActivity extends AppCompatActivity {
    private boolean recordShot = false;
    private int SENSOR_UPDATE_INTERVAL = 100;

    private SensorEventListener acceleromterListener = new AccelerometerListener();
    private SensorEventListener gyroscopeListener = new GyroscopeListener();

    private Sensor acceleromterSensor;
    private Sensor gyroscopeSensor;

    private ArrayList<SensorValues> accelerometerValues = new ArrayList<>();
    private ArrayList<SensorValues> gyroscopeValues = new ArrayList<>();

    private SensorManager sensorManager;

    private TextView accText; //= (TextView) findViewById(R.id.acc_text);
    private TextView gyroText; //= (TextView) findViewById(R.id.gyro_text);

    private class AccelerometerListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            accelerometerValues.add(new SensorValues(event.values[0], event.values[1], event.values[2]));
            if(accText != null)accText.setText("Accelerometer: \n x = " + event.values[0] + "\n y = " + event.values[1] + "\n z = " + event.values[2] + "\n # values = " + accelerometerValues.size());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private class GyroscopeListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            gyroscopeValues.add(new SensorValues(event.values[0], event.values[1], event.values[2]));
            if(gyroText != null) gyroText.setText("Gyroscope: \n x = " + event.values[0] + "\n y = " + event.values[1] + "\n z = " + event.values[2] + "\n # values = " + gyroscopeValues.size());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accText = (TextView) findViewById(R.id.acc_text);
        gyroText = (TextView) findViewById(R.id.gyro_text);
        if(sensorManager == null){
            sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
            acceleromterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        }
        final Button shotButton = (Button) findViewById(R.id.shot_button);
        shotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordShot = !recordShot;
                if(recordShot == false){
                    shotButton.setText("Start Shot");
                    sensorManager.unregisterListener(acceleromterListener);
                    sensorManager.unregisterListener(gyroscopeListener);
                    String dataCSV = buildCSV();
                    //String shot = getShotType();
                    accelerometerValues.clear();
                    gyroscopeValues.clear();
                    sendShotData(dataCSV);
                }
                else{
                    shotButton.setText("End Shot");
                    sensorManager.registerListener(acceleromterListener, acceleromterSensor, SensorManager.SENSOR_DELAY_GAME);
                    sensorManager.registerListener(gyroscopeListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
                }
            }
        });
    }

    private void sendShotData(final String shotData){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shot Type");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Send shot data", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputStr = input.getText().toString();
                String finalOutput = inputStr + "\n" + shotData;
                System.out.println(finalOutput);
                new DataSender().execute(finalOutput);


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    class DataSender extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params) {
            String finalOutput = params[0];
            URL url = null;
            try {
                url = new URL("http://54.218.20.72:5000/saveTrainingData");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "text/plain");
                conn.getOutputStream().write(finalOutput.getBytes());
                conn.getInputStream();
                System.out.println("request succesfully sent");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private String buildCSV() {
        int numDatapoints = 0;
        if(accelerometerValues.size() <= gyroscopeValues.size()) numDatapoints = accelerometerValues.size();
        else numDatapoints = gyroscopeValues.size();
        String csvString = "accX, accY, accZ, gyroX, gyroY, gyroZ\n";
        for(int i = 0; i < numDatapoints; i++){
            String curRow = accelerometerValues.get(i).x + ", " + accelerometerValues.get(i).y + ", " + accelerometerValues.get(i).z + ", " +
                            gyroscopeValues.get(i).x + ", " + gyroscopeValues.get(i).x + ", " + gyroscopeValues.get(i).x;
            csvString += curRow + "\n";
        }
        System.out.println(csvString);
        return csvString;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class SensorValues{
        float x;
        float y;
        float z;
        public SensorValues(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
