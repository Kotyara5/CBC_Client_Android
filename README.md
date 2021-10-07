# CBC_Client_Android

Простое приложение "CBC" на Android для общения между зарегистрированными пользователями.

Для работы приложения необходим активный сервер: https://github.com/Kotyara5/CBC_Server
Необходимо так же обновить ссылку на сервер в коде приложения.

Функциональность:
 - Форма регистрации и авторизации.
 - Список всех зарегистрированных пользователей. Возможность добавить пользователя в список друзей.
 - Список диалогов с друзьями. На каждом диалоге отображается последнее отправленное сообщение.
 - Общение с помощью отправки и получения сообщений в выбранном диалоге.

Приложение написано на java в Android Studio.
Отправка https запросов осуществляется с помощью Retrofit 2.
Отправка и получение сообщений (а так же обновление "последнего сообщения" в состоянии диалога) осуществляется через Websocket с помощью STOMP.

Дополнительно используется:
 - Форма сообщений: https://github.com/lguipeng/BubbleView
 - Поддержка Смайлов: https://github.com/hani-momanii/SuperNova-Emoji
 - Реализация STOMP: https://github.com/NaikSoftware/StompProtocolAndroid