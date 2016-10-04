package tcnr6.com.m1701;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import tcnr6.com.m1701.providers.FriendsContentProvider;

public class M1701 extends Activity implements OnItemSelectedListener,LocationListener{


	private static final String MAP_URL = "file:///android_asset/googleMAP.html";
	// 自建的html檔名
	private WebView webView;
	   //--------GPS
	   private boolean webviewReady = false;
	   private Location mostRecentLocation = null;
	   //---------------
	   
		private LocationManager locationMgr;
//		Criteria criteria = new Criteria();
		String provider;

		private long minTime = 5000;// ms
		private float minDist = 5.0f;// meter
	   

		 private Spinner mSpnLocation;
         private TextView m1701_T001;
         private Button m1701_B001;
         private int navon=0;
         
         String Navstart ; // 起始點
         String Navend ; // 結束點
		 
		 private String LatGps=null;
		 private String LngGps=null;
		 
			private static ContentResolver mContRes;
			private String[] MYCOLUMN = new String[] { "id", "name", "grp", "address" };
			
			 int autotime = 30;// 要幾秒的時間 更新匯入MySQL資料
			 SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			 private Long startTime;
			 int old_index = 0;
			 int update_time=0;
			 private Handler handler = new Handler();
		 private String settitle;//地名變數
		private static String TAG="tcnr6==>";
		 private String myID="3",myName="古佳弘",myGrp="A",myAddr="24.1674900,120.63989";
	    private int mySpinNum=0,targetNum=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m1701);
		
		
		setupwebview();
		
		setupviewcomponent();
		db_mySQL();
	}



	private void setupviewcomponent() {
		// TODO Auto-generated method stub
      m1701_T001=(TextView)findViewById(R.id.m1701_T001);
	  m1701_T001.getBackground().setAlpha(100);
	  m1701_B001=(Button)findViewById(R.id.m1705_B001);
	  m1701_B001.getBackground().setAlpha(100);
	  m1701_B001.setOnClickListener(bNavon);
	  
	  startTime = System.currentTimeMillis();
	  // 設定Delay的時間
	  handler.postDelayed(updateTimer, autotime*1000); // 延遲
	  Date curDate = new Date(System.currentTimeMillis()); //  獲取當前時間
	  String str = formatter.format(curDate);
//	  nowtime.setText(getString(R.string.now_time) + str);
//	  Log.d(TAG, "setupViewComponent()=" + str);
	  // ---------------------------------------
//	  sqliteupdate(); // 抓取SQLite資料
	  // ----------------------------------------
	}

	private void setupwebview() {
		// TODO Auto-generated method stub
		webView = (WebView) findViewById(R.id.googlemap);		
		
		mSpnLocation=(Spinner)findViewById(R.id.m1701_S001);
		mSpnLocation.getBackground().setAlpha(100);
		
		selectSQL(myName);
//		  // ----Location-----------
//		  ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//				  android.R.layout.simple_spinner_item);
//		  
//		  for (int i = 0; i < locations.length; i++)
//			   adapter.add(locations[i][0]);
//		  
//		  adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
//		  mSpnLocation.setAdapter(adapter);
//		
//		mSpnLocation.setOnItemSelectedListener(this);
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(M1701.this, "AndroidFunction");

		webView.loadUrl(MAP_URL);
	}
	
   private void selectSQL(String myName2) {
	   
	
		try {
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("query_string", "query"));
			params.add(new BasicNameValuePair("name", myName2));
			
			String result = DBConnector.executeQuery("query",params);
			/**************************************************************************
			 * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
			 * jsonData = new JSONObject(result);
			 **************************************************************************/
			 Log.d(TAG, "result->"+result);
			if(result.equals("查無姓名")){
				   Toast.makeText(M1701.this, "查無姓名"+myName, Toast.LENGTH_SHORT).show();
				   insertSQL(myName2,myGrp,myAddr);
			   }else {
				   myID=null;myName=null;myGrp=null;myAddr=null;
				   JSONArray jsonArray = new JSONArray(result);
				   JSONObject jsonData = jsonArray.getJSONObject(0);
				   myID=jsonData.getString("id").toString();
				   myName=jsonData.getString("name").toString();
				   myGrp=jsonData.getString("grp").toString();
//				   myAddr=jsonData.getString("address");
				   
			}
			
			   //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
			   //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
//			   Log.d(TAG, "httpstate="+DBConnector.httpstate );
			   if (DBConnector.httpstate == 200) {
			    Uri uri = FriendsContentProvider.CONTENT_URI;
			    mContRes.delete(uri, null, null);
			    Toast.makeText(getBaseContext(), "已經完成由伺服器會入資料",
			      Toast.LENGTH_LONG).show();
			    } else {
			    Toast.makeText(getBaseContext(), "伺服器無回應，請稍後在試",
			      Toast.LENGTH_LONG).show();
			   }
			  
			   
			   
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
   
   
	private void insertSQL(String myName, String myGrp, String myAddr) {
	
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("query_string", "insert"));
		nameValuePairs.add(new BasicNameValuePair("name", myName));
		nameValuePairs.add(new BasicNameValuePair("grp", myGrp));
		nameValuePairs.add(new BasicNameValuePair("address", myAddr));
		try {
			Thread.sleep(500); // 延遲Thread 睡眠0.5秒
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String result = DBConnector.executeInsert("insert", nameValuePairs);

	}

	private void mySQL_update(String tid, String tname, String tgrp, String taddr) {
	
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		  nameValuePairs.add(new BasicNameValuePair("query_string", "update"));
		  nameValuePairs.add(new BasicNameValuePair("id", tid));
		  nameValuePairs.add(new BasicNameValuePair("name", tname));
		  nameValuePairs.add(new BasicNameValuePair("grp", tgrp));
		  nameValuePairs.add(new BasicNameValuePair("address", taddr));
		  String result = DBConnector.executeUpdate("update", nameValuePairs);
		  Log.d(TAG, "result:" + result);
		  Toast.makeText(getApplicationContext(), "mySQL:"+result, Toast.LENGTH_LONG).show();
		
	}

private	Button.OnClickListener bNavon=new  Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			if (navon==0) {

				m1701_B001.setBackgroundColor(getResources().getColor(R.drawable.red));
				m1701_B001.getBackground().setAlpha(100);
				navon=1;
				m1701_B001.setText("關閉路線規劃");
				final String URL = "javascript:getNavon("+navon+")" ;
				webView.loadUrl(URL);
				setMapLocation();
			} else if(navon==1){
				webView.loadUrl(MAP_URL);

				m1701_B001.setBackgroundColor(getResources().getColor(R.drawable.blue));
				m1701_B001.getBackground().setAlpha(100);
				navon=0;
				m1701_B001.setText("開啟路線規劃");
				final String URL = "javascript:getNavon("+navon+")" ;
				webView.loadUrl(URL);
				setMapLocation();
			}
//			Log.d(TAG, "Navon()->"+GetNavon());
		}


		
	};
	
	//<!-----------從mySQL抓資料新增到SQLite------------->
		private void db_mySQL() {
			

			// -------------------------
			mContRes = getContentResolver();
			//-----------------------------		
			Cursor c = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
			c.moveToFirst(); // 一定要寫，不然會出錯	
			try {
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("query_string", "import"));
				
				String result = DBConnector.executeQuery("import",params);
				/**************************************************************************
				 * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
				 * jsonData = new JSONObject(result);
				 **************************************************************************/
				 String r = result.toString().trim();
				   //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
				   //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
//				   Log.d(TAG, "httpstate="+DBConnector.httpstate );
				   if (DBConnector.httpstate == 200) {
				    Uri uri = FriendsContentProvider.CONTENT_URI;
				    mContRes.delete(uri, null, null);
				    Toast.makeText(getBaseContext(), "已經完成由伺服器會入資料",
				      Toast.LENGTH_LONG).show();
				    } else {
				    Toast.makeText(getBaseContext(), "伺服器無回應，請稍後在試",
				      Toast.LENGTH_LONG).show();
				   }
				
				JSONArray jsonArray = new JSONArray(result);
				// ---
//				Log.d(TAG, jsonArray.toString());
				if(jsonArray.length()>0){
					Uri uri = FriendsContentProvider.CONTENT_URI;
					mContRes.delete(uri, null, null); // 刪除所有資料	
				}
				
				ContentValues newRow = new ContentValues();
				
			
				for (int i = 0; i < jsonArray.length(); i++) {
					
					JSONObject jsonData = jsonArray.getJSONObject(i);
//					Log.d(TAG, "mySpinNum->"+i);
				  // 取出 jsonObject 中的字段的值的空格Iterator功能進四cursor
					Iterator itt = jsonData.keys();//ID用mySQL的ID
					while (itt.hasNext()) {
						
						String key = itt.next().toString();
						String value = jsonData.getString(key);
						if (value == null) {
							continue;
						} else if ("".equals(value.trim())) {
							continue;
						} else {
							newRow.put(key, value.trim());
						}						

					}
					

				
					mContRes.insert(FriendsContentProvider.CONTENT_URI, newRow);

//					count_t.setTextColor(Color.BLUE);
//					count_t.setText("顯示資料：共加入" + Integer.toString(jsonArray.length()) + " 筆");
//					if(jsonData.getString("id").toString().equals(myID)){
////						mySpinNum=;
//						Thread.sleep(500);
//						Log.d(TAG, "myName->"+myID);
//						Log.d(TAG, "i->"+i);
//					}
//					Log.d(TAG, "mySpinNum->"+i);
					
				}

			} catch (Exception e) {
				Log.d(TAG,"error->"+ e.toString());
			}
			c.close();
