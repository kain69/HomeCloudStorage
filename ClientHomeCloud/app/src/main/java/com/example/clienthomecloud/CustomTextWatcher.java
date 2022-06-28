package com.example.clienthomecloud;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;

public class CustomTextWatcher implements TextWatcher {

    View v1, v2;
    EditText ed_ip;

    public CustomTextWatcher(EditText ed_ip, Button v1, Button v2) {
        this.v1 = v1;
        this.v2 = v2;
        this.ed_ip = ed_ip;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (ifIpText( ed_ip.getText().toString().trim() )) {
            v1.setEnabled(true);
            v2.setEnabled(true);
        }
        else {
            v1.setEnabled(false);
            v2.setEnabled(false);
        }
    }

    public boolean ifIpText(String textIp){
        if (!textIp.matches ("^(\\d{1,3}\\.){3}\\d{1,3}")) {
            return false;
        }
        else {
            String[] splits = textIp.split("\\.");
            if(splits.length != 4) return false;
            for (int i=0; i<splits.length; i++) {
                if (Integer.valueOf(splits[i]) > 255) {
                    return false;
                }
            }
        }
        return true;
    }
}