package com.bc.communitybc.connection;

public class RegistrationRequest {
    private final String login;
    private final String password;
    private final String name;
    private final String email;

    public RegistrationRequest(String login, String password, String name, String email) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