//			onCreate(null); // 重構
			sqliteupdate();
			
		}
		// --------SQLite資料------------------------------------------------
		private void sqliteupdate() {
			mContRes = getContentResolver();
			Cursor c = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
			c.moveToFirst(); // 一定要寫，不然會出錯
			int tcount = c.getCount();
			// ---------------------------
//			count_t.setTextColor(Color.BLUE);
//			count_t.setText("顯示資料： 共" + tcount + " 筆");
//			E004.setTextColor(Color.RED);
			// -------------------------
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_style);

			for (int i = 0; i < tcount; i++) {
				c.moveToPosition(i);
//				adapter.add(c.getString(0) + "-" + c.getString(1) + "," + c.getString(2) + "," + c.getString(3));
				 adapter.add("名稱:" +c.getString(1) + " 位置:" + c.getString(3));
			}
			c.close();
	       
			// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			adapter.setDropDownViewResource(R.layout.spinner_style);
			mSpnLocation.setAdapter(adapter);
			mSpnLocation.setOnItemSelectedListener(this);
			// S001.setSelection(position=下拉視窗跳到第幾筆
			// , animate);
//			showip = NetwordDetect();
			mSpnLocation.setSelection(mySpinNum, true); //spinner 小窗跳到第幾筆
			// ----------------------------------------
			// 宣告鈴聲
