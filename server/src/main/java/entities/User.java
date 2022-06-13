package entities;

public class User {

    private String uuid;
    private String login;
    private String password;
    private String name;
    private String email;
    private boolean isActive;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public User() {
    }

    public User(String login, String password, String name, String email) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.email = email;
        this.isActive = true;
        this.uuid = "";
    }

    public User(User user) {

        login = user.getLogin();
        password = user.getPassword();
        uuid = user.getUuid();
        name = user.getName();
        email = user.getEmail();
        isActive = user.isActive();
    }

    public User(String id, String name, String email, boolean isActive) {
        this.uuid = id;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
        this.login = "";
        this.password = "";
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }



    public boolean isActive() {
        return isActive;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
