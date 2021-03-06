package cn.neuedu.his.service;
import cn.neuedu.his.model.*;
import cn.neuedu.his.util.inter.Service;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by ccm on 2019/05/24.
 */
public interface PaymentService extends Service<Payment> {
    List<Payment> getByDoctor(Integer patientId,Integer doctorId);
    List<Payment> getAll(Integer patientId,Date start,Date end);


    Payment createRegistrationPayment(Integer registrationId) throws IllegalArgumentException, IndexOutOfBoundsException;
    Invoice payRegistrationPayment(Integer paymentId, Integer settlementTypeId) throws IllegalArgumentException, IndexOutOfBoundsException;
    JSONObject payPayment(ArrayList<Integer> paymentIdList, Integer settlementTypeId, Integer tollKeeperId) throws RuntimeException;
    Invoice retreatPayment(Integer paymentId, Integer adminId) throws IllegalArgumentException, UnsupportedOperationException, IndexOutOfBoundsException;
    void produceRetreatDrugPayment(Integer paymentId, Integer adminId, Integer retreatQuantity) throws IllegalArgumentException, UnsupportedOperationException, IndexOutOfBoundsException;
    Invoice retreatDrugFee(Integer paymentId, Integer paymentAdminId);

    Payment findByRegistrationId(Integer registrationId);
    ArrayList<Payment> getWithItem(Integer id);
    List<Payment> getByRegistrationId(Integer id, Integer type);
    ArrayList<Payment> findAllByDoctor(ArrayList<Integer> doctorIdList, Date start, Date end);
    Integer getAllPayments(Integer doctorId, String start, String end,Integer id);
    public ArrayList<Payment> findAllByItemAndPaymentType(Integer itemId, Integer paymentTypeId);
    ArrayList<Payment> findAllByItemAndPaymentTypeAndState(Integer itemId, Integer paymentTypeId, Integer state) throws RuntimeException;
    Map<Integer,Integer> getForStatistics(Integer doctorId, Integer patientId, String start, String end);
    Payment findOneByInvoice(Integer invoiceId);
}
