<template>
    <div>
        <a-row type="flex" align="middle" justify="center" class="info-medicine">
            <a-col span="23">
                <a-card hoveracble title="费用类别管理" :headStyle="{fontSize:'30px'}" :bodyStyle="{padding:'5px 0'}">

                    <a-row type="flex" align="top" justify="space-between"
                           style="margin:5px 0 10px 0;text-align: center;width:85%">
                        <a-col span="6" style="margin-left:20px;">
                            <a-input-search placeholder="请输入费用编码" @search="onSearchByCode" enterButton></a-input-search>
                        </a-col>
                        <a-col span="6">
                            <a-input-search placeholder="请输入费用名称" @search="onSearchByName" enterButton></a-input-search>
                        </a-col>
                        <a-col span="3">
                            <a-button @click="add" style="color:#1890FF">
                                <a-icon type="edit"/>
                                新增类型
                            </a-button>
                        </a-col>

                        <a-col span="3">
                            <a-button @click="deleteAll" type="danger">
                                <a-icon type="plus-circle"/>
                                全部删除
                            </a-button>
                        </a-col>

                        <a-col span="3">
                            <a-button @click="importI" type="primary">
                                <a-icon type="plus-circle"/>
                                导入数据
                            </a-button>
                        </a-col>
                        <a-col :span="2">
                            <a-button @click="getPaymentList" type="primary" shape="circle" icon="reload"
                                      style=";margin-top:5px" size="small"></a-button>
                        </a-col>
                    </a-row>


                    <a-table :columns="columns" style="width:85%;text-align: center" :dataSource="paymentTypeList"
                             bordered :rowSelection="{slectedRowKeys:selectedRowKeys, onChange:onSelectChange}">

                        <template slot="type" slot-scope="text,record">
                            <a-select
                                    v-if="record.editable"
                                    :defaultValue="text"
                                    style="width:100%"
                                    @change="e => typeChange(e)"
                            >
                                <a-select-option v-for="d in typeList" :key="d.id">{{d.name}}</a-select-option>
                            </a-select>
                            <template v-else>{{text}}</template>
                        </template>

                        <template slot="isDelete" slot-scope="text,record">
                            <a-select
                                    v-if="record.editable"
                                    :defaultValue="text?'已删':'未删'"
                                    style="width:100%"
                                    @change="e => deleteChange(e)"
                            >
                                <a-select-option key="0">否</a-select-option>
                                <a-select-option key="1">是</a-select-option>
                            </a-select>
                            <template v-else>
                                <a-tag color="red" style="font-size:13px" v-if="text">已删</a-tag>
                                <a-tag color="blue" style="font-size:13px" v-else>未删</a-tag>
                            </template>
                        </template>

                        <template v-for="col in ['name', 'code']" :slot="col" slot-scope="text, record">
                            <div :key="col">
                                <a-input
                                        v-if="record.editable"
                                        style="margin: -5px 0"
                                        :defaultValue="text"
                                        @change="e => handleChange(e.target.value, record.key, col)"
                                />
                                <template v-else>{{text}}</template>
                            </div>
                        </template>


                        <template slot="action" slot-scope="text, record">
                            <div class='editable-row-operations'>
                                <span v-if="record.editable">
                                <a @click="() => saveRow(record)">保存</a>
                                <a-divider type="vertical"/>
                                <a-popconfirm title='确定取消吗?' @confirm="() => cancel(record)">
                                <a style="color:red" @click="() => cancel(record)">取消</a>
                                </a-popconfirm>
                                </span>
                                <span v-else>
                                <a @click="() => edit(record)">编辑</a>
                                <a-divider type="vertical"/>
                                <a @click="() => deleteRow(record.id)" style="color:red">删除</a>
                                </span>
                            </div>
                        </template>
                    </a-table>

                </a-card>
            </a-col>
        </a-row>
    </div>
</template>


