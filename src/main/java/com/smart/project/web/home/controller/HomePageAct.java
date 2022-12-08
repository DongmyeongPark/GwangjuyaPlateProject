package com.smart.project.web.home.controller;

import com.smart.project.proc.Test;
import com.smart.project.web.home.act.HomeDataAct;
import com.smart.project.web.home.vo.CommonMemberVO;
import com.smart.project.web.home.vo.KakaoMemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomePageAct {

    final private Test test;
    final private BCryptPasswordEncoder encoder;

    final private HomeDataAct homeDataAct;

    //일반 회원 로그인
    @PostMapping("/commonLogin")
    public String commonLogin(CommonMemberVO vo, HttpSession session, HttpServletRequest request){
        String userId = vo.getUserId();
        String userPw = request.getParameter("userPw");
        CommonMemberVO result  = test.selectOneMem(userId);
        if(result!= null && encoder.matches(userPw, result.getUserPw())){
            String userEmail = result.getUserEmail();
            session.setAttribute("email",result.getUserEmail());
            HttpServletRequest useremail = request;
            useremail.getSession().getAttribute("email");
            homeDataAct.wishSelect(useremail);
        }else{
            return "Member/login/login";
        }
        return "redirect:/";
    }

    //가입
    @PostMapping("/register")
    public String createMember(CommonMemberVO vo) {
        String securityPw = encoder.encode(vo.getUserPw());
        vo.setUserPw(securityPw);
        test.insertMember(vo);
        return "redirect:/";
    }
    
    //카카오 로그인 데이터 저장
    @RequestMapping("/kakaoJoin")
    public String kakaoJoin(@ModelAttribute KakaoMemberVO vo, HttpSession session, HttpServletResponse response) {
        if(!(vo.getEmail().equals(""))){
            session.setAttribute("email",vo.getEmail());
        }
        test.kakaoJoin(vo);
        return "redirect:/";
    }
    //로그아웃
    @RequestMapping("/logout")
    public String logoutMainGET(HttpServletRequest request) throws Exception{
        HttpSession session = request.getSession();
        session.invalidate();
        request.getSession(true);
        return "redirect:/";
    }

    @PostMapping("/FindId")
    public String FindId(CommonMemberVO vo, HttpServletResponse response) throws Exception {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        String userEmail = vo.getUserEmail();
        String userName = vo.getUserName();
        CommonMemberVO result = test.findMemberId(userEmail, userName);
        if(result != null) {
            out.println("<script>alert('아이디는 "+result.getUserId()+" 입니다');</script>");
        }
        else {
            out.println("<script>alert('가입된 아이디가 없습니다');</script>");

        }
        return "Member/login/password";
    }

    @PostMapping("/FindPw")
    public String FindPw(CommonMemberVO vo, HttpServletResponse response,HttpSession session) throws Exception {

        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();

        String userEmail = vo.getUserEmail();
        String userName = vo.getUserName();
        String userPhoneNum = vo.getUserPhoneNum();

        CommonMemberVO result = test.findMemberPw(userEmail, userName, userPhoneNum);

        if(result != null) {
            session.setAttribute("userEmail",userEmail);
            out.println("<script>alert('비밀번호를 변경해주세요.');</script>");
            return "Member/login/updatePw";
        }
        else {
            out.println("<script>alert('가입된 비밀번호가 없습니다');</script>");
            return "Member/login/password";
        }
    }

    @PostMapping("/updatePw")
    public String updatePw(CommonMemberVO vo, HttpSession session) {

        String userEmail = (String) session.getAttribute("userEmail");
        String userPw = vo.getUserPw();

        String securityPw = encoder.encode(userPw);
        test.updateMemberPw(userEmail, securityPw);

        return "Member/login/login";

    }

}
