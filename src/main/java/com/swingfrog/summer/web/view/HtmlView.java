package com.swingfrog.summer.web.view;

public class HtmlView extends TextView {

    public static HtmlView of(String title, String content) {
        return new HtmlView(title, content);
    }

    public static HtmlView of(String content) {
        return new HtmlView("", content);
    }

    public HtmlView(String title, String content) {
        super(String.format("<!DOCTYPE html>\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<meta charset=\"utf-8\">\n" +
                "\t\t<title>%s</title>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t%s\n" +
                "\t</body>\n" +
                "</html>\n", title, content));
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public String toString() {
        return "HtmlView";
    }

}
