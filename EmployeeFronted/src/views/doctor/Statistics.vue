<template>
    <div>

        <a-row type="flex" align="middle" justify="center" class="info-medicine">
            <a-col span="23">
                <a-card hoveracble title="工作量统计" :headStyle="{fontSize:'30px'}" :bodyStyle="{padding:'5px 0'}">
                </a-card>
            </a-col>
        </a-row>


        <a-row type="flex" align="middle" justify="center" style="text-align:center;margin-bottom:20px">

            <a-col span="6" style="text-align: center;margin-top:10px">
                <a-range-picker style="text-align:center;" v-model="time">
                    <template slot="dateRender" slot-scope="current">
                        <div class="ant-calendar-date" :style="getCurrentStyle(current)">
                            {{current.date()}}
                        </div>
                    </template>
                </a-range-picker>
            </a-col>

            <a-col span="4" style="text-align: center;margin-top:10px">
                <a-button type="primary" style="text-align:center;" @click="search">查询</a-button>
            </a-col>

        </a-row>


        <a-row type="flex" align="top" justify="center" style="text-align:center;margin-bottom:20px" v-if="showDoctor">


            <a-col style="width:78%;margin-left:10px;text-align:center">
                <a-card :body-style="{padding: 0}" style="text-align:center">
                    <a-form layout="inline" style="text-align:center;margin-top:10px">
                        <a-row align="middle" justify="center" style="width:100%">
                            <a-col span="1" style="width:50%">
                                <a-form-item>
                                    <a-icon type="user"/>
                                </a-form-item>

                                <a-form-item
                                        label="医生姓名"
                                >
                                    <a-button type="dashed" disabled style="color:black;width:100px;">
                                        {{this.doctor.realName}}
                                    </a-button>
                                </a-form-item>
                            </a-col>

                            <a-col span="1" style="width:50%">
                                <a-form-item
                                        label="ID"
                                >
                                    <a-button type="dashed" disabled style="color:black;width:100px;">
                                        {{this.doctor.id}}
                                    </a-button>
                                </a-form-item>
                            </a-col>
                        </a-row>


                    </a-form>

                    <a-divider style="margin-top:20px;font-size:20px;">信息统计</a-divider>

                    <a-table :columns="columns" :dataSource="result">

                    </a-table>
                </a-card>
            </a-col>
        </a-row>

        <a-row type="flex" align="top" justify="center" style="text-align:center;margin-bottom:20px" v-if="showList">

            <a-col style="width:20%">
                <a-card hoverable :body-style="{padding: '2px 0 0 0'}">
                <span slot="title" style="font-size: 22px">
                <span style="margin-left:35px">患者列表</span>
                <a-button @click="getPatient" type="primary" shape="circle" icon="reload"
                          style="float:right;margin-top:12px" size="small"></a-button>
                </span>

                    <a-collapse defaultActiveKey="2" :bordered="false">

                        <a-col span="3" style="text-align: center;margin-top:6px">
                            <a-input-search style="text-align: center;width:95%" placeholder="患者id"
                                            @search="onSearchByPid" enterButton></a-input-search>
                        </a-col>

                        <a-collapse-panel header="搜索结果" key="1">
                            <a-list itemLayout="horizontal" :dataSource="patients"
                                    style="overflow: auto;height: 400px;margin-top:5px">
                                <a-list-item slot="renderItem" slot-scope="item">
                                    <a-list-item-meta>
                                <span slot="title"
                                      style="font-size: 15px;line-height: 20px">{{item.realName}}</span>
                                        <span slot="description" style="text-align:center">
                                        <span style="width:33%;margin-left:10px">ID: {{item.id}}   </span>
                                        <span style="width:33%;margin-right:10px">年龄: {{item.age}}岁  </span>
                                        <span style="width:33%;margin-right:10px">性别: {{item.sex?'男':'女'}}</span>
                                    </span>
                                    </a-list-item-meta>
                                </a-list-item>
                            </a-list>
                        </a-collapse-panel>

                        <a-collapse-panel header="全部患者信息" key="2">
                            <a-list itemLayout="horizontal" :dataSource="originalP"
                                    style="overflow: auto;height: 400px;margin-top:5px">
                                <a-list-item slot="renderItem" slot-scope="item" @click="selectPatient(item)">
                                    <a-list-item-meta>
                                <span slot="title"
                                      style="font-size: 15px;line-height: 20px">{{item.realName}}</span>
                                        <span slot="description" style="text-align:center">
                                        <span style="width:33%;margin-left:10px">ID: {{item.id}}   </span>
                                        <span style="width:33%;margin-right:10px">年龄: {{item.age}}岁  </span>
                                        <span style="width:33%;margin-right:10px">性别: {{item.sex?'男':'女'}}</span>
                                    </span>
                                    </a-list-item-meta>
                                </a-list-item>
                            </a-list>
                        </a-collapse-panel>

                    </a-collapse>
                </a-card>
            </a-col>

            <a-col style="width:78%;margin-left:10px">
                <a-card :body-style="{padding: 0}">
                <span slot="title" style="font-size: 22px">
                    
                <a-form :form="form" style="text-align: center" layout="inline">
                    
                    <a-form-item>
                        <a-icon type="user"/>
                    </a-form-item>


                    <a-form-item
                            label="姓名"
                            :label-col="formItemLayout.labelCol"
                            :wrapper-col="formItemLayout.wrapperCol"
                    >
                        <a-button type="dashed" disabled style="color:black;width:100px;margin-right:20px">{{this.patient.realName}}</a-button> 
                    </a-form-item>


                    <a-form-item
                            label="性别"
                            :label-col="formItemLayout.labelCol"
                            :wrapper-col="formItemLayout.wrapperCol"
                    >
                        <a-button type="dashed" disabled style="color:black;width:100px;margin-right:20px">{{this.patient.sex?'男':'女'}}</a-button> 
                    </a-form-item>

                        <a-form-item
                                label="年龄"
                                :label-col="formItemLayout.labelCol"
                                :wrapper-col="formItemLayout.wrapperCol"
                        >
                        <a-button type="dashed" disabled style="color:black;width:100px;margin-right:20px">{{this.patient.age}}</a-button> 
                    
                    </a-form-item>

                        <a-form-item
                                label="ID"
                                :label-col="formItemLayout.labelCol"
                                :wrapper-col="formItemLayout.wrapperCol"
                        >
                        
                        <a-button type="dashed" disabled style="color:black;width:100px;margin-right:20px">{{this.patient.id}}</a-button> 
                    </a-form-item>
    
                </a-form>
                
                <a-divider style="margin-top:20px;font-size:20px;">费用统计</a-divider>
                
                <a-table :columns="rColumns" :dataSource="rData">
                    <template slot="createTime" slot-scope="text">
                        <span name>{{text| timeStampToDate}}</span>
                    </template>
                </a-table>       

                </span>

                </a-card>
            </a-col>
        </a-row>
    </div>
