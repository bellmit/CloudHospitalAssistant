package cn.neuedu.his.mapper;

import cn.neuedu.his.model.MedicalRecord;
import cn.neuedu.his.model.Registration;
import cn.neuedu.his.util.inter.MyMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component

public interface RegistrationMapper extends MyMapper<Registration> {
     List<Registration> getAllWaitingRegistration(Integer doctorID, Integer state, Date time);
     List<Registration> getRegistrationByPatientName(String name,Integer doctorID,Integer state);
     ArrayList<Registration> getAllRegistrationWaitingByPatientId(@Param("patientId") Integer patientId, @Param("state1") Integer state1, @Param("state2") Integer state2);
     ArrayList<Integer> getAllByDoctor(Integer doctorId, String start, String end, Integer state);
     Integer getRegistrationInof(Date time, Integer doctorId);
     Integer getRegistrationState(Integer id);
     Registration getRegistrationAndType(Integer id);
     ArrayList<Registration> getStatistics(String start,String end,Integer doctorId);
     ArrayList<Registration> getPatient(Integer doctorId,String start,String end,@Param("medicalTypeId") Integer medicalTypeId,Integer inspectionTypeId);
     ArrayList<Registration> getRegistrations(Integer id);
     Registration getFee(Integer registrationId,@Param("medicalTypeId") Integer medicalTypeId,Integer inspectionTypeId);
}