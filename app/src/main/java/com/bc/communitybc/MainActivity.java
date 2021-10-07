package com.bc.communitybc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bc.communitybc.connection.AuthRequest;
import com.bc.communitybc.connection.AuthResponse;
import com.bc.communitybc.connection.AuthenticationInterceptor;
import com.bc.communitybc.connection.InterfaceServerApi;
import com.bc.communitybc.connection.RegistrationRequest;
import com.bc.communitybc.dialog.DialogDto;
import com.bc.communitybc.dialog.DialogAdapter;
import com.bc.communitybc.dialog.MessageDto;
import com.bc.communitybc.dialog.MessageAdapter;
import com.bc.communitybc.user.UserDto;
import com.bc.communitybc.user.UserAdapter;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

import static com.bc.communitybc.dialog.EnumMessageType.CHAT;

public class MainActivity extends AppCompatActivity {

    public static final String SERVER_URL = "https://b943-62-176-30-195.ngrok.io"; //TODO нужно обновлять ссылку для тестов

    //Сохранение и загрузка данных с устройства
    //SharedPreferences sPref;

    //Основные поля
    private LinearLayout signIn_VerLayout, register_VerLayout, chat_VerLayout, friends_VerLayout, messages_Layout, profile_VerLayout;
    //private CheckBox signIn_checkBox;
    private RecyclerView list_of_friends, list_of_dialogs, list_of_all_users, list_of_messages;

    private static UserDto currentUserDto;
    private List<UserDto> allUserDto;
    private List<UserDto> friendsUserDto;
    private List<DialogDto> allDialogDtos;
    private List<MessageDto> allMessageDto;

    //Окно переписки
    private ImageButton sendButton; //Кнопка отправки сообщения
    private EmojiconEditText emojiconEditText; //Окно ввода текста и смайлов

    //REST
    private static OkHttpClient.Builder httpClient;
    private Retrofit retrofit;
    private InterfaceServerApi serverApi;

    //STOMP
    private StompClient mStompClient;
    private Disposable dispTopicMessageCurrentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView start_image = findViewById(R.id.start_imageView);
        start_image.setVisibility(View.VISIBLE);

        signIn_VerLayout = findViewById(R.id.signIn_VerLayout);
        register_VerLayout = findViewById(R.id.register_VerLayout);
        chat_VerLayout = findViewById(R.id.chat_VerLayout);
        messages_Layout = findViewById(R.id.messages_Layout);
        friends_VerLayout = findViewById(R.id.friends_VerLayout);
        profile_VerLayout = findViewById(R.id.profile_VerLayout);

        list_of_dialogs = findViewById(R.id.list_of_dialogs);
        list_of_friends = findViewById(R.id.list_of_friends);
        list_of_all_users = findViewById(R.id.list_of_all_users);
        list_of_messages = findViewById(R.id.list_of_messages);

