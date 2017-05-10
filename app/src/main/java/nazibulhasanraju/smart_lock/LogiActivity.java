package nazibulhasanraju.smart_lock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogiActivity extends AppCompatActivity {
    EditText userid,userpassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logi);
        userid= (EditText) findViewById(R.id.userEdit);
        userpassword= (EditText) findViewById(R.id.passEdit);
    }
    public void done(View view){
        if((userid.getText().toString().equals("anik")==true) && (userpassword.getText().toString().equals("1234")==true)){
            Intent intent=new Intent(LogiActivity.this,Device_ListActivity.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(this,"Sorry you entered wrong password",Toast.LENGTH_LONG).show();
        }
    }
}
