package cn.neuedu.his.controller;

import cn.neuedu.his.util.constants.Constants;
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

import java.sql.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MedicalRecordTest {
    @Autowired
    private MockMvc mockMvc;
    private String token = "";

    @Before
    public void setUp() throws Exception {
        String token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, Constants.JWT_SECRET)
                .setHeaderParam("typ", Constants.TOKEN_TYPE)
                .setIssuer(Constants.TOKEN_ISSUER)
                .setAudience(Constants.TOKEN_AUDIENCE)
                .setSubject("ccmccm")
                .setExpiration(new Date(System.currentTimeMillis() + Constants.EXPIRY_TIME))
                .claim("id", 1)
                .claim("typeId", 605)
                .compact();
        this.token = Constants.TOKEN_PREFIX + token;
//        mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilter(new JwtCheckAuthorizationFilter()).build();
    }
    @Test
    public void deleteMedicalRecordTemp() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/medical_record/getDrugNonDrugAndResult/22")
                .contentType(MediaType.APPLICATION_JSON)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());

    }
}
