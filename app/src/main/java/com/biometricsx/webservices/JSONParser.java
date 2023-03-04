package com.biometricsx.webservices;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {

    public String parseJSON(JSONObject json)
    {
        String ans="";
        try {
            ans=json.getString("Value");
        } catch (JSONException e) {
           ans="Parse() " + e.getMessage();
        }
        return ans;
    }
}
