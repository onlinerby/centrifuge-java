package org.coindirect.centrifuge.java.it;

import org.coindirect.centrifuge.java.Centrifugo;
import org.coindirect.centrifuge.java.credentials.Token;
import org.coindirect.centrifuge.java.credentials.User;
import org.coindirect.centrifuge.java.subscription.SubscriptionRequest;
import org.coindirect.centrifuge.java.TestWebapp;
import org.coindirect.centrifuge.java.async.DeadLockException;
import org.coindirect.centrifuge.java.listener.ConnectionListener;
import org.coindirect.centrifuge.java.listener.DataMessageListener;
import org.coindirect.centrifuge.java.listener.SubscriptionListener;
import org.coindirect.centrifuge.java.message.DataMessage;
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
 * Created by semyon on 29.04.16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Ignore
public class BasicTests {

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
    public void testConnection() throws Exception {
        String containerIpAddress = centrifugo.getContainerIpAddress() + ":" + centrifugo.getMappedPort(8000);
        String centrifugoAddress = "ws://" + containerIpAddress + "/connection/websocket";

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
        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());
    }


    @Test
    public void testSubscribe() throws Exception {
        String containerIpAddress = centrifugo.getContainerIpAddress() + ":" + centrifugo.getMappedPort(8000);
        String centrifugoAddress = "ws://" + containerIpAddress + "/connection/websocket";

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

        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());
    }

    @Test
    public void testSubscribeBeforeConnect() throws Exception {
        String containerIpAddress = centrifugo.getContainerIpAddress() + ":" + centrifugo.getMappedPort(8000);
        String centrifugoAddress = "ws://" + containerIpAddress + "/connection/websocket";

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
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest("test-channel");
        centrifugo.subscribe(subscriptionRequest);

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
        Assert.assertEquals("test-channel", channelSubscription.lockAndGet());

        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());
    }

    @Test
    public void testSendReceiveMessage() throws Exception {
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

        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());
    }

    @Test(expected = DeadLockException.class)
    public void deadLockPreventionTest() throws Exception {
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
        final Centrifugo centrifugo = new Centrifugo.Builder(centrifugoAddress)
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
        final DataLock<Exception> exceptionDataLock = new DataLock<>();
        centrifugo.setDataMessageListener(new DataMessageListener() {
            @Override
            public void onNewDataMessage(final DataMessage message) {
                messageData.setData(message);
                try {
                    centrifugo.requestPresence("test-channel").blockingGet();
                } catch (Exception e) {
                    exceptionDataLock.setData(e);
                }
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

        Exception exception = exceptionDataLock.lockAndGet();

        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());

        throw exception;
    }

    private JSONObject sendMessageJson(final String channel, final JSONObject message) {
        JSONObject sendMessageJson = new JSONObject();
        try {
            sendMessageJson.put("method", "publish");
            JSONObject params = new JSONObject();
            params.put("channel", channel);
            params.put("data", message);
            sendMessageJson.put("params", params);
        } catch (JSONException e) {
        }
        return sendMessageJson;
    }

}
