package org.coindirect.centrifuge.java.it;

import org.coindirect.centrifuge.java.Centrifugo;
import org.coindirect.centrifuge.java.credentials.Token;
import org.coindirect.centrifuge.java.credentials.User;
import org.coindirect.centrifuge.java.subscription.SubscriptionRequest;
import org.coindirect.centrifuge.java.TestWebapp;
import org.coindirect.centrifuge.java.listener.ConnectionListener;
import org.coindirect.centrifuge.java.listener.JoinLeaveListener;
import org.coindirect.centrifuge.java.listener.SubscriptionListener;
import org.coindirect.centrifuge.java.message.presence.JoinMessage;
import org.coindirect.centrifuge.java.message.presence.LeftMessage;
import org.coindirect.centrifuge.java.message.presence.PresenceMessage;
import org.coindirect.centrifuge.java.util.DataLock;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;

import javax.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 02.05.2016.
 * */
@RunWith(RobolectricGradleTestRunner.class)
@Ignore
public class PresenceTests {

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
    public void testJoined() throws Exception {
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

        final DataLock<JoinMessage> joinMessageDataLock = new DataLock<>();
        centrifugo.setJoinLeaveListener(new JoinLeaveListener() {
            @Override
            public void onJoin(final JoinMessage joinMessage) {
                joinMessageDataLock.setData(joinMessage);
            }

            @Override
            public void onLeave(final LeftMessage leftMessage) {

            }
        });

        Centrifugo anotherClient = addAnotherClient(1);
        Assert.assertNotNull(joinMessageDataLock.lockAndGet());

        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());

        anotherClient.disconnect();
    }

    @Test
    public void testLeft() throws Exception {
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

        final DataLock<JoinMessage> joinMessageDataLock = new DataLock<>();
        final DataLock<LeftMessage> leftMessageDataLock = new DataLock<>();
        centrifugo.setJoinLeaveListener(new JoinLeaveListener() {
            @Override
            public void onJoin(final JoinMessage joinMessage) {
                joinMessageDataLock.setData(joinMessage);
            }

            @Override
            public void onLeave(final LeftMessage leftMessage) {
                leftMessageDataLock.setData(leftMessage);
            }
        });

        Centrifugo anotherClient = addAnotherClient(1);
        Assert.assertNotNull(joinMessageDataLock.lockAndGet());
        anotherClient.disconnect();
        Assert.assertNotNull(leftMessageDataLock.lockAndGet());

        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());
    }

    @Test
    public void testPresenceRequest() throws Exception {
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

        final DataLock<JoinMessage> joinMessageDataLock = new DataLock<>();
        final DataLock<LeftMessage> leftMessageDataLock = new DataLock<>();
        centrifugo.setJoinLeaveListener(new JoinLeaveListener() {
            @Override
            public void onJoin(final JoinMessage joinMessage) {
                joinMessageDataLock.setData(joinMessage);
            }

            @Override
            public void onLeave(final LeftMessage leftMessage) {
                leftMessageDataLock.setData(leftMessage);
            }
        });

        Centrifugo anotherClient = addAnotherClient(1);
        Assert.assertNotNull(joinMessageDataLock.lockAndGet());
        PresenceMessage presenceMessage = centrifugo.requestPresence("test-channel").blockingGet();
        Assert.assertEquals(2, presenceMessage.getUserList().size());
        anotherClient.disconnect();
        Assert.assertNotNull(leftMessageDataLock.lockAndGet());

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

    private Centrifugo addAnotherClient(final int num) throws IOException, JSONException {
        String containerIpAddress = centrifugo.getContainerIpAddress() + ":" + centrifugo.getMappedPort(8000);
        String centrifugoAddress = "ws://" + containerIpAddress + "/connection/websocket";

        OkHttpClient okHttpClient = new OkHttpClient();

        String url = mockWebServer.url("/tokens").toString();
        Request build = new Request.Builder().url(url).header("num", "" + num).build();
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
        return centrifugo;
    }

}
