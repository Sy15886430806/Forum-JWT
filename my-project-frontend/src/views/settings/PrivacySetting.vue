<script setup>

import Card from "@/components/Card.vue";
import {Edit, Lock, MoreFilled, Refresh, Setting, Switch} from "@element-plus/icons-vue";
import {reactive, ref} from "vue";
import {post, get} from "@/net/index.js";
import {ElMessage} from "element-plus";

const form = reactive({
  password: "",
  new_password: "",
  new_password_repeat: "",
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.new_password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  password: [
    {required: true, message: '请输入原来的密码', trigger: 'blur'},
  ],
  new_password: [
    {required: true, message: '请输入新的密码', trigger: 'blur'},
    {min: 6, max: 20, message: '密码的长度必须在6-20个字符之间', trigger: ['blur', 'change']}
  ],
  new_password_repeat: [
    {required: true, message: '请再次输入新的密码', trigger: 'blur'},
    {validator: validatePassword, trigger: ['blur', 'change']},
  ]
}

const formRef = ref()
const valid = ref(true)
const onValidate = (prop, isValid) => valid.value = isValid

function resetPassword() {
  formRef.value.validate(valid => {
    if (valid) {
      post('/api/user/change-password', form, () => {
        ElMessage.success('修改密码成功')
        formRef.value.resetFields();
      })
    }
  })
}

const saving = ref(true)

const privacy = reactive({
  phone: false,
  email: false,
  qq: false,
  wx: false,
  gender: false,
})

get('/api/user/privacy', data => {
  privacy.phone = data.phone
  privacy.email = data.email
  privacy.wx = data.wx
  privacy.qq = data.qq
  privacy.gender = data.gender
  saving.value = false
})

function saveProivacy(type, status) {
  saving.value = true
  post('/api/user/save-privacy', {
    type: type,
    status: status
  }, () => {
    ElMessage.success('隐私设置修改成功')
    saving.value = false
  })
}

</script>

<template>
  <div style="max-width: 600px;margin: auto">
    <div style="margin-top: 20px">
      <card :icon="Setting" title="隐私设置" desc="在这里设置哪些内容可以背其他人看见,请各位小伙伴注重自己的隐私"
            v-loading="saving">
        <div class="checkbox-list">
          <el-checkbox @change="saveProivacy('phone', privacy.phone)" v-model="privacy.phone">
            公开展示我的手机号
          </el-checkbox>
          <el-checkbox @change="saveProivacy('email', privacy.email)" v-model="privacy.email">
            公开展示我的电子邮件地址
          </el-checkbox>
          <el-checkbox @change="saveProivacy('qq', privacy.qq)" v-model="privacy.qq">
            公开展示我的QQ号
          </el-checkbox>
          <el-checkbox @change="saveProivacy('wx', privacy.wx)" v-model="privacy.wx">
            公开展示我的微信号
          </el-checkbox>
          <el-checkbox @change="saveProivacy('gender', privacy.gender)" v-model="privacy.gender">
            公开展示我的性别
          </el-checkbox>
        </div>
      </card>
      <card style="margin: 20px 0" :icon="Edit" title="修改密码" desc="修改密码请在这里操作,请务必记住您的新密码">
        <el-form :rules="rules" :model="form" ref="formRef" @validate="onValidate" label-width="100px"
                 style="margin: 20px">
          <el-form-item label="当前密码" prop="password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.password" placeholder="当前密码"
                      maxlength="20"/>
          </el-form-item>
          <el-form-item label="新密码" prop="new_password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password" placeholder="新密码"
                      maxlength="20"/>
          </el-form-item>
          <el-form-item label="重复新密码" prop="new_password_repeat">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password_repeat"
                      placeholder="重新输入新密码" maxlength="20"/>
          </el-form-item>
        </el-form>
        <div style="text-align: center">
          <el-button :icon="Refresh" @click="resetPassword" type="success">立即重置密码</el-button>
        </div>
      </card>
    </div>
  </div>
</template>

<style scoped>
.checkbox-list {
  margin: 10px 0 0 10px;
  display: flex;
  flex-direction: column;
}
</style>