<script>
    export default {
        data () {
            return {
                originalList: [],
                selectedRowKeys: [],
                form: this.$form.createForm(this),
                paymentTypeTemp: [{id: -1, code: '0', name: 'default', type: 'default', isDelete: false}],
                showList: true,
                columns: [{
                    title: '编码',
                    dataIndex: 'code',
                    key: 'code',
                    sorter: true,
                    width: '20%',
                    scopedSlots: {
                        customRender: 'code'
                    }
                }, {
                    title: '名称',
                    dataIndex: 'name',
                    key: 'name',
                    sorter: true,
                    width: '20%',
                    scopedSlots: {customRender: 'name'}
                }, {
                    title: '大类',
                    dataIndex: 'type',
                    key: 'type',
                    sorter: true,
                    width: '20%',
                    scopedSlots: {customRender: 'type'}
                }, {
                    title: '是否删除',
                    dataIndex: 'isDelete',
                    key: 'isDelete',
                    sorter: true,
                    width: '20%',
                    scopedSlots: {customRender: 'isDelete'}
                }, {
                    title: '操作',
                    key: 'action',
                    dataIndex: 'action',
                    width: '20%',
                    align: 'middle',
                    scopedSlots: {customRender: 'action'}
                }],
                paymentTypeList: [],
                typeList: [],
                idKeyMap: {},
                nameKeyMap: {},
            }
        }, created () {
            this.getPaymentList()
        }, methods: {
            getPaymentList () {
                this.paymentTypeList = []
                this.typeList = []
                this.nameKeyMap = new Map()
                this.idKeyMap = new Map()
                this.selectedRowKeys = []
                this.paymentTypeTemp = [{id: -1, code: '0', name: 'default', type: 'default', isDelete: false}]
                let that = this
                this.$api.get("/payment_type/getAll", null,
                    res => {
                        if (res.code === "100") {
                            var list = res.data
                            var t = list.filter(item => 0 === item.type)
                            var map = new Map()
                            var m = new Map()
                            for (var i = 0; i < t.length; i++) {
                                that.typeList.push({
                                    key: t[i].id,
                                    id: t[i].id,
                                    code: t[i].code,
                                    name: t[i].name,
                                    isDelete: t[i].delete,
                                    type: '大类'
                                })
                                map.set(t[i].id, t[i].name)
                                m.set(t[i].name, t[i].id)
                                that.paymentTypeList.push(that.typeList[i])
                            }
                            that.idKeyMap = map
                            that.idKeyMap.set(0, '大类')
                            that.nameKeyMap = m
                            that.nameKeyMap.set('大类', 0)
                            that.typeList.push({id: 0, name: '大类'})
                            var p = res.data.filter(item => 0 !== item.type)
                            for (let i = 0; i < p.length; i++) {
                                if (p[i].type !== 0) {
                                    that.paymentTypeList.push({
                                        key: p[i].id,
                                        id: p[i].id,
                                        code: p[i].code,
                                        name: p[i].name,
                                        isDelete: p[i].delete,
                                        type: map.get(p[i].type)
                                    })
                                }
                            }
                            that.originalList = that.paymentTypeList
                        } else {
                            that.$message.error(res.msg)
                        }
                    }, () => {
                    })
            }, onSearchByCode (value) {
                if (value) {
                    this.paymentTypeList = this.paymentTypeList.filter(item => (item.code.indexOf(value) >= 0))
                } else {
                    this.paymentTypeList = this.originalList
                }
            }, onSearchByName (value) {
                if (value) {
                    this.paymentTypeList = this.paymentTypeList.filter(item => (item.name.indexOf(value) >= 0))
                } else {
                    this.paymentTypeList = this.originalList
                }
            }, add () {
                var a = {id: null, code: '无', name: '无', type: '大类', isDelete: false, editable: true, isAdd: true}
                this.paymentTypeTemp = a
                this.paymentTypeList.unshift(a)
            }, deleteAll () {
                for (var i = 0; i < this.selectedRowKeys.length; i++) {
                    this.deleteRow(this.selectedRowKeys[i])
                }
            }, importI () {
            }, typeChange (e) {
                this.paymentTypeTemp.type = e
            }, deleteChange (e) {
                if (e === "0")
                    this.paymentTypeTemp.isDelete = 0
                if (e === "1")
                    this.paymentTypeTemp.isDelete = 1
            }, handleChange (value, record, name) {
                var newData = [...this.paymentTypeList]
                if (name === "name") {
                    var temp = newData.filter(item => value === item.name)
                    if (temp.length > 1) {
                        this.$message.error('名称重复')
                        return
                    }
                    this.paymentTypeTemp.name = value
                } else {
                    let temp = newData.filter(item => value === item.code)
                    if (temp.length > 1) {
                        this.$message.error('编码重复')
                        return
                    }
                    this.paymentTypeTemp.code = value
                }
            }, saveRow (record) {
                let that = this
                if (record.isAdd) {
                    record = this.paymentTypeTemp
                    let t = this.nameKeyMap.get(record.type)
                    if (t != null && t >= 0 && t < 100) {
                        record.type = t
                    }
                    record.delete = record.isDelete
                    this.$api.post("/payment_type/insertPaymentType", record,
                        res => {
                            if (res.code === "100") {
                                const newData = [...this.paymentTypeList]
                                const tar = newData.filter(item => item.id === record.id)[0]
                                tar.id = record.id
                                tar.code = record.code
                                tar.name = record.name
                                tar.delete = p.delete
                                tar.type = that.idKeyMap.get(record.type)
                                delete tar.editable
                                tar.isDelete = record.delete
                                that.paymentTypeList = newData
                                that.$message.success('更新成功')
                            } else {
                                that.$message.error('更新失败: ' + res.msg)
                            }
                        }, () => {
                        })
                    return
                }
                let value = record.id
                if (value != this.paymentTypeTemp.id)
                    return
                let p = this.paymentTypeTemp

                let t = this.nameKeyMap.get(p.type)
                if (t != null && t >= 0 && t < 100) {
                    p.type = t
                }
                p.delete = p.isDelete
                if (p.delete) {
                    this.$api.post("/payment_type/updatePaymentType", p,
                        res => {
                            if (res.code === "100") {
                                const newData = [...that.paymentTypeList]
                                const tar = newData.filter(item => item.id === p.id)[0]
                                tar.id = p.id
                                tar.code = p.code
                                tar.name = p.name
                                tar.type = that.idKeyMap.get(p.type)
                                delete tar.editable
                                tar.isDelete = true
                                tar.delete = true
                                that.paymentTypeList = newData
                                that.$message.success('更新成功')
                            } else {
                                that.$message.error(res.msg)
                            }
                        }, () => {
                        })
                } else {
                    this.retreatDelete(p)
                }
            }, cancel (record) {
                if (record.isAdd) {
                    this.paymentTypeList.shift()
                    return
                }
                var id = record.id
                const newData = [...this.paymentTypeList]
                const target = newData.filter(item => id === item.id)[0]
                if (target) {
                    delete target.editable
                    this.paymentTypeList = newData
                }
            }, edit (value) {
                const newData = [...this.paymentTypeList]
                const target = newData.filter(item => value.id === item.id)[0]
                if (target) {
                    target.editable = true
                    this.modalVisible = true
                    this.paymentTypeTemp = value
                    this.paymentTypeList = newData
                }
            }, deleteRow (value) {
                let that = this
                this.$api.post("/payment_type/deletePaymentType/" + value, null,
                    res => {
                        if (res.code === "100") {
                            that.$message.success('更新成功')
                            const target = that.paymentTypeList.filter(item => value === item.id)[0]
                            target.isDelete = true
                        } else {
                            that.$message.error('更新失败: ' + res.msg)
                        }
                    }, () => {
                    })

            }, ok () {
            }, cancle () {
                this.modalVisible = false
            }, onSelectChange (rowKeys) {
                this.selectedRowKeys = rowKeys
            }, retreatDelete (p) {
                let that = this
                this.$api.post("/payment_type/recoverPaymentType", p,
                    res => {
                        if (res.code === "100") {
                            const newData = [...that.paymentTypeList]
                            const tar = newData.filter(item => item.id === p.id)[0]
                            tar.id = p.id
                            tar.code = p.code
                            tar.name = p.name
                            tar.type = that.idKeyMap.get(p.type)
                            delete tar.editable
                            tar.isDelete = false
                            tar.delete = false
                            that.paymentTypeList = newData
                            that.$message.success('更新成功')
                        } else {
                            that.$message.error(res.msg)
                        }
                    }, () => {
                    })
            }
        }
    }
</script>

<style scoped>
</style>