package org.coindirect.centrifuge.java.it;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SendEventTest {

    @Test
    public void testSendEvent() {


        JSONObject jsonObject = new JSONObject();
        String commandId = "123";
        try {
            jsonObject.put("uid", commandId);
            jsonObject.put("method", "publish");
            JSONObject params = new JSONObject();
            params.put("channel", "channel");
            params.put("data", "event");
            jsonObject.put("params", params);
        } catch (JSONException e) {
            //FIXME error handling
        }

        String result = jsonObject.toString();

        Assert.assertEquals(result, "{\"uid\":\"123\",\"method\":\"publish\",\"params\":{\"channel\":\"channel\",\"data\":\"event\"}}");
    }
}
