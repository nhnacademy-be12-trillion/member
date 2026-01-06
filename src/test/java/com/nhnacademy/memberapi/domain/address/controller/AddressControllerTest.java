package com.nhnacademy.memberapi.domain.address.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.memberapi.domain.address.dto.AddressCreateRequest;
import com.nhnacademy.memberapi.domain.address.dto.AddressResponse;
import com.nhnacademy.memberapi.domain.address.dto.AddressUpdateRequest;
import com.nhnacademy.memberapi.domain.address.service.AddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
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

    private static final String BASE_URL = "/members/addresses";
    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final Long TEST_MEMBER_ID = 1L;

    @Test
    @DisplayName("주소 등록 성공 - 201 Created")
    void addAddress() throws Exception {
        AddressCreateRequest request = new AddressCreateRequest("12345", "기본주소", "상세주소", "별칭");
        doNothing().when(addressService).addAddress(anyLong(), any(AddressCreateRequest.class));

        mockMvc.perform(post(BASE_URL)
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("전체 주소 조회 성공 - 200 OK")
    void getAllAddresses() throws Exception {
        AddressResponse mockResponse = new AddressResponse(10L, "12345", "기본주소", "상세주소", "별칭");
        when(addressService.getAllAddresses(TEST_MEMBER_ID)).thenReturn(List.of(mockResponse));

        mockMvc.perform(get(BASE_URL)
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addressId").value(10L))
                // 수정: zipCode -> addressPostCode
                .andExpect(jsonPath("$[0].addressPostCode").value("12345"));
    }

    @Test
    @DisplayName("특정 주소 조회 성공 - 200 OK")
    void getAddress() throws Exception {
        Long addressId = 1L;
        AddressResponse mockResponse = new AddressResponse(addressId, "12345", "기본주소", "상세주소", "별칭");

        when(addressService.getAddress(TEST_MEMBER_ID, addressId)).thenReturn(mockResponse);

        mockMvc.perform(get(BASE_URL + "/{addressId}", addressId)
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(addressId))
                // 수정: zipCode -> addressPostCode
                .andExpect(jsonPath("$.addressPostCode").value("12345"));
    }

    @Test
    @DisplayName("주소 수정 성공 - 204 No Content")
    void updateAddress() throws Exception {
        Long addressId = 1L;
        AddressUpdateRequest request = new AddressUpdateRequest("54321", "새주소", "새상세", "새별칭");

        doNothing().when(addressService).updateAddress(eq(TEST_MEMBER_ID), eq(addressId), any(AddressUpdateRequest.class));

        mockMvc.perform(put(BASE_URL + "/{addressId}", addressId)
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("주소 삭제 성공 - 204 No Content")
    void deleteAddress() throws Exception {
        Long addressId = 1L;

        doNothing().when(addressService).deleteAddress(TEST_MEMBER_ID, addressId);

        mockMvc.perform(delete(BASE_URL + "/{addressId}", addressId)
                        .header(MEMBER_ID_HEADER, TEST_MEMBER_ID))
                .andExpect(status().isNoContent());
    }
}