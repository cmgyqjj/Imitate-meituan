package com.hmdp.fliter;

import org.owasp.validator.html.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    private static String antisamyPath = XssRequestWrapper.class.getClassLoader().getResource("antisamy-test.xml").getFile();
    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public Policy policy=null;
    static{
        try {
            Policy.getInstance(antisamyPath);
        } catch (PolicyException e) {
            throw new RuntimeException(e);
        }
    }

//    通过AntiSmay框架过滤字符
    public String cleanXss(String text){
        AntiSamy antiSamy = new AntiSamy();
        try {
            CleanResults cleanResults = antiSamy.scan(text, policy);
            text=cleanResults.getCleanHTML();
        } catch (ScanException e) {
            throw new RuntimeException(e);
        } catch (PolicyException e) {
            throw new RuntimeException(e);
        }
        return text;
    }

    @Override
    public String[] getParameterValues(String name) {
        return super.getParameterValues(name);
    }
}
