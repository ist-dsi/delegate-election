package adapter;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import core.Student;

public class StudentAdapter implements JsonSerializer<Student> {
    @Override
    public JsonElement serialize(Student s, Type type, JsonSerializationContext jsc) {
        JsonParser parser = new JsonParser();

        JsonObject studentObject = new JsonObject();
        JsonObject photoObject = new JsonObject();

        studentObject.addProperty("name", s.getName());
        studentObject.addProperty("username", s.getUsername());

//        if (s.getPhotoBytes() == null) {
//            photoObject.add("data", parser.parse(""));
//            photoObject.add("type", parser.parse(""));
//        } else {
//            photoObject.add("data", parser.parse(s.getPhotoBytes()));
//            photoObject.add("type", parser.parse(s.getPhotoType()));
//        }
//        studentObject.add("photo", photoObject);

        return studentObject;
    }

}