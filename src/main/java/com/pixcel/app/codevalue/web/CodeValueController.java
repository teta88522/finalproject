package com.pixcel.app.codevalue.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pixcel.app.codevalue.service.CodeValueService;
import com.pixcel.app.codevalue.service.CodeValueVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
//@RequestMapping("/codevalue")
public class CodeValueController {

    private final CodeValueService codeValueService;

    // 코드값 전체 조회 화면
    @GetMapping("/codevalue/list")
    public String codeValueList(Model model) {
        model.addAttribute("codeValueGroupMap", codeValueService.getCodeValueGroupMap());
        model.addAttribute("groupNameMap", getGroupNameMap());

        return "codevalue/list";
        
        
    }

    // 코드값 생성 화면
    @GetMapping("/codevalue/create")
    public String codeValueCreateForm(@RequestParam("settingGroupName") String settingGroupName,
                                      Model model) {
        CodeValueVO codeValue = new CodeValueVO();
        codeValue.setSettingGroupName(settingGroupName);
        codeValue.setDefaultYn("N");

        model.addAttribute("codeValue", codeValue);
        model.addAttribute("groupNameMap", getGroupNameMap());

        return "codevalue/create";
    }

    // 코드값 생성 처리
    @PostMapping("/codevalue/create")
    public String codeValueCreate(@ModelAttribute CodeValueVO codeValue,
                                  RedirectAttributes redirectAttributes) {
        try {
            codeValueService.createCodeValue(codeValue);
            redirectAttributes.addFlashAttribute("message", "코드값이 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/codevalue/create?settingGroupName=" + codeValue.getSettingGroupName();
        }

        return "redirect:/codevalue/list";
    }

    // 기본값 설정
    @PostMapping("/codevalue/default/set")
    public String setDefaultCodeValue(@RequestParam("settingCodeId") String settingCodeId,
                                      RedirectAttributes redirectAttributes) {
        try {
            codeValueService.setDefaultCodeValue(settingCodeId);
            redirectAttributes.addFlashAttribute("message", "기본값으로 설정되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/codevalue/list";
    }

    // 기본값 취소
    @PostMapping("/codevalue/default/cancel")
    public String cancelDefaultCodeValue(@RequestParam("settingCodeId") String settingCodeId,
                                         RedirectAttributes redirectAttributes) {
        try {
            codeValueService.cancelDefaultCodeValue(settingCodeId);
            redirectAttributes.addFlashAttribute("message", "기본값이 취소되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/codevalue/list";
    }

    // 코드값 삭제 요청
    @PostMapping("/codevalue/delete")
    public String deleteCodeValue(@RequestParam("settingCodeId") String settingCodeId,
                                  RedirectAttributes redirectAttributes) {
        try {
            boolean replacementRequired = codeValueService.isReplacementRequired(settingCodeId);

            if (replacementRequired) {
                return "redirect:/codevalue/delete/replace?settingCodeId=" + settingCodeId;
            }

            codeValueService.removeCodeValue(settingCodeId);
            redirectAttributes.addFlashAttribute("message", "코드값이 삭제되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/codevalue/list";
    }

    // 대체 코드값 선택 화면
    @GetMapping("/codevalue/delete/replace")
    public String replaceDeleteForm(@RequestParam("settingCodeId") String settingCodeId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            CodeValueVO deleteTarget = codeValueService.getCodeValueDetail(settingCodeId);
            List<CodeValueVO> replacementList = codeValueService.getReplacementCodeValueList(settingCodeId);

            model.addAttribute("deleteTarget", deleteTarget);
            model.addAttribute("replacementList", replacementList);
            model.addAttribute("groupNameMap", getGroupNameMap());

            return "codevalue/replace-delete";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/codevalue/list";
        }
    }

    // 대체 코드값 선택 후 삭제 처리
    @PostMapping("/codevalue/delete/replace")
    public String replaceDeleteCodeValue(@RequestParam("deleteSettingCodeId") String deleteSettingCodeId,
                                         @RequestParam("replaceSettingCodeId") String replaceSettingCodeId,
                                         RedirectAttributes redirectAttributes) {
        try {
            codeValueService.removeCodeValueWithReplacement(deleteSettingCodeId, replaceSettingCodeId);
            redirectAttributes.addFlashAttribute("message", "대체 코드값으로 변경 후 삭제되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/codevalue/delete/replace?settingCodeId=" + deleteSettingCodeId;
        }

        return "redirect:/codevalue/list";
    }

    // 코드값 그룹 표시명
    private Map<String, String> getGroupNameMap() {
        Map<String, String> groupNameMap = new LinkedHashMap<>();

        groupNameMap.put("g001", "작업분류");
        groupNameMap.put("g002", "일감 우선순위");
        groupNameMap.put("g003", "문서 유형");

        return groupNameMap;
    }
}