package ir.halname.wake_up;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends Activity implements OnClickListener {
	Button submit;
	String username, password;
	EditText userinput, passinput;
	SharedPreferences sh_Pref;
	Editor toEdit;

	String user ="";
	String pass = "";
	
	//datePicker veiw
	DateFormat fmtDateAndTime = DateFormat.getDateTimeInstance();
	TextView lblDateAndTime;
	Calendar myCalendar = Calendar.getInstance();
	
	DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
		
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
	myCalendar.set(Calendar.YEAR, year);
	myCalendar.set(Calendar.MONTH, monthOfYear);
	myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	updateLabel();
	}
	};

	TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		myCalendar.set(Calendar.MINUTE, minute);
		updateLabel();
	}
	};

	private void updateLabel() {
		lblDateAndTime.setText(fmtDateAndTime.format(myCalendar.getTime()));
	}
	
	//onCreate
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		saveInformations();
		restoreInformations();

        //datePicker update text
        lblDateAndTime = (TextView) findViewById(R.id.dateAndTime);
    	Button btnDate = (Button) findViewById(R.id.date_btn);
    	btnDate.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			new DatePickerDialog(MainActivity.this, d, myCalendar
    					.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
    					myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    		}
    	});

    	Button btnTime = (Button) findViewById(R.id.time_btn);
    	btnTime.setOnClickListener(new View.OnClickListener() {
    		public  void onClick(View v) {
    			new TimePickerDialog(MainActivity.this, t, myCalendar
    					.get(Calendar.HOUR_OF_DAY), myCalendar
    					.get(Calendar.MINUTE), true).show();
    		}
    	});
    	updateLabel();

	}
	
	//call action
	public void call(View view){
		EditText txt_user = (EditText) findViewById(R.id.txt_user);
		EditText txt_pass = (EditText) findViewById(R.id.txt_pass);
		EditText txt_phone = (EditText) findViewById(R.id.txt_tel);
		user = txt_user.getText().toString();
		pass = txt_pass.getText().toString();
		String tel = "";
		tel = txt_phone.getText().toString();
		int date_year = myCalendar.get(Calendar.YEAR);
		int date_month = myCalendar.get(Calendar.MONTH)+1;
		int date_day = myCalendar.get(Calendar.DAY_OF_MONTH);
		int time_hour = myCalendar.get(Calendar.HOUR_OF_DAY);
		int time_minute = myCalendar.get(Calendar.MINUTE);
		
		if ((user.length()==0) || (pass.length()==0)){
			Toast.makeText(this, "Enter username & password" , Toast.LENGTH_LONG).show();
		}else{
			//validation phone number
			if((tel.length()!= 11) || tel.charAt(0) != '0'){
				Toast.makeText(this, "Your Phone Number isn't correct!" , Toast.LENGTH_LONG).show();
			}else{
				String url= "http://wake.huri.ir/wake/?user="+user+"&pass="+pass+"&d="+date_year+"-"+date_month+"-"+date_day+"&t="+time_hour+"-"+time_minute+"&p="+tel;
				try{
					String output = new DownloadTextTask().execute(url).get();
					switch (output) {
					  case "1":
							Toast.makeText(this,"The Operation is Successful" , Toast.LENGTH_LONG).show();
					        break;
					  case "2": 
							Toast.makeText(this,"Username or password isn't correct!" , Toast.LENGTH_LONG).show();
					        break;
					  default:
							Toast.makeText(this,"Some problems occurred!" , Toast.LENGTH_LONG).show();
					        break;
					}
				}catch(Exception ex){
			    	ex.printStackTrace();
				}
			}
		}
	}
	
	//wake_up action
	public void wake(View view){
		EditText txt_user = (EditText) findViewById(R.id.txt_user);
		EditText txt_pass = (EditText) findViewById(R.id.txt_pass);
		user = txt_user.getText().toString();
		pass = txt_pass.getText().toString();
		if ((user.length()==0) || (pass.length()==0)){
			Toast.makeText(this, "Enter username & password" , Toast.LENGTH_LONG).show();
		}else{
			String url= "http://wake.huri.ir/call/?user="+user+"&pass="+pass;
			try{
				String output = new DownloadTextTask().execute(url).get();
				if (output.length()>1){
					Intent i= new Intent("android.intent.action.DIAL");
					i.setData(Uri.parse("tel:"+output));
					startActivity(i);
				}else{
					Toast.makeText(this,"Nobody wants to be called" , Toast.LENGTH_LONG).show();
				}
			}catch(Exception ex){
		    	ex.printStackTrace();
			}
		}
	}
	
	//store user & pass
	public void saveInformations() {
		submit = (Button) findViewById(R.id.save_btn);
		userinput = (EditText) findViewById(R.id.txt_user);
		passinput = (EditText) findViewById(R.id.txt_pass);
		submit.setOnClickListener(this);
	} 
	
	public void sharedPrefernces() {
		sh_Pref = getSharedPreferences("Login", MODE_PRIVATE);
		toEdit = sh_Pref.edit();
		toEdit.putString("Username", username);
		toEdit.putString("Password", password);
		toEdit.commit();
	}
	
	@Override
	public void onClick(View currentButton) {
		if (currentButton.getId()==R.id.save_btn) {
			username = userinput.getText().toString();
			password = passinput.getText().toString();
			sharedPrefernces();
			Toast.makeText(this,"Informations are saved" , Toast.LENGTH_LONG).show();
		} 
	}

	//restore user & pass
	public void restoreInformations() {
		TextView user_restore = (TextView) findViewById(R.id.txt_user);
		TextView pass_restore = (TextView) findViewById(R.id.txt_pass);
		SharedPreferences sp1=this.getSharedPreferences("Login",0);
		String userTxt=sp1.getString("Username", null);       
		String passTxt = sp1.getString("Password", null);
		user_restore.setText(userTxt);
		pass_restore.setText(passTxt);

	} 
	
	//http Connection
	private InputStream OpenHttpConnection(String urlString) 
		    throws IOException
		    {
		        InputStream in = null;
		        int response = -1;
		           
		        URL url = new URL(urlString); 
		        URLConnection conn = url.openConnection();
		                 
		        if (!(conn instanceof HttpURLConnection))                     
		            throw new IOException("Not an HTTP connection");        
		        try{
		            HttpURLConnection httpConn = (HttpURLConnection) conn;
		            httpConn.setAllowUserInteraction(false);
		            httpConn.setInstanceFollowRedirects(true);
		            httpConn.setRequestMethod("GET");
		            httpConn.connect();
		            response = httpConn.getResponseCode();                 
		            if (response == HttpURLConnection.HTTP_OK) {
		                in = httpConn.getInputStream();                                 
		            }                     
		        }
		        catch (Exception ex)
		        {
		            throw new IOException("Error connecting");
		        }
		        return in;     
		    }
		
	private String DownloadText(String URL)
    {
        int BUFFER_SIZE = 2000;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
        } catch (IOException e) {
            return "";
        }
        
        InputStreamReader isr = new InputStreamReader(in);
        int charRead;
        String str = "";
        char[] inputBuffer = new char[BUFFER_SIZE];          
        try {
            while ((charRead = isr.read(inputBuffer))>0) {                    
                //---convert the chars to a String---
                String readString = String.copyValueOf(inputBuffer, 0, charRead);                    
                str += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            in.close();
        } catch (IOException e) {
            return "";
        }    
        return str;        
    }    
    
	private class DownloadTextTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... urls) {
			return DownloadText(urls[0]);
		}
		@Override
		protected void onPostExecute(String result) {
			
		}
	}

}
