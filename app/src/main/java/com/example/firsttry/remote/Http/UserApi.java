package com.example.firsttry.remote.Http;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.firsttry.activity.message.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserApi {

    private static final String TAG = "UserApi";
    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.parse("application/json; charset=utf-8");

    // ① 用户相关回调接口（注册 / 登录 / 修改密码等）
    public interface UserCallback {
        void onSuccess(String tokenOrMessage);  // 对登录是 token，对其他接口可以是 message
        void onError(String message);
        void onFailure(IOException e);
    }

    // ② 消息列表回调接口（新增）
    public interface MessageListCallback {
        void onSuccess(List<Message> messages);
        void onError(String message);
        void onFailure(IOException e);
    }

    // ------------------- 注册 -------------------
    public static void register(String account,
                                String email,
                                String password,
                                String confirmedPassword,
                                String photo,
                                UserCallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", account);
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("confirmedPassword", confirmedPassword);
            // 新增头像字段（字段名按后端接口要求改）
            jsonObject.put("photo", photo);
        } catch (JSONException e) {
            callback.onError("请求数据构造失败");
            return;
        }

        String jsonStr = jsonObject.toString();
        Log.d(TAG, "register request json = " + jsonStr);

        RequestBody body = RequestBody.create(jsonStr, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(HttpClient.BASE_URL + "register/")   // 按需修改路径
                .post(body)
                .build();

        HttpClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resStr = response.body().string();
                Log.d(TAG, "raw response: " + resStr);

                try {
                    JSONObject resJson = new JSONObject(resStr);
                    String status  = resJson.optString("status");
                    String message = resJson.optString("message");

                    if ("success".equals(status)) {
                        // 从返回中取 token（字段名依后端约定调整）
                        String token = resJson.optString("token", null);
                        // 如果后端是把 token 放在 message 里，就改成：
                        // String token = resJson.optString("message", null);
                        callback.onSuccess(token);
                    } else {
                        callback.onError(message);
                    }
                } catch (JSONException e) {
                    callback.onError("数据解析失败");
                }
            }
        });
    }

    // ------------------- 登录 -------------------
    public static void login(String account, String password, UserCallback callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", account);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            callback.onError("请求数据构造失败");
            return;
        }

        String jsonStr = jsonObject.toString();
        Log.d(TAG, "login request json = " + jsonStr);

        RequestBody body = RequestBody.create(jsonStr, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(HttpClient.BASE_URL + "login/")   // 按需修改路径
                .post(body)
                .build();

        HttpClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resStr = response.body().string();
                Log.d(TAG, "raw response: " + resStr);

                try {
                    JSONObject resJson = new JSONObject(resStr);
                    String status  = resJson.optString("status");
                    String message = resJson.optString("message");

                    if ("success".equals(status)) {
                        // 从 JSON 中取出 token —— 字段名按你的接口改
                        String token = resJson.optString("message", null);
                        // 如果后端返回结构类似：
                        // { "status": "success", "token": "xxx" }
                        // 就改成：
                        // String token = resJson.optString("token", null);
                        callback.onSuccess(token);
                    } else {
                        callback.onError(message);
                    }
                } catch (JSONException e) {
                    callback.onError("数据解析失败");
                }
            }
        });
    }

    // ------------------- 忘记密码（发邮件） -------------------
    public static void forget_password(String email, UserCallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (JSONException e) {
            callback.onError("请求数据构造失败");
            return;
        }

        String jsonStr = jsonObject.toString();
        Log.d(TAG, "email request json = " + jsonStr);

        RequestBody body = RequestBody.create(jsonStr, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(HttpClient.BASE_URL + "sendEmail/")  // 按需修改路径
                .post(body)
                .build();

        HttpClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resStr = response.body().string();
                Log.d(TAG, "raw response: " + resStr);

                try {
                    JSONObject resJson = new JSONObject(resStr);
                    String status  = resJson.optString("status");
                    String message = resJson.optString("code");

                    if ("success".equals(status)) {
                        String code = resJson.optString("code", null);
                        callback.onSuccess(code);
                    } else {
                        callback.onError(message);
                    }
                } catch (JSONException e) {
                    callback.onError("邮件发送失败");
                }
            }
        });
    }

    // ------------------- 重置密码 -------------------
    public static void reset_password(String email, String password, String confirmPassword, UserCallback callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("newPassword", password);
            jsonObject.put("confirmedPassword", confirmPassword);
        } catch (JSONException e) {
            callback.onError("请求数据构造失败");
            return;
        }

        String jsonStr = jsonObject.toString();
        Log.d(TAG, "register request json = " + jsonStr);

        RequestBody body = RequestBody.create(jsonStr, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(HttpClient.BASE_URL + "resetpassword/") // 按需修改路径
                .post(body)
                .build();

        HttpClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resStr = response.body().string();
                Log.d(TAG, "raw response: " + resStr);

                try {
                    JSONObject resJson = new JSONObject(resStr);
                    String status = resJson.optString("status");
                    String message = resJson.optString("message");

                    if ("success".equals(status)) {
                        callback.onSuccess(message);
                    } else {
                        callback.onError(message);
                    }
                } catch (JSONException e) {
                    callback.onError("数据解析失败");
                }
            }
        });
    }

    // 在 UserApi.java 中

    public static void getMessages(String token, final MessageListCallback callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", "true");
            jsonObject.put("receiver", "wyt");
        } catch (JSONException e) {
            if (callback != null) callback.onError("请求数据构造失败");
            return;
        }
        String jsonStr = jsonObject.toString();

        // ... (RequestBody 和 Request 的构建保持不变)
        RequestBody body = RequestBody.create(jsonStr, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(HttpClient.BASE_URL + "getMessages/") // 确认 HttpClient 和 BASE_URL
                .post(body)
                // .addHeader("Authorization", "Bearer " + token) // 记得根据需要添加认证
                .build();

        HttpClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (callback != null) callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resStr = response.body() != null ? response.body().string() : "";
                response.close();

                if (!response.isSuccessful()) {
                    if (callback != null) callback.onError("服务器错误: " + response.code());
                    return;
                }

                try {
                    JSONObject resJson = new JSONObject(resStr);

                    // === 【核心修改点】直接处理 data 数组 ===
                    // 我们不再关心外层的 status，直接解析 data
                    JSONArray dataArray = resJson.optJSONArray("data");
                    List<Message> result = new ArrayList<>();

                    if (dataArray != null) {
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject msgJson = dataArray.optJSONObject(i);
                            if (msgJson == null) continue;

                            // 使用 Message.fromJson() 来统一解析逻辑
                            Message messageItem = Message.fromJson(msgJson);

                            if (messageItem != null) {
                                result.add(messageItem);
                            }
                        }
                    }

                    if (callback != null) {
                        callback.onSuccess(result);
                    }

                } catch (JSONException e) {
                    if (callback != null) {
                        callback.onError("消息数据解析失败");
                    }
                }
            }
        });
    }
}