//			ToneGenerator	toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100); // 100=max
//			toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 500);
//			toneG.release();
		}
		
		// -----------------------------------------------------------
		// 固定要執行的方法
		private Runnable updateTimer = new Runnable() {
			@Override
			public void run() {
				Long spentTime = System.currentTimeMillis() - startTime;
				// 計算目前已過分鐘數
				Long minius = (spentTime / 1000) / 60;
				// 計算目前已過秒數
				Long seconds = (spentTime / 1000) % 60;
				handler.postDelayed(this, autotime * 1000); // 真正延遲的時間
				Date curDate = new Date(System.currentTimeMillis()); //  獲取當前時間
				String str = formatter.format(curDate);
//				Log.d(TAG, "run:" + str);
				// -------執行匯入MySQL
				db_mySQL();
				++update_time;
                Toast.makeText(M1701.this,"(每"+autotime+"秒)"+ str + " " +"\n目前更新了:"+ minius + ":" + seconds+" ("+update_time+"次)" , Toast.LENGTH_LONG).show();
//				nowtime.setText(getString(R.string.now_time) +"(每"+autotime+"秒)"+ str + " " +"\n目前更新了:"+ minius + ":" + seconds+" ("+update_time+"次)");
				mSpnLocation.setSelection(mySpinNum, true); // spinner 小窗跳到第幾筆
				
				 String[][] sqLiteData=getSQLiteData(mContRes, FriendsContentProvider.CONTENT_URI, MYCOLUMN);
				 String[] sLocation=  sqLiteData[mySpinNum][3].split(",");
				
				 final String setendURL="javascript:setend("+sLocation[0]+","+sLocation[1]+")";

				  webView.loadUrl(setendURL);
				// ---------------------------


			}


		};
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// 清除多餘的點或軌跡圖
//		   final String centerURL = "javascript:clear()";
//		   webView.loadUrl(centerURL);
		   // ----------------------
		   mySpinNum = position; // 設定 spinner 框頭為選擇項目
		   
		 setMapLocation();
		 
	}

	private void setMapLocation() {
          String[][] sqLiteData=getSQLiteData(mContRes, FriendsContentProvider.CONTENT_URI, MYCOLUMN);
		
		  settitle=null;LatGps=null;LngGps=null;
		  int iSelect = mSpnLocation.getSelectedItemPosition();
		  
		  String[] sLocation=  sqLiteData[iSelect][3].split(",");
		  
		  

//		  String[] sLocation  = locations[iSelect][1].split(",");

		  LatGps = sLocation[0]; // 南北緯
		  LngGps = sLocation[1]; // 東西經
		  
//		  settitle = locations[iSelect][0];  //地名
		  
		  final String mypositionURL="javascript:mark("+LatGps+","+LngGps+")";

		  webView.loadUrl(mypositionURL);
//	       final String centerURL = "javascript:centerAt(" +
//	    		   LatGps + "," +
//	    		   LngGps+ ")";
//	  		    webView.loadUrl(centerURL);
	  		    
	  			if(navon == 1 && iSelect!=0){
	  				Navstart = myAddr;
	  				Navend = sqLiteData[iSelect][3];
	  			}

	  			
	}
	
