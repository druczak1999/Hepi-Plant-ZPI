package com.example.hepiplant.helper;

public class LangUtils {

    public static String getCommentsSuffix(final int commentsCount){
        final String comments_base = " komentarz";
        if(commentsCount == 1){
            return comments_base;
        }
        if(commentsCount%100 >= 2 && commentsCount%100 <= 4){
            return comments_base + "e";
        }
        return comments_base + "y";
    }

    public static String getFrequency(final String frequency){
        final String what = "Co";
        final String days = "dni";
        return what +" "+ frequency +" "+ days;
    }
}
