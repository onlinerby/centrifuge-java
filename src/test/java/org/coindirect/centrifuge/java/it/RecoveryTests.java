package org.coindirect.centrifuge.java.it;

import org.coindirect.centrifuge.java.Centrifugo;
import org.coindirect.centrifuge.java.TestWebapp;
import org.coindirect.centrifuge.java.credentials.Token;
import org.coindirect.centrifuge.java.credentials.User;
import org.coindirect.centrifuge.java.listener.ConnectionListener;
import org.coindirect.centrifuge.java.listener.DataMessageListener;
import org.coindirect.centrifuge.java.listener.SubscriptionListener;
import org.coindirect.centrifuge.java.message.DataMessage;
import org.coindirect.centrifuge.java.subscription.SubscriptionRequest;
import org.coindirect.centrifuge.java.util.DataLock;
import org.coindirect.centrifuge.java.util.Signing;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.testcontainers.containers.GenericContainer;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 04.05.2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Ignore
public class RecoveryTests {

    public GenericContainer centrifugo;

    private MockWebServer mockWebServer;

    @Before
    public void beforeMethod() throws Exception {
        centrifugo = new GenericContainer("samvimes/centrifugo-with-web:1.3")
                .withExposedPorts(8000);
        centrifugo.start();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void afterMethod() throws Exception {
        mockWebServer.shutdown();
        centrifugo.stop();
    }

    @Test
    public void testRecoveryAfterDisconnect() throws Exception {
        String containerIpAddress = centrifugo.getContainerIpAddress() + ":" + centrifugo.getMappedPort(8000);
        String centrifugoAddress = "ws://" + containerIpAddress + "/connection/websocket";
        String centrifugoApiAddress = "http://" + containerIpAddress + "/api/";

        mockWebServer.setDispatcher(new TestWebapp());
        String url = mockWebServer.url("/tokens").toString();

        OkHttpClient okHttpClient = new OkHttpClient();

        Request build = new Request.Builder().url(url).build();
        Response execute = okHttpClient.newCall(build).execute();
        String body = execute.body().string();
        JSONObject loginObject = new JSONObject(body);
        String userId = loginObject.optString("userId");
        String timestamp = loginObject.optString("timestamp");
        String token = loginObject.optString("token");
        Centrifugo centrifugo = new Centrifugo.Builder(centrifugoAddress)
                .setUser(new User(userId, null))
                .setToken(new Token(token, timestamp))
                .build();

        final DataLock<Boolean> connected = new DataLock<>();
        final DataLock<Boolean> disconnected = new DataLock<>();

        centrifugo.setConnectionListener(new ConnectionListener() {
            @Override
            public void onWebSocketOpen() {
            }

            @Override
            public void onConnected(@Nullable final String clientId) {
                connected.setData(true);
            }

            @Override
            public void onDisconnected(final int code, final String reason, final boolean remote) {
                disconnected.setData(!remote);
            }
        });

        centrifugo.connect();
        Assert.assertTrue("Failed to connect to centrifugo", connected.lockAndGet());


        final DataLock<String> channelSubscription = new DataLock<>();
        centrifugo.setSubscriptionListener(new SubscriptionListener() {
            @Override
            public void onSubscribed(final String channelName) {
                channelSubscription.setData(channelName);
            }

            @Override
            public void onUnsubscribed(final String channelName) {

            }

            @Override
            public void onSubscriptionError(final String channelName, final String error) {

            }
        });
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest("test-channel");
        centrifugo.subscribe(subscriptionRequest);
        Assert.assertEquals("test-channel", channelSubscription.lockAndGet());

        final DataLock<DataMessage> messageData = new DataLock<>();
        centrifugo.setDataMessageListener(new DataMessageListener() {
            @Override
            public void onNewDataMessage(final DataMessage message) {
                messageData.setData(message);
            }
        });

        MediaType appJson = MediaType.parse("application/json");
        JSONObject msg = new JSONObject();
        msg.put("input", "Hello world");
        JSONObject jsonObject = sendMessageJson("test-channel", msg);
        String apiSign = Signing.generateApiToken(jsonObject.toString());
        Request post = new Request.Builder()
                .url(centrifugoApiAddress)
                .method("POST",
                        RequestBody.create(appJson, jsonObject.toString()))
                .header("X-API-Sign", apiSign)
                .header("Content-type", "application/json")
                .build();
        Response postMessage = okHttpClient.newCall(post).execute();
        Assert.assertEquals(200, postMessage.code());
        DataMessage dataMessage = messageData.lockAndGet();
        String input = dataMessage.getData().toString();
        Assert.assertEquals(msg.toString(), input);

        //disconnect after sending first message
        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());

        //send message while client is disconnected
        msg = new JSONObject();
        msg.put("input", "Hello world#2");
        jsonObject = sendMessageJson("test-channel", msg);
        apiSign = Signing.generateApiToken(jsonObject.toString());
        post = new Request.Builder()
                .url(centrifugoApiAddress)
                .method("POST",
                        RequestBody.create(appJson, jsonObject.toString()))
                .header("X-API-Sign", apiSign)
                .header("Content-type", "application/json")
                .build();
        postMessage = okHttpClient.newCall(post).execute();
        Assert.assertEquals(200, postMessage.code());

        final DataLock<DataMessage> recoveredMessageData = new DataLock<>();
        centrifugo.setDataMessageListener(new DataMessageListener() {
            @Override
            public void onNewDataMessage(final DataMessage message) {
                recoveredMessageData.setData(message);
            }
        });

        //connect again
        centrifugo.connect();
        Assert.assertTrue("Failed to connect to centrifugo", connected.lockAndGet());
        DataMessage recoveredMessage = recoveredMessageData.lockAndGet();
        String recoveredInput = recoveredMessage.getData().toString();
        Assert.assertEquals("{\"input\":\"Hello world#2\"}", recoveredInput);

        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());
    }

    private JSONObject sendMessageJson(final String channel, final JSONObject message) {
        JSONObject sendMessageJson = new JSONObject();
        try {
            sendMessageJson.put("method", "publish");
            JSONObject params = new JSONObject();
            params.put("channel", channel);
            params.put("data", message);
            sendMessageJson.put("params", params);
        } catch (JSONException e) {}
        return sendMessageJson;
    }

}

