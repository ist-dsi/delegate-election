package core;

import com.google.gson.JsonObject;

public class StudentFactory {

    private static StudentFactory instance;

    private StudentFactory() {
    }

    public static StudentFactory getInstance() {
        if (instance == null) {
            instance = new StudentFactory();
        }
        return instance;
    }

    public Student createStudent(JsonObject json) {

        final String username = json.get("username").toString();
        final String name = json.get("name").toString();
        final String email = json.get("email").toString();
        //byte[] photo = (byte[]) json.get("photo.data").toString();
        final JsonObject photodetails = json.getAsJsonObject("photo");
        final String type = photodetails.get("type").toString();
        final String photoData = photodetails.get("data").toString();
        return new Student(username, name, email, type, photoData);
    }
}
