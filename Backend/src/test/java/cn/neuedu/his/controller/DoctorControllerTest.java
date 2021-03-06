package cn.neuedu.his.controller;

import cn.neuedu.his.model.*;
import cn.neuedu.his.service.MedicalRecordService;
import cn.neuedu.his.service.MedicalRecordTemplateService;
import cn.neuedu.his.service.RegistrationService;
import cn.neuedu.his.service.impl.RedisServiceImpl;
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
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    RegistrationService registrationService;
    @Autowired
    MedicalRecordService medicalRecordService;
    private String token = "";

    @Before
    public void setUp() throws Exception {
        String token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, Constants.JWT_SECRET)
                .setHeaderParam("typ", Constants.TOKEN_TYPE)
                .setIssuer(Constants.TOKEN_ISSUER)
                .setAudience(Constants.TOKEN_AUDIENCE)
                .setSubject("ccm2")
                .setExpiration(new Date(System.currentTimeMillis() + Constants.EXPIRY_TIME))
                .claim("id", 12)
                .claim("typeId", 602)
                .compact();
        this.token = Constants.TOKEN_PREFIX + token;
//        mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilter(new JwtCheckAuthorizationFilter()).build();
    }
   /**
     * 暂存病历
     * @throws Exception
     */
    @Test
    public void saveTemporaryMR() throws Exception{
        Integer registrationId=1;
        MedicalRecord record=new MedicalRecord();
        record.setRegistrationId(1);
        record.setIsPregnant(false);
        record.setIsWesternMedicine(false);
        record.setPreviousTreatment("测试redis");
        JSONObject object=new JSONObject();
        object.put("registrationId", registrationId);
        object.put("medicalRecord", record);

        String  requestJson=object.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.post("/medical_record/saveTemporaryMR")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }
    /**
     * 获得暂存病历
     * @throws Exception
     */
    @Test
    public void getTemporaryMR() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/MRT/delete/19")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }
    /**
     * 删除暂存病历
     * @throws Exception
     */
    @Test
    public void deleteTemporaryMR() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/medical_record/deleteTemporaryMR/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 暂存检查/处置
     * @throws Exception
     */
    @Test
    public void saveTemporaryInspection() throws Exception{

        InspectionApplication i = new InspectionApplication();
        i.setQuantity(1);
        i.setNonDrugId(1);

        InspectionApplication i1 = new InspectionApplication();
        i1.setQuantity(2);
        i1.setNonDrugId(2);

        Prescription prescription = new Prescription();
        prescription.setDrugId(1);
        prescription.setNeedSkinTest(false);
        prescription.setNote("测试申请检查项目");
        prescription.setTemplate(false);
        prescription.setUsageId(Constants.Usage.SUBCUTANEOUSINJECTION.getId());
        prescription.setUseAmount("三个");
        prescription.setAmount(1);
        prescription.setFrequency("一天一次");
        prescription.setDays(2);

        List<Prescription> prescriptions=new ArrayList<>();
        List<InspectionApplication> applications=new ArrayList<>();

        prescriptions.add(prescription);
        applications.add(i);
        applications.add(i1);


        JSONObject object=new JSONObject();
        object.put("inspections",applications);
        object.put("prescriptions", prescriptions);
        object.put("registrationId", 1);

        String  requestJson=object.toJSONString();

        mockMvc.perform(MockMvcRequestBuilders.post("/inspection_application/saveTemporaryInspection")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 获得暂存的检查/处置
     * @throws Exception
     */
    @Test
    public void getTemporaryInspection() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/inspection_application/getTemporaryInspection/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void get() throws Exception {

        Map<String, Integer> map=redisService.getMapAll("userType");
        Set<String> set=map.keySet();
        for (String  key:set){
            System.out.println(key+" "+map.get(key));
        }
    }
    /**
     * 删除暂存的检查/处置
     * @throws Exception
     */
    @Test
    public void deleteTemporaryInspection() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/inspection_application/deleteTemporaryInspection/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }




    @Autowired
    RedisServiceImpl redisService;




    @Test
    public void getAllRecordByPatientId() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getAllRecord/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void getRegistrationByPatientName() throws Exception , AuthenticationServiceException {
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getByName/"+"小红")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void getAllWaitingRegistration() throws  Exception{
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date time=formatter.parse("2019-06-28 10:10:47", pos);
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getAllRegistration")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    //检查模板
    @Test
    public void getHospitalCheckTemps() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getHospitalCheckTemps")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getDeptCheckTemps() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getDeptCheckTemps")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getPersonalCheckTemps() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getPersonalCheckTemps")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    //病例模板
    @Test
    public void getHospitalMR() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getMRTemplate/"+Constants.HOSPITALLEVEL)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getDeptMR() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getMRTemplate/"+Constants.DEPTLEVEL)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getPersonalMR() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getMRTemplate/"+Constants.PERSONALLEVEL)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void findDiseaseByName() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/findDisease/霍乱")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getAllDiseases() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getAllDiseases")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void findNonDrugByName() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/findNonDrug/蛋白")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getAllNonDrug() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getAllNonDrug")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getResult() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/getResult/3")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void setFirstDiagnose() throws Exception, AuthenticationServiceException {

        MedicalRecord medicalRecord = medicalRecordService.findById(1);
        medicalRecord.setId(null);
        JSONObject object=new JSONObject();
        object.put("medicalRecordId", medicalRecord);
        object.put("registrationId", 1);

        ArrayList<Integer> diagnose=new ArrayList<>();
        diagnose.add(3);
        diagnose.add(4);
        object.put("diagnoses",diagnose );
        String requestJson=object.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/firstDiagnose")
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
    public void updateMR() throws Exception, AuthenticationServiceException {

        MedicalRecord medicalRecord = medicalRecordService.findById(1);

        medicalRecord.setSelfDescription("啦啦啦啦");



        JSONObject object=new JSONObject();

        List<Integer> list=new ArrayList<>();
        list.add(1);
        list.add(6);

        List<Integer> list2=new ArrayList<>();
        list2.add(8);
        list2.add(7);

        object.put("record", medicalRecord);
        object.put("firstDiagnose",list);
        object.put("finalDiagnose",list);

        String requestJson=object.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/updateMR")
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
    public void saveHospitalMRTemplate() throws Exception, AuthenticationServiceException {
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordWithDiagnose(1);
        JSONObject object=new JSONObject();
        object.put("medicalRecord", medicalRecord);
        object.put("name", "测试存入全院模板");
        String  requestJson=object.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/saveHospitalMRTemplate")
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
    public void saveDeptMRTemplate() throws Exception, AuthenticationServiceException {
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordWithDiagnose(1);
        JSONObject object=new JSONObject();
        object.put("medicalRecord", medicalRecord);
        object.put("name", "测试存入科室模板");
        String  requestJson=object.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/saveDeptMRTemplate")
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
    public void savePersonalMRTemplate() throws Exception, AuthenticationServiceException {
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordWithDiagnose(1);
        JSONObject object=new JSONObject();
        object.put("medicalRecord", medicalRecord);
        object.put("name", "测试存入个人模板");
        String  requestJson=object.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/savePersonalMRTemplate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }


    @Autowired
    MedicalRecordTemplateService templateService;


    @Test
    public void updateMedicalRecordTemp() throws Exception, AuthenticationServiceException {
        MedicalRecordTemplate medicalRecord = templateService.findById(1);
        JSONObject object=new JSONObject();
        medicalRecord.setName("测试更新病历模板");
        object.put("medicalRecordTemplate", medicalRecord);
        String  requestJson=object.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/updateMedicalRecordTemp")
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
    public void deleteMedicalRecord() throws  Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/deleteMedicalRecordTemp/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"));

    }


        @Test
    public void saveInspection() throws Exception {
        InspectionApplication i = new InspectionApplication();
        i.setQuantity(1);
        i.setNonDrugId(1);

        InspectionApplication i1 = new InspectionApplication();
        i1.setQuantity(2);
        i1.setNonDrugId(2);

        Prescription prescription = new Prescription();
        prescription.setDrugId(1);
        prescription.setNeedSkinTest(false);
        prescription.setNote("测试申请检查项目");
        prescription.setTemplate(false);
        prescription.setUsageId(Constants.Usage.SUBCUTANEOUSINJECTION.getId());
        prescription.setUseAmount("三个");
        prescription.setAmount(1);
        prescription.setFrequency("一天一次");
        prescription.setDays(2);

        List<Prescription> prescriptions=new ArrayList<>();
        List<InspectionApplication> applications=new ArrayList<>();

        prescriptions.add(prescription);
        applications.add(i);
        applications.add(i1);

        InspectionTemplate template=new InspectionTemplate();
        template.setApplications(applications);
        template.setPrescriptions(prescriptions);

        JSONObject object=new JSONObject();
        object.put("template",template);
        object.put("isDisposal", true);
        object.put("registrationId", 67);
        String  requestJson=object.toJSONString();

        mockMvc.perform(MockMvcRequestBuilders.post("/inspection_application/saveInspection")
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
    public void saveInspectionTem() throws Exception {
        InspectionApplication i = new InspectionApplication();
        i.setQuantity(1);
        i.setNonDrugId(1);

        InspectionApplication i1 = new InspectionApplication();
        i1.setQuantity(2);
        i1.setNonDrugId(2);

        Prescription prescription = new Prescription();
        prescription.setDrugId(1);
        prescription.setNeedSkinTest(false);
        prescription.setNote("测试申请检查项目");
        prescription.setTemplate(false);
        prescription.setUsageId(Constants.Usage.SUBCUTANEOUSINJECTION.getId());
        prescription.setUseAmount("三个");
        prescription.setAmount(1);
        prescription.setFrequency("一天一次");
        prescription.setDays(2);

        List<Prescription> prescriptions=new ArrayList<>();
        List<InspectionApplication> applications=new ArrayList<>();

        prescriptions.add(prescription);
        applications.add(i);
        applications.add(i1);

        InspectionTemplate template=new InspectionTemplate();
        template.setApplications(applications);
        template.setPrescriptions(prescriptions);
        template.setName("测试创建模板");
        template.setLevel(Constants.PERSONALLEVEL);

        JSONObject object=new JSONObject();
        object.put("template",template);

        String  requestJson=object.toJSONString();

        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/saveInspectionTem")
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
    public void updateInspectionTem() throws Exception {
        InspectionApplication i = new InspectionApplication();
        i.setQuantity(1);
        i.setNonDrugId(1);


        Prescription prescription = new Prescription();
        prescription.setDrugId(1);
        prescription.setNeedSkinTest(false);
        prescription.setNote("测试更新检查项目");
        prescription.setTemplate(false);
        prescription.setUsageId(Constants.Usage.SUBCUTANEOUSINJECTION.getId());
        prescription.setUseAmount("三个");
        prescription.setAmount(1);
        prescription.setFrequency("一天一次");
        prescription.setDays(2);
        prescription.setItemId(1);
        prescription.setCreateTime(new Date());

        List<Prescription> prescriptions=new ArrayList<>();
        List<InspectionApplication> applications=new ArrayList<>();

        prescriptions.add(prescription);
        applications.add(i);

        InspectionTemplate template=new InspectionTemplate();
        template.setApplications(applications);
        template.setPrescriptions(prescriptions);
        template.setName("测试更新模板");
        template.setLevel(Constants.PERSONALLEVEL);
        template.setId(1);
        template.setCreatedById(1);
        template.setDepartmentId(1);


        JSONObject object=new JSONObject();
        object.put("template",template);

        String  requestJson=object.toJSONString();

        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/updateInspectionTem")
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
    public void saveFinalDiagnose() throws Exception {
        JSONObject object=new JSONObject();

        object.put("registrationId", 1);
        object.put("medicalRecordId",1);

        ArrayList<Integer> diagnose=new ArrayList<>();
        diagnose.add(1);
        diagnose.add(2);

        object.put("diagnoses",diagnose);

        String  requestJson=object.toJSONString();

        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/finalDiagnose")
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
    public void savePrescriptions() throws Exception {
        JSONObject object=new JSONObject();

        Prescription prescription = new Prescription();
        prescription.setDrugId(1);
        prescription.setNeedSkinTest(false);
        prescription.setNote("测试申请成药项目");
        prescription.setTemplate(false);
        prescription.setUsageId(Constants.Usage.SUBCUTANEOUSINJECTION.getId());
        prescription.setUseAmount("三个");
        prescription.setAmount(1);
        prescription.setFrequency("一天一次");
        prescription.setDays(2);
        List<Prescription> prescriptions=new ArrayList<>();
        prescriptions.add(prescription);

        object.put("prescriptions",prescriptions);
        object.put("registrationId", 1);
        object.put("medicalRecordId", 1);
        String  requestJson=object.toJSONString();

        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/savePrescriptions")
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
    public void savePrescriptionTemp() throws Exception {
        JSONObject object=new JSONObject();

        Prescription prescription = new Prescription();
        prescription.setDrugId(1);
        prescription.setNeedSkinTest(false);
        prescription.setNote("测试模板");
        prescription.setTemplate(false);
        prescription.setUsageId(Constants.Usage.SUBCUTANEOUSINJECTION.getId());
        prescription.setUseAmount("三个");
        prescription.setAmount(1);
        prescription.setFrequency("一天一次");
        prescription.setDays(2);
        List<Prescription> prescriptions=new ArrayList<>();
        prescriptions.add(prescription);

        DrugTemplate template=new DrugTemplate();
        template.setPrescriptions(prescriptions);
        template.setName("测试模板1");
        template.setLevel(Constants.PERSONALLEVEL);


        object.put("template",template);
//        object.put("registrationId", 1);
        object.put("medicalRecordId", 1);
        String  requestJson=object.toJSONString();

        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/savePrescriptionTemp")
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
    public void finishDiagnose() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/finishDiagnose/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void paymentDetails() throws  Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/doctor/paymentDetails/1/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }



    @Test
    public void getDoctorTotal() throws  Exception{
        Date start=new Date();
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
        start=ft.parse("2019-05-28 00:00:00");
        Date end=new Date(System.currentTimeMillis());

        JSONObject object=new JSONObject();
        object.put("doctorId", 1);
        object.put("start", "2019-05-28 00:00:00");
        object.put("end", "2019-06-05 00:00:00");

        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/getDoctorTotal")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(Constants.TOKEN_HEADER, token)
                .content(object.toJSONString())
                .accept(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("100"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getDoctorWorkload() throws Exception {
        JSONObject param = new JSONObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = new Date();
        Date end = new Date();
        try {
            start = formatter.parse("2019-05-29 06:00:00");
            end = formatter.parse("2019-06-13 00:24:48");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        param.put("start", start);
        param.put("end", null);
        String requestJson = param.toJSONString();
        mockMvc.perform(MockMvcRequestBuilders.post("/doctor/getDoctorWorkload")
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