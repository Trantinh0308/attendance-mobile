package com.example.attendance.model;

import android.annotation.SuppressLint;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class SqlDateTypeAdapter extends TypeAdapter<Date> {

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(dateFormat.format(value));
        }
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String dateStr = in.nextString();
        try {
            return new Date(Objects.requireNonNull(dateFormat.parse(dateStr)).getTime());
        } catch (ParseException e) {
            throw new JsonParseException("Failed parsing '" + dateStr + "' as SQL Date; " + e.getMessage());
        }
    }
}