        UserAdapter.OnItemClickListener friendsClickListener = new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserDto userDto, int position) {
                Toast.makeText(getApplicationContext(), "Это ваш друг: " + userDto.getName(), Toast.LENGTH_SHORT).show();
            }
        };

        //Нажатие по пользователю в списке всех пользователей
        //Данное действие добавит пользователя в друзья текущего пользователя или покажет ошибку с информацией, почему пользователя невозможно добавить в друзья
        UserAdapter.OnItemClickListener allUsersClickListener = new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserDto userDto, int position) {
                Long user_id = userDto.getId(); //Получаем ид (ключ) выбранного пользователя
                //Если пользователь нажал на себя
                if (user_id == currentUserDto.getId()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Вы не можете добавить себя в друзья", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                Call<String> call = serverApi.addNewFriend(user_id);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            //Итог регистрации
                            if (response.body() == null){
                                Toast toast = Toast.makeText(getApplicationContext(), call.toString(), Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, 0);
                                toast.show();
                            } else {
                                Toast toast;
                                switch (response.body()) {
                                    case "user added to friends":
                                        toast = Toast.makeText(getApplicationContext(), "Пользователь добавлен в друзья!", Toast.LENGTH_SHORT);
                                        break;
                                    case "the user is already a friend":
                                        toast = Toast.makeText(getApplicationContext(), "Пользователь уже ваш друг!", Toast.LENGTH_SHORT);
                                        break;
                                    default:
                                        toast = Toast.makeText(getApplicationContext(), call.toString(), Toast.LENGTH_SHORT);
                                        break;
                                }
                                toast.setGravity(Gravity.TOP, 0, 0);
                                toast.show();
                            }
                        } else {
                            Log.d("response code " + response.code(), response.errorBody().toString());
                            Toast.makeText(MainActivity.this, "response code " + response.code() + ": " + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("failure", t.getMessage());
                        Toast.makeText(MainActivity.this, "failure " + t, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

        DialogAdapter.OnItemClickListener dialogClickListener = new DialogAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DialogDto dialog, int position) {
                //Нажатие по диалогу откроет выбранный диалог
                displayAllMessagesOfDialog(dialog.getId());
            }
        };

        allUserDto = new ArrayList<>();
        list_of_all_users.setAdapter(new UserAdapter(this, allUserDto, allUsersClickListener));

        friendsUserDto = new ArrayList<>();
        list_of_friends.setAdapter(new UserAdapter(this, friendsUserDto, friendsClickListener));

        allDialogDtos = new ArrayList<>();
        list_of_dialogs.setAdapter(new DialogAdapter(this, allDialogDtos, dialogClickListener));

        //Адаптер для отображения списка сообщений
        allMessageDto = new ArrayList<>();
        list_of_messages.setAdapter(new MessageAdapter(this, allMessageDto));

        httpClient = new OkHttpClient.Builder();
        retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL + "/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        serverApi = retrofit.create(InterfaceServerApi.class);

        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "wss://" + SERVER_URL.substring(8) + "/ws/websocket");

        //Окно переписки
        sendButton = findViewById(R.id.sendButton);
        emojiconEditText = findViewById(R.id.textField);
        EmojIconActions emojIconActions = new EmojIconActions(getApplicationContext(), findViewById(R.id.main_layout), emojiconEditText, findViewById(R.id.emoji_Button));
        emojIconActions.ShowEmojIcon();

        //Чтение файла с устройства
        /*sPref = getSharedPreferences("MainData", MODE_PRIVATE);
        boolean m_isRememberUser = sPref.getBoolean("isRememberUser", false);

        //Если установлена настройка "Запомнить пользователя", то войти автоматически с сохранёнными данными
        if (m_isRememberUser) {
            String m_email = sPref.getString("email", "@a");
            String m_pass = sPref.getString("pass", "11");
            signInChat(m_email, m_pass);
        } else { //Иначе отобразить окно входа
            showSignInWindow();
        }*/

        //Кнопки для переключения между меню друзей, диалогов и профиля
        Button dialogs_button = findViewById(R.id.dialogs_button);
        Button friends_button = findViewById(R.id.friends_button);
        Button profile_button = findViewById(R.id.profile_button);
        dialogs_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friends_VerLayout.setVisibility(View.INVISIBLE);
                list_of_dialogs.setVisibility(View.VISIBLE);
                profile_VerLayout.setVisibility(View.INVISIBLE);

                updateDialogs();
            }
        });
        friends_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friends_VerLayout.setVisibility(View.VISIBLE);
                list_of_dialogs.setVisibility(View.INVISIBLE);
                profile_VerLayout.setVisibility(View.INVISIBLE);
            }
        });
        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friends_VerLayout.setVisibility(View.INVISIBLE);
                list_of_dialogs.setVisibility(View.INVISIBLE);
                profile_VerLayout.setVisibility(View.VISIBLE);
            }
        });
        //Кнопки для переключения между списоком всех пользователей и только друзьями
        Button only_my_friends_button = findViewById(R.id.only_my_friends_button);
        Button all_users_button = findViewById(R.id.all_users_button);
        only_my_friends_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_of_all_users.setVisibility(View.INVISIBLE);
                list_of_friends.setVisibility(View.VISIBLE);

                updateFriends();
            }
        });
        all_users_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_of_all_users.setVisibility(View.VISIBLE);
                list_of_friends.setVisibility(View.INVISIBLE);

                updateAllUsers();
            }
        });

        //Кнопка выхода из текущего аккаунта
        Button exit_button = findViewById(R.id.exit_button);
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSignInWindow();
            }
        });

        start_image.setVisibility(View.INVISIBLE);
        showSignInWindow();
    }
    private void showSignInWindow() {
        signIn_VerLayout.setVisibility(View.VISIBLE);
        chat_VerLayout.setVisibility(View.INVISIBLE);
        register_VerLayout.setVisibility(View.INVISIBLE);

        //Кнопки регистрации и авторизации
        Button register_Button = findViewById(R.id.register_button);
        Button signIn_Button = findViewById(R.id.signIn_button);
        //signIn_checkBox = findViewById(R.id.signIn_checkBox);
        register_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterWindow();
            }
        });
        signIn_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Проверка заполненности полей
                MaterialEditText login = findViewById(R.id.loginField_SignIn);
                MaterialEditText pass = findViewById(R.id.passwordField_SignIn);
                if (TextUtils.isEmpty(login.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Введите логин", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                if (TextUtils.isEmpty(pass.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Введите пароль", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                signIn(login.getText().toString(), pass.getText().toString());
            }
        });
    }
    private void showRegisterWindow(){
        signIn_VerLayout.setVisibility(View.INVISIBLE);
        register_VerLayout.setVisibility(View.VISIBLE);

        final MaterialEditText login = findViewById(R.id.loginField_Register);
        final MaterialEditText pass = findViewById(R.id.passwordField_Register);
        final MaterialEditText passToo = findViewById(R.id.passwordToo_Field_Register);
        final MaterialEditText name = findViewById(R.id.userNameField_Register);
        final MaterialEditText email = findViewById(R.id.emailField_Register);

        Button register = findViewById(R.id.register_buttonInReg);
        Button cancel = findViewById(R.id.cancel_button);
        //Выйти из окна регистрации в окно авторизации
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInWindow();
                login.setText("");
                pass.setText("");
                passToo.setText("");
                name.setText("");
                email.setText("");
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Проверка правильности заполненности полей регистрации
                if (TextUtils.isEmpty(login.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Поле логин не заполнено", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                if (pass.getText().toString().length() < 6) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Пароль должен иметь 6 или более символов", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                if (!passToo.getText().toString().equals(pass.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Пароли не совпадают", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                if (TextUtils.isEmpty(name.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Поле имя не заполнено", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                if (TextUtils.isEmpty(email.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Поле email не заполнено", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return;
                }
                //Регистрация пользователя в бд
                Call<String> call = serverApi.register(new RegistrationRequest(
                        login.getText().toString(), pass.getText().toString(), name.getText().toString(), email.getText().toString()));
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            //Итог регистрации
                            if (response.body() == null){
                                Toast toast = Toast.makeText(getApplicationContext(), call.toString(), Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, 0);
                                toast.show();
                            } else {
                                Toast toast;
                                switch (response.body()) {
                                    case "user is save":
                                        toast = Toast.makeText(getApplicationContext(), "Пользователь успешно зарегистрирован!", Toast.LENGTH_SHORT);
                                        showSignInWindow();
                                        login.setText("");
                                        pass.setText("");
                                        passToo.setText("");
                                        name.setText("");
                                        email.setText("");
                                        break;
                                    case "login exists":
                                        toast = Toast.makeText(getApplicationContext(), "Указанный логин уже существует, придумайте новый", Toast.LENGTH_SHORT);
                                        break;
                                    default:
                                        toast = Toast.makeText(getApplicationContext(), call.toString(), Toast.LENGTH_SHORT);
                                        break;
                                }
                                toast.setGravity(Gravity.TOP, 0, 0);
                                toast.show();
                            }
                        } else {
                            Log.d("response code " + response.code(), response.errorBody().toString());
                            Toast.makeText(MainActivity.this, "response code " + response.code() + ": " + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("failure", t.getMessage());
                        Toast.makeText(MainActivity.this, "failure " + t, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showMainWindow() {
        signIn_VerLayout.setVisibility(View.INVISIBLE);
        chat_VerLayout.setVisibility(View.VISIBLE);
    }

    private void signIn(String login, String pass){
        Call<AuthResponse> call = serverApi.auth(new AuthRequest(login, pass));
        call.enqueue(new Callback<AuthResponse>() {
             @Override
             public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                 if (response.isSuccessful()) {
                     //Вытащить все необходимые данные из полученного запроса
                     //Если при входе была установлена настройка "Запомнить пользователя", то данные входа записываются на устройство
                     /*if (signIn_checkBox.isChecked()) {
                         sPref = getSharedPreferences("MainData", MODE_PRIVATE);
                         SharedPreferences.Editor editor = sPref.edit();
                         editor.putString("email", email);
                         editor.putString("pass", pass);
                         editor.putBoolean("isRememberUser", true);
                         editor.apply();
                     }*/

                     //Текущий авторизованный пользователь
                     if (response.body() != null) {
                         currentUserDto = response.body().getUserDto();
                         TextView profile_textView = findViewById(R.id.profile_textView);
                         profile_textView.setText("Логин:" + currentUserDto.getLogin() + "\n Имя:" + currentUserDto.getName() +
                                 "\n email:" + currentUserDto.getEmail() + "\n Роль:" + currentUserDto.getRole());
                     }

                     //Вытащить токен и сохранить в перехватчик
                     String authToken = response.body().getTokenType() + " " + response.body().getAccessToken();
                     if (!TextUtils.isEmpty(authToken)) {
                         AuthenticationInterceptor interceptor =
                                 new AuthenticationInterceptor(authToken);

                         if (!httpClient.interceptors().contains(interceptor)) {
                             httpClient.addInterceptor(interceptor);

                             retrofit = new Retrofit.Builder()
                                     .baseUrl(SERVER_URL + "/")
                                     .addConverterFactory(ScalarsConverterFactory.create())
                                     .addConverterFactory(GsonConverterFactory.create())
                                     .client(httpClient.build())
                                     .build();
                             serverApi = retrofit.create(InterfaceServerApi.class);
                         }

                         //Подключить вебсокет (обмен сообщениями)
                         /*List<StompHeader> headers = new ArrayList<>();
                         headers.add(new StompHeader("Authorization", authToken));
                         mStompClient.connect(headers);*/
                         mStompClient.connect();
                     }

                     //Отобразить основной экран приложения с диалогами и друзьями
                     showMainWindow();
                 } else {
                     Log.d("response code " + response.code(), response.errorBody().toString());
                     Toast.makeText(MainActivity.this, "response code " + response.code() + ": " + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                 }
             }

             @Override
             public void onFailure(Call<AuthResponse> call, Throwable t) {
                 Log.d("failure", t.getMessage());
                 Toast.makeText(MainActivity.this, "failure " + t, Toast.LENGTH_LONG).show();
             }
         });
    }

    private void updateAllUsers() {
        Call<List<UserDto>> callAllUsers = serverApi.findAllUsers();
        callAllUsers.enqueue(new Callback<List<UserDto>>() {
            @Override
            public void onResponse(Call<List<UserDto>> call, Response<List<UserDto>> response) {
                if (response.isSuccessful()) {
                    Log.d("response ", String.valueOf(response.body().size()));
                    //Toast.makeText(MainActivity.this, "response " + response.body().size(), Toast.LENGTH_LONG).show();

                    allUserDto.clear();
                    allUserDto.addAll(response.body());
                    list_of_all_users.getAdapter().notifyDataSetChanged();
                } else {
                    Log.d("response code " + response.code(), response.errorBody().toString());
                    Toast.makeText(MainActivity.this, "response code " + response.code() + ": " + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserDto>> call, Throwable t) {
                Log.d("failure", t.getMessage());
                Toast.makeText(MainActivity.this, "failure " + t, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void updateFriends(){
        Call<List<UserDto>> callFriends = serverApi.findAllFriends();
        callFriends.enqueue(new Callback<List<UserDto>>() {
            @Override
            public void onResponse(Call<List<UserDto>> call, Response<List<UserDto>> response) {
                if (response.isSuccessful()) {
                    Log.d("response ", String.valueOf(response.body().size()));
                    //Toast.makeText(MainActivity.this, "response " + response.body().size(), Toast.LENGTH_LONG).show();
                    if(response.body() != null) {
                        friendsUserDto.clear();
                        friendsUserDto.addAll(response.body());
                        list_of_friends.getAdapter().notifyDataSetChanged();
                    }
                } else {
                    Log.d("response code " + response.code(), response.errorBody().toString());
                    Toast.makeText(MainActivity.this, "response code " + response.code() + ": " + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserDto>> call, Throwable t) {
                Log.d("failure", t.getMessage());
                Toast.makeText(MainActivity.this, "failure " + t, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateDialogs(){
        Call<List<DialogDto>> callDialogs = serverApi.findAllDialogs();
        callDialogs.enqueue(new Callback<List<DialogDto>>() {
            @Override
            public void onResponse(Call<List<DialogDto>> call, Response<List<DialogDto>> response) {
                if (response.isSuccessful()) {
                    Log.d("response ", String.valueOf(response.body().size()));
                    //Toast.makeText(MainActivity.this, "response " + response.body().size(), Toast.LENGTH_LONG).show();
                    if(response.body() != null) {
                        allDialogDtos.clear();
                        allDialogDtos.addAll(response.body());
                        list_of_dialogs.getAdapter().notifyDataSetChanged();

                        subscribeDialogs();
                    }
                } else {
                    Log.d("response code " + response.code(), response.errorBody().toString());
                    Toast.makeText(MainActivity.this, "response code " + response.code() + ": " + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<DialogDto>> call, Throwable t) {
                Log.d("failure", t.getMessage());
                Toast.makeText(MainActivity.this, "failure " + t, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayAllMessagesOfDialog(Long idOpenDialog) {
        chat_VerLayout.setVisibility(View.INVISIBLE);
        messages_Layout.setVisibility(View.VISIBLE);

        //Кнопка отправления сообщения
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Если поле пустое, то ничего не происходит
                if (TextUtils.isEmpty(emojiconEditText.getText().toString())) return;
                //Иначе добавляется новое сообщение
                MessageDto messageDto = new MessageDto(idOpenDialog, currentUserDto.getId(), "error_name", emojiconEditText.getText().toString(), CHAT, null);
                sendNewMessage(messageDto);
                emojiconEditText.setText("");
            }
        });

        //Загрузить все имеющиеся сообщения
        Call<List<MessageDto>> callMessage = serverApi.findAllMessagesOfDialog(idOpenDialog);
        callMessage.enqueue(new Callback<List<MessageDto>>() {
            @Override
            public void onResponse(Call<List<MessageDto>> call, Response<List<MessageDto>> response) {
                if (response.isSuccessful()) {
                    Log.d("response ", String.valueOf(response.body().size()));
                    //Toast.makeText(MainActivity.this, "response " + response.body().size(), Toast.LENGTH_LONG).show();
                    if(response.body() != null) {
                        allMessageDto.clear();
                        allMessageDto.addAll(response.body());
                        list_of_messages.getAdapter().notifyDataSetChanged();
                    }
                } else {
                    Log.d("response code " + response.code(), response.errorBody().toString());
                    Toast.makeText(MainActivity.this, "response code " + response.code() + ": " + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<MessageDto>> call, Throwable t) {
                Log.d("failure", t.getMessage());
                Toast.makeText(MainActivity.this, "failure " + t, Toast.LENGTH_LONG).show();
            }
        });

        //Подписаться на обновление сообщений в открытом диалоге
        dispTopicMessageCurrentDialog = mStompClient.topic("/queue/dialog_" + idOpenDialog.toString() + "/messages").subscribe(topicMessage -> {
            //Добавить сообщение в отображение
            Log.e("infoo", topicMessage.getPayload());
            Gson gson = new Gson();
            allMessageDto.add(gson.fromJson(topicMessage.getPayload(), MessageDto.class));
            //Только оригинальный поток может обновлять список(UI), иначе клиент отписывается
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    list_of_messages.getAdapter().notifyDataSetChanged();
                    list_of_messages.smoothScrollToPosition(list_of_messages.getAdapter().getItemCount());
                }
            });
        }, throwable -> {
            Log.e("ERROR", "Error on subscribe topic", throwable);
        });
    }

    public void sendNewMessage(MessageDto messageDto) {
        //Конвертировать объект (сообщение) в json-строку
        Gson gson = new Gson();
        String jsonStringMessage = gson.toJson(messageDto);

        mStompClient.send("/app/sendMessage", jsonStringMessage).subscribe();
    }

    @Override
    public void onBackPressed() {
        //Если открыт диалог, то перейти в окно списка диалогов
        if (messages_Layout.getVisibility() == View.VISIBLE) {
            //При выходе из диалога, сообщения перестают приходить прямо в диалог
            dispTopicMessageCurrentDialog.dispose();
            Log.e("infoo", "dispTopicMessageCurrentDialog.dispose");

            messages_Layout.setVisibility(View.INVISIBLE);
            chat_VerLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        mStompClient.disconnect();
        super.onDestroy();
    }

    public static long getIdCurrentUserDto(){
        return currentUserDto.getId();
    }

    public void subscribeDialogs(){
        for (DialogDto value : allDialogDtos)
            mStompClient.topic("/queue/dialog_" + value.getId()).subscribe(topicMessage -> {
                Log.e("getLastMessage", topicMessage.getPayload());
                Gson gson = new Gson();
                DialogDto dialogDto = gson.fromJson(topicMessage.getPayload(), DialogDto.class);

                for (DialogDto valueN : allDialogDtos)
                    if (valueN.getId() == dialogDto.getId())
                        valueN.setLastMessage(dialogDto.getLastMessage());

                //Только оригинальный поток может обновлять список(UI), иначе клиент отписывается
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list_of_dialogs.getAdapter().notifyDataSetChanged();
                    }
                });
            });

    }
}