package com.yongmac.VletterApp.Activity;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.yongmac.VletterApp.MyApplication;
import com.yongmac.VletterApp.R;
import com.yongmac.VletterApp.Retrofit.RetrofitAPI;
import com.yongmac.VletterApp.Retrofit.UserListVO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LetterActivity extends AppCompatActivity {
    //녹음여부 , 음성재생 여부의 flag
    public boolean isRecording = false;
    public boolean isPlaying = false;

    //해당 음성파일이 자신의 해당 폰에 임시 저장될 path
    static final String RECORDED_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordVoice.pcm";
    // MediaPlayer 클래스에 재생에 관련된 메서드와 멤버변수가 저장어되있다.

    //음성과 관련된 API
    MediaRecorder recorder;
    MediaPlayer player;
    MultipartEntityBuilder builder;

    //해당 음성이 담길 String 문자열
    String encodedVoice = null;
    String name, phoneNumber;

    Button recordBtn, playBtn, sendBtn, addressbookBtn;
    EditText edName, edPhoneNumber;

    public String HostingURL= "http://13.209.89.216:8080/NUGU/";
    final String url_address = "http://13.209.89.216:8080/NUGU/letterUpload";

    private Retrofit retrofit;
    private RetrofitAPI mRetrofitAPI;


    final String URL_ADRESS = HostingURL;

    public ArrayList<UserListVO.User> userArrayList;
    public UserListVO userListVO;

    InputMethodManager imm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter);


        recordBtn = (Button) findViewById(R.id.Btnrecord);
        playBtn = (Button) findViewById(R.id.Btnplay);
        sendBtn = (Button) findViewById(R.id.BtnSend);

        addressbookBtn = (Button) findViewById(R.id.addressbook);
        edPhoneNumber = (EditText) findViewById(R.id.edReceverPhoneNumber);
        edName = (EditText)findViewById(R.id.edReceverName);

        PermissionFunction();
        RetrofitFunction();


        try {
            Intent intent = getIntent();
            name = intent.getExtras().getString("name");
            phoneNumber = intent.getExtras().getString("phoneNumber").replace("-","");
            edName.setText(name);
            edPhoneNumber.setText(phoneNumber);
        } catch (Exception e) {

        }

        XMLSetting();

    }


    private void RetrofitFunction() {
        setRetrofitInit();
        callUserList();
    }

    private void callUserList() {
        Call<UserListVO> call = mRetrofitAPI.getUsers();
        call.enqueue(new Callback<UserListVO>() {
            @Override
            public void onResponse(Call<UserListVO> call, Response<UserListVO> response) {
                //URL_ADRESS + /test/getFriendList 에 get 방식으로 접속해서 데이터를 성공적으로 가져옴.
                //데이터를 friendArrayList에 넣는다.
                if (response.isSuccessful()) {
                    userListVO = response.body();
                    userArrayList = userListVO.getUserList();
                }
            }
            @Override
            public void onFailure(Call<UserListVO> call, Throwable t) {

            }
        });
    }

    private void setRetrofitInit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(URL_ADRESS)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = retrofit.create(RetrofitAPI.class);
    }

    private void PermissionFunction() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(getApplicationContext(), "권한허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "권한거부 \n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("음성 녹음을 위해서는 접근 권한이 필요해요.")
                .setDeniedMessage("음성 녹음 접근 권한을 허용하지 않았습니다. \n 하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS)
                .check();

    }

    private void XMLSetting() {

        if (encodedVoice == null) {
            //  화면에 안 보임.
            playBtn.setVisibility(View.INVISIBLE);
            sendBtn.setVisibility(View.INVISIBLE);
            recordBtn.setVisibility(View.VISIBLE);
        }

        addressbookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AdressActivity.class);
                startActivity(intent);
            }
        });

        // 저장할 파일 위치를 String 으로 처리했다.
        // 녹음 시작 버튼
        recordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isRecording) {  //녹음 멈추는 부분.
                    isRecording = false;
                    try {
                        if (recorder == null)
                            return;
                        recorderClear();
                        ToastFunction("녹음이 중지되었습니다.");
                    } catch (RuntimeException e) {
                        ToastFunction("음성을 다시 녹음해주세요.");
                    }
                    try {
                        //encode by Base64
                        File file = new File(RECORDED_FILE);
                        byte[] FileBytes = getBytesFromFile(file);
                        encodedVoice = Base64.encodeToString(FileBytes, Base64.NO_WRAP);
                        playBtn.setVisibility(View.VISIBLE);
                        sendBtn.setVisibility(View.VISIBLE);
                        recordBtn.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                    }
                } else {  //녹음 하는 부분.
                    try {
                        if (recorder != null)
                            recorderClear();
                    } catch (RuntimeException e) {
                        ToastFunction("음성을 다시 녹음해주세요.");
                    }
                    try {
                        setting();
                        isRecording = true;
                        ToastFunction("녹음이 시작되었습니다.");
                        // 녹음 준비,시작
                        recorder.prepare();
                        recorder.start();
                    } catch (Exception ex) {
                        isRecording = false;
                        ex.printStackTrace();
                        ToastFunction("음성 녹음에 대한 권한이 없습니다.");
                        PermissionFunction();
                    }
                }
            }
        });

        // 오디오 플레이 버튼
        playBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isPlaying) {
                    if (player == null)
                        return;
                    // 오디오 재생 중지
                    playerClear();
                    isPlaying = false;
                    ToastFunction("재생이 중지되었습니다.");
                } else {  //음성 재생하기.
                    if (player != null)
                        playerClear();
                    try {
                        // 오디오를 플레이 하기위해 MediaPlayer 객체 player를 생성한다.
                        player = new MediaPlayer();
                        // 재생할 오디오 파일 저장위치를 설정
                        player.setDataSource(RECORDED_FILE);
                        // 오디오 재생준비,시작
                        player.prepare();
                        player.start();
                        isPlaying = true;
                        ToastFunction("편지를 들어보겠습니다.");
                    } catch (Exception e) {
                        ToastFunction("편지를 녹음해주세요.");
                        isPlaying = false;
                    }
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = false;
                phoneNumber = edPhoneNumber.getText().toString();
                for (UserListVO.User userData : userArrayList) {
                    if (userData.getPhone().equals(phoneNumber)) {
                        b = true;
                        break;
                    }
                }

                if (b) {
                    new SendPost().execute();
                } else {
                    edName.setText("");
                    edPhoneNumber.setText("");
                    ToastFunction("해당 친구가 존재하지 않습니다. 전화 번호를 입력해주세요.");
                }
            }
        });

    }

    //Byte[] 형태로 파일 읽어오는 메서드
    public byte[] getBytesFromFile(java.io.File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
        }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
            offset += numRead;
        if (offset < bytes.length)
            throw new IOException("Could not completely read file " + file.getName());
        is.close();
        return bytes;
    }

    public void setting() {
        // 실험 결과 왠만하면 아래 recorder 객체의 속성을 지정하는 순서는 이대로 하는게 좋다 위치를 바꿨을때 에러가 났었음
        // 녹음 시작을 위해  MediaRecorder 객체  recorder를 생성한다.
        recorder = new MediaRecorder();
        // 오디오 입력 형식 설정
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 음향을 저장할 방식을 설정
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 오디오 인코더 설정
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        // 저장될 파일 지정
        recorder.setOutputFile(RECORDED_FILE);
    }

    public void playerClear() {
        //오디오 재생을 멈춤.
        player.stop();
        // 오디오 재생에 필요한 메모리들을 해제한다
        player.release();
        player = null;
    }

    public void recorderClear() {
        // 녹음을 중지
        recorder.stop();
        // 오디오 녹음에 필요한  메모리를 해제한다
        recorder.release();
        recorder = null;

    }

    private void ToastFunction(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void builderSetting(String key, String value) {
        builder.addTextBody(key, value, ContentType.create("Multipart/related", "UTF-8"));
    }

    private class SendPost extends AsyncTask<Void, Void, String> {
        HttpClient httpClient;

        @Override
        protected String doInBackground(Void... unused) {
            //Background 에서 함수 실행시킨다. 음성파일과 정보를 보낸다.
            excuteClient();
            return null;
        }

        private void excuteClient() {
            try {
                builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                MyApplication myApp = (MyApplication) getApplicationContext();
                builderSetting("id",myApp.getId());
                builderSetting("date",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).trim());
                builderSetting("recever",phoneNumber);
                builder.addPart("filename", new FileBody(new File(RECORDED_FILE)));

                InputStream inputStream = null;
                httpClient = AndroidHttpClient.newInstance("Android");
                HttpPost httpPost = new HttpPost(url_address);
                httpPost.setEntity(builder.build());
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                inputStream = httpEntity.getContent();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                inputStream.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onPostExecute(String s) {
            ToastFunction("편지가 전송 되었습니다.");
            finish();
        }
        @Override
        protected void onCancelled() {
            super.onCancelled();
            ToastFunction("편지를 전송하지 못 했습니다.");
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }


}