</template>

<script>

    export default {
        data () {
            return {
                form: this.$form.createForm(this),
                time: null,
                showList: false,
                showPaitent: false,
                showDoctor: true,
                patient: {id: null, realName: '无', age: null, sex: null},
                doctor: {},
                patients: [],
                originalP: [],
                paymentData: [],
                paymentColumns: [],
                selectedRowKeys: [],
                paymentTypeList: [],
                paymentTypeMap: [],
                result: [],
                name: [],
                value: [],
                columns: [{
                    title: '费用类型',
                    key: 'name',
                    dataIndex: 'name',
                    width: '50%',
                    align: 'center',
                    scopedSlots: {customRender: 'name'}
                }, {
                    title: '总金额',
                    key: 'value',
                    dataIndex: 'value',
                    width: '50%',
                    align: 'center',
                    scopedSlots: {customRender: 'value'}
                }],
                doctorStatistics: [],
                rColumns: [{
                    title: '挂号ID',
                    key: 'id',
                    dataIndex: 'id',
                    width: '16%',
                    align: 'center',
                    scopedSlots: {customRender: 'id'}
                }, {
                    title: '药费',
                    key: 'medicalFee',
                    dataIndex: 'medicalFee',
                    width: '16%',
                    align: 'center',
                    scopedSlots: {customRender: 'medicalFee'}
                }, {
                    title: '检查项目费',
                    key: 'inspectionFee',
                    dataIndex: 'inspectionFee',
                    width: '16%',
                    align: 'center',
                    scopedSlots: {customRender: 'inspectionFee'}
                },{
                    title: '挂号费',
                    key: 'registrationFee',
                    dataIndex: 'registrationFee',
                    width: '16%',
                    align: 'center',
                    scopedSlots: {customRender: 'registrationFee'}
                }, {
                    title: '挂号时间',
                    key: 'createTime',
                    dataIndex: 'createTime',
                    width: '20%',
                    align: 'center',
                    scopedSlots: {customRender: 'createTime'}
                },],
                rData: [],
            }
        }, computed: {
            formItemLayout () {
                const {formLayout} = this;
                return formLayout === 'horizontal' ? {
                    labelCol: {span: 4},
                    wrapperCol: {span: 14},
                } : {};
            },
        }, created () {
            this.getPaymentType();
        }, methods: {
            getCurrentStyle (current) {
                const style = {}
                if (current.date() === 1) {
                    style.border = '1px solid #1890ff'
                    style.borderRadius = '50%'
                }
                return style
            }, search () {
                var start = null
                var end = null
                if (this.time != null || this.time.length == 2) {
                    start = this.time[0].utc().format('YYYY-MM-DD')
                    end = this.time[1].utc().format('YYYY-MM-DD')
                } else {
                    start = null
                    end = null
                }
                let that = this
                var m = {start: start, end: end}
                this.name = []
                this.value = []
                this.$api.post("/doctor/getDoctorStatistics", m,
                    res => {
                        if (res.code === "100") {
                            that.result = res.data.feeMap
                            delete res.data.feeMap
                            that.doctor = res.data
                            // this.$chart.bar1('chart1','工作量统计','',that.name,that.value)
                            that.showDoctor = true
                            that.getRStatistics()
                        } else {
                            that.$message.error(res.msg)
                        }
                    }, () => {
                    })

            }, getRStatistics () {
                var start = null
                var end = null
                if (this.time != null || this.time.length == 2) {
                    start = this.time[0].utc().format('YYYY-MM-DD')
                    end = this.time[1].utc().format('YYYY-MM-DD')
                } else {
                    start = null
                    end = null
                }
                let that = this
                var m = {start: start, end: end}
                this.$api.post("/doctor/getRStatistics", m,
                    res => {
                        if (res.code === "100") {
                            that.originalP = res.data
                            that.showList = true
                        } else {
                            that.$message.error(res.msg)
                        }
                    }, () => {
                    })
            }, onSearchByPid (value) {
                this.patients = []
                this.rData = []
                for (var i = 0; i < this.originalP.length; i++) {
                    if (this.originalP[i].id == value) {
                        var element = this.originalP[i]
                        if (element != null) {
                            this.patient = element
                            this.patients.push(element)
                            this.patients[0].key = element.id
                            element.registrations.forEach(element => {
                                this.rData.push({
                                    id: element.id,
                                    medicalFee: element.medicalFee,
                                    inspectionFee: element.inspectionFee,
                                    createTime: element.createTime,
                                    registrationFee:element.registrationFee,
                                    key: element.id
                                })
                            });
                        }
                        break;
                    } else {
                        this.patient = {id: null, realName: null, age: null, sex: null}
                    }
                }

            }, getPatient () {

            }, selectPatient (item) {
                this.onSearchByPid(item.id)
            }, getPaymentType () {
                let that = this
                this.$api.get("/payment_type/getAll", null,
                    res => {
                        if (res.code === "100") {
                            var name = res.data
                            for (let i = 0; i < name.length; i++) {
                                if (!name[i].delete) {
                                    that.paymentTypeList.push({
                                        name: name[i].name,
                                        id: name[i].id
                                    })
                                    that.paymentTypeMap.push({
                                        id: name[i].id,
                                        name: name[i].name
                                    })
                                }
                            }
                        }
                    }, () => {
                    })
            }
        }
    }


</script>

<style scoped>
    .info-medicine {
        margin-top: 40px;
        margin-bottom: 20px;
    }

    #chart1 {
        width: 400px;
        height: 400px;
    }
</style>