//	private String convertLocationsToStrings(){
//		
//		String str = "";
//		  for (int i = 0; i < locations.length; i++){
//		   
//		   str += locations[i][0] + "," + locations[i][1] + "#";   
//		  }
//		  
//		  str = str.substring(0, str.length() -1);
//		
//		return str;
//		
//	}
	
	private JSONArray arrayToJson(){//可用在GPS多點顯示
		double lat=24.172127;
		double lng=120.610313;
		int addone=0;
		 JSONArray jsonArray=new JSONArray();
		 for (int i = 1; i < 152; i++) {
			 if(i%25==0){  //+((double)i/100)+
				 addone++;
			 }
			 JSONObject jsonObject=new JSONObject();
			 String strNum = String.format("%03d", i);
			 try {
				jsonObject.put("title", "寶可夢"+i+"號");
				jsonObject.put("lat",(lat+((double)i/100)-((double)addone/4)));
				jsonObject.put("lng", (lng-((double)addone/4)));
				jsonObject.put("img", "http://tcnr6hung.16mb.com/pokemon_ico/p"+strNum+".png");
				
				jsonArray.put(jsonObject);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 
//		 for (int i = 0; i < locations.length; i++) {
//			JSONObject jsonObject=new JSONObject();
//			String[] locationArr=locations[i][1].split(",");
//			
//			try {
//				jsonObject.put("title", locations[i][0]);
//				jsonObject.put("lat", locationArr[0]);
//				jsonObject.put("lng", locationArr[1]);
//				jsonObject.put("img", "img/p00"+(i+1)+".png");
//				
//				jsonArray.put(jsonObject);
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			 
//		}		 
//		 Log.d(TAG, "jsonArray->"+jsonArray);
	  return jsonArray;
	 }

		
		
		




	//-----------------------------
	 @JavascriptInterface
	 public String getLat(){
	  return LatGps;
	 }
	 
	 @JavascriptInterface
	 public String getLng(){
	  return LngGps;
	 }

	 @JavascriptInterface
	 public String get_title(){
	  return settitle;
	 }
//	 @JavascriptInterface
//	 public String getStr(){
//		return convertLocationsToStrings();
//		 
//	 }

	 @JavascriptInterface
	 public JSONArray getJsonArray(){
//		 Log.d(TAG, "ArrayToJson->"+arrayToJson());
		return arrayToJson();
		 
	 }
	//----------------------------------------------  
	  @JavascriptInterface
	  public int GetNavon(){
	   return navon;
	  }

	  @JavascriptInterface
	  public String Getstart(){
	   return Navstart;
	  }

	  @JavascriptInterface
	  public String Getend(){
	   return Navend;
	  } 

	//----------------------------------------------------
	 
	

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
//-----------------------------
	@Override
	protected void onStart() {
		if (initLocationProvider()) {
			nowaddress();

		} else {
			m1701_T001.setText("GPS未開啟，請開啟GPS...");

		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		locationMgr.removeUpdates(this);
		super.onStop();
	}
	

	@Override
	protected void onDestroy() {
		handler.removeCallbacks(updateTimer);
		super.onDestroy();
	}
//----------------------------------
	private boolean initLocationProvider() {// 取得現在位置
		locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;// 取得GPS資料
			return true;
		}

		return false;
	}
	private void nowaddress() {
		// TODO Auto-generated method stub
		// 取得上次已知位置
		Location location = locationMgr.getLastKnownLocation(provider);
		getGPSlocation(location);
		// 取得GPS listener
		locationMgr.addGpsStatusListener(gpsListener);

		// Location Listener

		locationMgr.requestLocationUpdates(provider, minTime, minDist, this);

	}
	// -------------------------------
	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

		@Override
		public void onGpsStatusChanged(int event) {
			// TODO Auto-generated method stub
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				Log.d(TAG, "GPS_EVENT_STARTED");
				break;

			case GpsStatus.GPS_EVENT_STOPPED:
				Log.d(TAG, "GPS_EVENT_STOPPED");
				break;

			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.d(TAG, "GPS_EVENT_FIRST_FIX");
				break;

			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
				break;
			}
		}
		
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.m1701, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location location) {
	     if (location !=null){
             getGPSlocation(location);
             
//  	       final String centerURL = "javascript:centerAt(" +
//  		   location.getLatitude() + "," +
//  		   location.getLongitude()+ ")";
//		    webView.loadUrl(centerURL);
		    
//		    final String deletOverlayURL = "javascript:deleteOverlay()";
//		 		    webView.loadUrl(deletOverlayURL);

	       }
		
	}

	private void getGPSlocation(Location location) {
		String where = "";myAddr=null;
		if (location != null) {
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			float speed = location.getSpeed();
			long time = location.getTime();
			String timeStr = getTimeString(time);

			where = "經度：" + lon + "\n緯度：" + lat + "\n速度：" + speed + "\n時間：" + timeStr;
			
			myAddr=lat+","+lon;
			mySQL_update( myID, myName, myGrp, myAddr);
			
			final String centerURL = "javascript:centerAt(" + lat + "," + lon+ ")";
	        webView.loadUrl(centerURL);
		} else {
			where = "GPS位置訊號消失！";
		}
		m1701_T001.setText(where);
//		if (location != null) {
//			String time=getTime(location.getTime());
//			
//			
//			m1701_T001.setText("目前位置:" + "\n緯度:" + location.getLatitude() + "\n經度:" + location.getLongitude() + "\n速度:"
//					+ location.getSpeed() + "\n時間:" + time);
//			// 將畫面移至定位點的位置，呼叫在googlemaps.html中的centerAt函式

//		}
		
	}

	private String getTimeString(long time) {
		
		SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String sysTime = Dateformatter.format(time);
		return sysTime;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		switch (status) {
		  case LocationProvider.OUT_OF_SERVICE:
		   Log.v(TAG, "Status Changed: Out of Service");
		   break;
		  case LocationProvider.TEMPORARILY_UNAVAILABLE:
		   Log.v(TAG, "Status Changed: Temporarily Unavailable");
		   break;
		  case LocationProvider.AVAILABLE:
		   Log.v(TAG, "Status Changed: Available");
		   break;
		  }
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		getGPSlocation(null);// 清空
		
	}

	public static String[][] getSQLiteData(ContentResolver mContRes, Uri uri,
			String[] COLUMN) {
		
		Cursor c = mContRes.query(uri, COLUMN, null, null, null);
		c.moveToFirst();// 一定要寫，不然會出錯
		
		int columNum=c.getColumnCount();
		int count=c.getCount();
	
		String[][] getValue = new String[count][columNum] ;
		Log.d(TAG, "getCount=>"+count+"getCoumn=>"+columNum);
		
		if(count>0&&columNum>0){
			for(int i=0;i<count;i++){
				c.moveToPosition(i);
				for(int j=0;j<columNum;j++){
					getValue[i][j]=c.getString(j);				
					
				}				
			}		
			
		}else {
			return null;
		}
		c.close();
		Log.d(TAG, "getValue=>"+getValue);
		return getValue;		
	}
}
