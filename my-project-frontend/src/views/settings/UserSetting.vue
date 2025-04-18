<script setup>

import Card from "@/components/Card.vue";
import {Message, Select, User, Refresh} from "@element-plus/icons-vue";
import {useStore} from "@/store";
import {computed, reactive, ref} from "vue";
import {get, post} from "@/net";
import {ElMessage} from "element-plus";
import axios from "axios";

const store = useStore()

const register_time = computed(() => new Date(store.user.register_time).toLocaleString())

const desc = ref('')
const baseFormRef = ref()
const emailFormRef = ref()

const baseForm = reactive({
  username: '',
  gender: 0,
  phone: '',
  qq: '',
  wx: '',
  desc: '',
})

const loading = reactive({
  form: true,
  base: false
})

const emailForm = reactive({
  email: '',
  code: '',
})

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'))
  } else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error('用户名不能包含特殊字符，只能是中文/英文'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    {validator: validateUsername, trigger: ['blur', 'change']},
    {min: 2, max: 8, message: '用户名的长度必须在2-8个字符之间', trigger: ['blur', 'change']},
  ],
  email: [
    {required: true, message: '请输入邮件地址', trigger: 'blur'},
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ]
}

function saveDetails() {
  baseFormRef.value.validate(isValid => {
    if (isValid) {
      loading.base = true
      post('/api/user/save-details', baseForm, () => {
        ElMessage.success('用户信息保存成功')
        store.user.username = baseForm.username
        desc.value = baseForm.desc
        loading.base = false
      }, (message) => {
        ElMessage.warning(message)
        loading.base = false
      })
    }
  })
}

get('/api/user/details', data => {
  baseForm.username = store.user.username
  baseForm.gender = data.gender
  baseForm.phone = data.phone
  baseForm.qq = data.qq
  baseForm.wx = data.wx
  baseForm.desc = desc.value = data.desc
  emailForm.email = store.user.email
  loading.form = false
})

const codeTime = ref(0)
const isEmailValid = ref(true)
const OvValidate = (prop, isValid) => {
  if (prop === 'email') isEmailValid.value = isValid
}

function sendEmailCode() {
  emailFormRef.value.validate(isValid => {
    if (isValid) {
      codeTime.value = 60
      get(`/api/auth/ask-code?email=${emailForm.email}&type=modify`, () => {
        ElMessage.success(`验证码已发送到邮箱: ${emailForm.email}，请注意查收`)
        const handle = setInterval(() => {
          codeTime.value--
          if (codeTime.value === 0) clearInterval(handle)
        }, 1000)
      }, (message) => {
        ElMessage.warning(message)
        codeTime.value = 0
      })
    }
  })
}

function modifyEmail() {
  emailFormRef.value.validate(isValid => {
    if (isValid) {
      post('api/user/modify-email', emailForm, () => {
        ElMessage.success('邮件修改成功')
        store.user.email = emailForm.email
        emailForm.code = null;
      })
    }
  })
}

</script>

<template>
  <div style="display: flex;max-width: 1000px;margin: auto">
    <div class="setting-left">
      <card :icon="User" title="账户信息设置" desc="在这里编辑您的个人信息，您可以在隐私设置中选择是否展示这些信息"
            v-loading="loading.form">
        <el-form :model="baseForm" :rules="rules" ref="baseFormRef" label-position="top"
                 style="margin: 0 10px 10px 10px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="baseForm.username" maxlength="10"/>
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="baseForm.gender">
              <el-radio :label="0">男</el-radio>
              <el-radio :label="1">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="baseForm.phone" maxlength="11"/>
          </el-form-item>
          <el-form-item label="QQ号" prop="qq">
            <el-input v-model="baseForm.qq" maxlength="13"/>
          </el-form-item>
          <el-form-item label="微信号" prop="wx">
            <el-input v-model="baseForm.wx" maxlength="20"/>
          </el-form-item>
          <el-form-item label="个人简介" prop="desc">
            <el-input v-model="baseForm.desc" type="textarea" :rows="6" maxlength="200"/>
          </el-form-item>
          <div>
            <el-button :icon="Select" @click="saveDetails" type="success" :loading="loading.base"
            >保存用户信息
            </el-button>
          </div>
        </el-form>
      </card>
      <card style="margin-top: 10px" :icon="Message" title="电子邮件设置" desc="您可以在这里修改默认绑定的电子邮件地址">
        <el-form :rules="rules" @validate="OvValidate" ref="emailFormRef" :model="emailForm" label-position="top"
                 style="margin: 0 10px 10px 10px">
          <el-form-item label="电子邮件" prop="email">
            <el-input v-model="emailForm.email"/>
          </el-form-item>
          <el-form-item prop="code">
            <el-row style="width: 100%" :gutter="10">
              <el-col :span="18">
                <el-input placeholder="请获取验证码" v-model="emailForm.code"/>
              </el-col>
              <el-col :span="6">
                <el-button type="success" style="width: 100%" :disabled="!isEmailValid || codeTime > 0"
                           @click="sendEmailCode" plain>{{ codeTime > 0 ? `${codeTime} 秒可重试` : '获取验证码' }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <el-button :icon="Refresh" @click="modifyEmail" type="success">更新电子邮件</el-button>
          </div>
        </el-form>
      </card>
    </div>
    <div class="setting-right">
      <div style="position:sticky;top: 20px">
        <card>
          <div style="text-align: center;padding: 5px 15px 0 15px">
            <el-avatar :size="70" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"/>
            <div style="font-weight: bold">你好,{{ store.user.username }}</div>
          </div>
          <el-divider style="margin: 10px 0"/>
          <div style="font-size: 14px;color: grey;padding: 10px">{{ desc || '这个用户很懒,没有填写个人简介~' }}</div>
        </card>
        <card style="margin-top: 10px;font-size: 14px">
          <div>账户注册时间：{{ register_time }}</div>
          <div style="color: grey">欢迎加入我们的学习论坛！</div>
        </card>
      </div>

    </div>
  </div>
</template>

<style scoped>

.setting-left {
  flex: 1;
  margin: 20px;
}

.setting-right {
  width: 300px;
  margin: 20px 30px 20px 0;
}

</style>