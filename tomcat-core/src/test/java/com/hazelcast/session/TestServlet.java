/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (req.getRequestURI().endsWith("write")) {
            session.setAttribute("key", "value");
            resp.getWriter().write("true");
        } else if (req.getRequestURI().endsWith("read")) {
            Object value = session.getAttribute("key");
            resp.getWriter().write(value == null ? "null" : value.toString());
        } else if (req.getRequestURI().endsWith("remove")) {
            session.removeAttribute("key");
            resp.getWriter().write("true");
        } else if (req.getRequestURI().endsWith("invalidate")) {
            session.invalidate();
            resp.getWriter().write("true");
        } else if (req.getRequestURI().endsWith("update")) {
            session.setAttribute("key", "value-updated");
            resp.getWriter().write("true");
        } else if (req.getRequestURI().endsWith("names")) {
            List names = Collections.list(session.getAttributeNames());
            String nameList = names.toString();
            //return comma separated list of attribute names
            resp.getWriter().write(nameList.substring(1, nameList.length() - 1).replace(", ", ","));
        } else if (req.getRequestURI().endsWith("reload")) {
            session.invalidate();
            session = req.getSession();
            session.setAttribute("first-key", "first-value");
            session.setAttribute("second-key", "second-value");
            resp.getWriter().write("true");
        } else if (req.getRequestURI().endsWith("isNew")) {
            resp.getWriter().write(session.isNew() ? "true" : "false");
        } else if (req.getRequestURI().endsWith("lastAccessTime")) {
            resp.getWriter().write(String.valueOf(session.getLastAccessedTime()));
        } else if (req.getRequestURI().endsWith("nonserializable")) {
            session.setAttribute("key", new Object());
            resp.getWriter().write("true");
        } else if (req.getRequestURI().endsWith("write-custom-attribute")) {
            session.setAttribute("key", new CustomAttribute("value"));
            resp.getWriter().write("true");
        } else if (req.getRequestURI().endsWith("read-custom-attribute")) {
            CustomAttribute value = (CustomAttribute) session.getAttribute("key");
            resp.getWriter().write(value == null ? "null" : value.toString());
        }  else if (req.getRequestURI().endsWith("get-session-id")) {
            resp.getWriter().write(req.getSession().getId());
        }
    }
}
