package cn.neuedu.his.service;
import cn.neuedu.his.model.JobSchedule;
import cn.neuedu.his.util.inter.Service;

/**
 *
 * Created by ccm on 2019/05/24.
 */
public interface JobScheduleService extends Service<JobSchedule> {
    void changeHaveRegistrationAmount(Integer id);
}
