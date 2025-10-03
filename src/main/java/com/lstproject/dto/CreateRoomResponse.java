package com.lstproject.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomResponse {
//    private String roomId;
//    private String username;
    private List<UserInfoDTO> nameList;
    private String message;

}
