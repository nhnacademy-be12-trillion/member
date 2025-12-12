package com.nhnacademy.memberapi.domain.address.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.address.dto.AddressResponse;
import com.nhnacademy.memberapi.domain.address.dto.AddressUpdateRequest;
import com.nhnacademy.memberapi.domain.address.service.AddressService;
import com.nhnacademy.memberapi.security.WithMockAuthUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = AddressController.class)
@TestPropertySource(properties = {
        // Spring Cloud Config 설정 비활성화
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
})
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;



    @Test
    @DisplayName("주소 등록")
    @WithMockAuthUser
    void addAddress() throws Exception{
        AddressCreateRequest request = new AddressCreateRequest("12345", "기본주소", "상세주소", "별칭");

        doNothing().when(addressService).addAddress(anyLong(), any(AddressCreateRequest.class));
        mockMvc.perform(post("/api/members/addresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("전체 주소 조회")
    @WithMockAuthUser
    void getAllAddresses() throws Exception{
        when(addressService.getAllAddresses(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/members/addresses")
                .with(csrf()))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("특정 주소 조회")
    @WithMockAuthUser
    void getAddress() throws Exception{
        Long addressId = 1L;
        AddressResponse mockResponse = new AddressResponse(addressId, "12345", "기본주소", "상세주소", "별칭");

        when(addressService.getAddress(anyLong(),anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/members/addresses/{addressId}", addressId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(addressId));
    }

    @Test
    @DisplayName("주소 업데이트")
    @WithMockAuthUser
    void updateAddress() throws Exception{
        Long addressId = 1L;
        AddressUpdateRequest request = new AddressUpdateRequest("12345","기본주소", "상세주소","별칭");

        doNothing().when(addressService).updateAddress(anyLong(), anyLong(), any(AddressUpdateRequest.class));

        mockMvc.perform(put("/api/members/addresses/{addressId}", addressId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("주소 삭제")
    @WithMockAuthUser
    void deleteAddress() throws Exception{
        Long addressId = 1L;

        doNothing().when(addressService).deleteAddress(anyLong(), anyLong());

        mockMvc.perform(delete("/api/members/addresses/{addressId}",addressId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}