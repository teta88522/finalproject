package com.pixcel.app.issuestatus.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IssueStatusVO {

    private String issueStatusId;

    private String statusName;

    private String statusAnswer;

    private String closedYn;

    private List<String> closedYnList;
    
    private String userId;
    
    private String initialStatusName;
}
