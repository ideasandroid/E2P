package com.ideasandroid.e2p;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class E2Pwindow extends Activity{
	
	private TextView textContent=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 //先去除应用程序标题栏  注意：一定要在setContentView之前
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //将我们定义的窗口设置为默认视图
        setContentView(R.layout.float_activity);
        textContent=(TextView)findViewById(R.id.e2pMessage);
        String text=getIntent().getExtras().getString("message");
        textContent.setText(text);
	}

}
