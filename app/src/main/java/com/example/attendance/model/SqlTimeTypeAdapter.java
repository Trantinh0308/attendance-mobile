package com.example.attendance.model;

import android.annotation.SuppressLint;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class SqlTimeTypeAdapter extends TypeAdapter<Time> {

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public void write(JsonWriter out, Time value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(timeFormat.format(value));
        }
    }

    @Override
    public Time read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String timeStr = in.nextString();
        try {
            return new Time(Objects.requireNonNull(timeFormat.parse(timeStr)).getTime());
        } catch (ParseException e) {
            throw new JsonParseException("Failed parsing '" + timeStr + "' as SQL Time; " + e.getMessage());
        }
    }
}