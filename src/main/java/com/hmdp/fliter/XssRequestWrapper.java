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

//    运用过滤方法进行过滤
    @Override
    public String[] getParameterValues(String name) {
        String[] parameterValues = super.getParameterValues(name);
        String[] clearParameterValues=new String[parameterValues.length];
        if(parameterValues==null){
            return null;
        }else{
            for (int i = 0; i < parameterValues.length; i++) {
                clearParameterValues[i]=cleanXss(parameterValues[i]);
            }
        }
        return clearParameterValues;
    }
}
