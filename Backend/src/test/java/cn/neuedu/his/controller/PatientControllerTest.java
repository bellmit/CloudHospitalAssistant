package cn.neuedu.his.controller;

import cn.neuedu.his.util.constants.Constants;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PatientControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private String token = "";

    @Autowired
    private RegistrationController registrationController;

    @Before
    public void setUp() throws Exception {
        String token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, Constants.JWT_SECRET)
                .setHeaderParam("typ", Constants.TOKEN_TYPE)
                .setIssuer(Constants.TOKEN_ISSUER)
                .setAudience(Constants.TOKEN_AUDIENCE)
                .setSubject("Alex")
                .setExpiration(new Date(System.currentTimeMillis() + Constants.EXPIRY_TIME))
                .claim("id", 9)
                .claim("typeId", 604)
                .compact();
        this.token = Constants.TOKEN_PREFIX + token;
//        mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilter(new JwtCheckAuthorizationFilter()).build();
    }

    @Test
    public void getAllPayment() throws Exception {
        JSONObject param = new JSONObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = new Date();
        Date end = new Date();
        try {
            start = formatter.parse("2019-05-29 06:11:29");
            end = formatter.parse("2019-06-04 00:18:35");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        param.put("patientId", 1);
        param.put("startDate", start);
//        param.put("endDate", end);
////        param.put("startDate", null);
        param.put("endDate", null);

        String requestJson = param.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.get("/patient/getAllPayment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void getNotPaidPayment() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/patient/getNotPaidPayment/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getNotConsumePaymentAndPatient() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/patient/getNotConsumePayment/21")
                .contentType(MediaType.APPLICATION_JSON)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getDrugTakenInfo() throws Exception {
        JSONObject param = new JSONObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date start = new Date();
        Date end = new Date();
        try {
            start = formatter.parse("2019-06-10");
            end = formatter.parse("2019-06-10");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        param.put("patientId", 1);
        param.put("start", start);
        param.put("end", end);

        String requestJson = param.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.get("/patient/getDrug")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getDrugDuringDateInfo() throws Exception {
        JSONObject param = new JSONObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date start = new Date();
        Date end = new Date();
        try {
            start = formatter.parse("2019-05-29");
            end = formatter.parse("2019-06-04");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        param.put("patientId", 1);
        param.put("start", start);
        param.put("end", end);

        String requestJson = param.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.get("/patient/retreatDrug")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }
}