package com.bc.communitybc.connection;

import com.bc.communitybc.dialog.DialogDto;
import com.bc.communitybc.dialog.MessageDto;
import com.bc.communitybc.user.UserDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface InterfaceServerApi {
    @GET("users/findAll")
    Call<List<UserDto>> findAllUsers();

    @POST("users/register")
    Call<String> register(@Body RegistrationRequest request); //Вернёт строку, что всё работает или логин уже существует
    @POST("users/auth")
    Call<AuthResponse> auth(@Body AuthRequest request);

    @POST("users/addNewFriend")
    Call<String> addNewFriend(@Query("friend_id") Long friend_id);
    @GET("users/findAllFriends")
    Call<List<UserDto>> findAllFriends();

    @GET("dialogs/findAllDialogs")
    Call<List<DialogDto>> findAllDialogs();
    @GET("dialogs/findAllMessagesOfDialog")
    Call<List<MessageDto>> findAllMessagesOfDialog(@Query("dialog_id") Long dialog_id);
}
