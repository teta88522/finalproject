package com.pixcel.app.manage.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pixcel.app.manage.service.ManageGroupVO;
import com.pixcel.app.manage.service.ManageProjectVO;
import com.pixcel.app.manage.service.ManageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manage")
public class ManageController {

    private final ManageService manageService;

    @GetMapping("/groups")
    public String manageGroupList(
            @CookieValue(value = "userId", required = false) String userId,
            @CookieValue(value = "user_pk", required = false) String userPk,
            @CookieValue(value = "subscribeYn", required = false) String subscribeYn,
            Model model) {

        // 구독 승인 관리자만 접근 가능
        if (!"Y".equals(subscribeYn)) {
            return "redirect:/project";
        }

        // 프로젝트 owner_id로 쓸 로그인 사용자 PK
        String ownerId = userId;

        if (ownerId == null || ownerId.equals("")) {
            ownerId = userPk;
        }

        if (ownerId == null || ownerId.equals("")) {
            return "redirect:/login";
        }

        List<ManageProjectVO> myProjectList = manageService.selectMyManageProjectList(ownerId);
        List<ManageGroupVO> manageGroupList = manageService.selectMyManageGroupList(ownerId);

        model.addAttribute("subscribeYn", subscribeYn);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("myProjectList", myProjectList);
        model.addAttribute("manageGroupList", manageGroupList);

        return "manage/group/manageGroupList";
    }
}