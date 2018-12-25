package cn.shuzilm.util;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;

import java.io.IOException;

public class MessageLiteToStringUtil {
    private static TextFormat.Printer singleLine = (new TextFormat.Printer()).setSingleLineMode(true)
            .setEscapeNonAscii(false);

    public  String toString(MessageLite lite) {
        if (lite == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        try {
            singleLine.print((MessageOrBuilder) lite, new TextFormat.TextGenerator(sb));
        } catch (IOException e) {
        }
        return sb.toString();
    }
    private static MessageLiteToStringUtil instance=new MessageLiteToStringUtil();
    public static MessageLiteToStringUtil getInstance(){
        return instance;
    }
}

