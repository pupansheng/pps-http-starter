/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pu PanSheng, 2021/4/23
 * @version OPRA v1.0
 */
public class RegexUtil {
    public static String matchOne(Pattern pattern, String input){

        Matcher matcher = pattern.matcher(input);
        if(matcher.find()){
            return matcher.group();
        }
        return "";
    }
    public static List<String> matchMany(Pattern  pattern, String input){

        List<String> list=new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()){
            list.add(matcher.group());
        }
        return list;
    }

    /**
     * 注意特殊字符
     * @param start
     * @param end
     * @param content
     * @return
     */
    public static String findOneContentByStartAndEnd(String start,String end,String content){
        String parr="(?<=%s)[\\s\\S]*?(?=%s)";
        String format = String.format(parr, start, end);
        Matcher matcher = Pattern.compile(format).matcher(content);
        if(matcher.find()){
            return  matcher.group();
        }
        return "";
    }

    /**
     * 注意特殊字符
     * @param start
     * @param end
     * @param content
     * @return
     */
    public static List<String> findManyContentByStartAndEnd(String start, String end, String content){

        List<String> list=new ArrayList<>();
        String parr="(?<=%s)[\\s\\S]*?(?=%s)";
        String format = String.format(parr, start, end);
        Matcher matcher = Pattern.compile(format).matcher(content);
        while (matcher.find()){
            String group = matcher.group();
            list.add(group);
        }
        return list;
    }

    /**
     * 注意特殊字符
     * @param start
     * @param end
     * @param content
     * @return
     */
    public static String findOneContentByStartAndEndWith(String start,String end,String content){
        String parr="(?<=%s)[\\s\\S]*?(?=%s)";
        String re=start+"%s"+end;
        String format = String.format(parr, start, end);
        Matcher matcher = Pattern.compile(format).matcher(content);
        if(matcher.find()){
            return String.format(re,matcher.group()) ;
        }
        return "";
    }

    /**
     *
     * 注意特殊字符
     * @param start
     * @param end
     * @param content
     * @return
     */
    public static List<String> findManyContentByStartAndEndWith(String start, String end, String content){

        List<String> list=new ArrayList<>();
        String parr="(?<=%s)[\\s\\S]*?(?=%s)";
        String re=start+"%s"+end;
        String format = String.format(parr, start, end);
        Matcher matcher = Pattern.compile(format).matcher(content);
        while (matcher.find()){
            String group = matcher.group();
            list.add(String.format(re,group));
        }
        return list;
    }

    /**
     * 获得所有url 注意特殊字符需要加\\
     * @param prefix
     * @param suffix
     * @param content
     * @param mid
     * @return
     */
    public static List<String> findAllUrl(String prefix,String suffix,String content,String ... mid){

        String midS="[a-zA-z0-9]||/||:||;||\\.||\\?||\\&||=||#";
        for (int i = 0; i < mid.length; i++) {
            midS=midS+"||"+mid[i];
        }
        List<String> list=new ArrayList<>();
        String format = "%s("+midS+")*?%s";
        Matcher matcher = Pattern.compile(String.format(format,prefix,suffix)).matcher(content);
        while (matcher.find()){
            String group = matcher.group();
            list.add(group);
        }
        return list;

    }

    public static String findHost(String url){

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host1 = uri.getHost();
            return  host1;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public static String findScheme(String url){

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host1 = uri.getHost();
            return  scheme;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


    }

    public static String findUrlPrefx(String url){

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host1 = uri.getHost();
            return  scheme+"://"+host1;
        } catch (URISyntaxException e) {
               throw new RuntimeException(e);
        }

    }
}
