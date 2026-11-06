<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1 class="title">注册新账号</h1>
        <p class="subtitle">AI陪伴与心理韧性成长平台</p>
      </div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        size="large"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model.trim="registerForm.username"
            placeholder="请输入用户名（2-20个字符）"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model.trim="registerForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>
        <el-form-item prop="code">
          <el-input
            v-model.trim="registerForm.code"
            placeholder="请输入6位验证码"
            prefix-icon="Key"
            maxlength="6"
          >
            <template #append>
              <el-button
                :loading="codeSending"
                :disabled="countdown > 0"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? countdown + 's后重发' : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20位，含字母和数字）"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span class="tip">已有账号？</span>
        <el-link type="primary" :underline="false" @click="goLogin">
          返回登录
        </el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { RegisterForm } from '@/types/auth'
import { registerApi, sendEmailCodeApi } from '@/api/auth'

const router = useRouter()

// 注册表单引用（用于表单校验、发送验证码前单独校验邮箱）
const registerFormRef = ref<FormInstance>()

// 注册请求加载态
const loading = ref(false)

// 验证码发送加载态
const codeSending = ref(false)

// 验证码重发倒计时（秒）
const countdown = ref(0)

// 倒计时定时器句柄
let countdownTimer: number | undefined = undefined

// 注册表单（含仅前端使用的确认密码字段）
const registerForm = reactive<RegisterForm>({
  username: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})

// 密码规则与后端 ParamValidator.isPassword 完全一致：6-20位且同时包含字母和数字
const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{6,20}$/

// 6位数字验证码规则（与后端 ParamValidator.isSixDigitCode 一致）
const SIX_DIGIT_PATTERN = /^\d{6}$/

// 确认密码一致性校验
const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单校验规则（与后端注册接口校验规则逐条对齐）
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度需在2-20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: SIX_DIGIT_PATTERN, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: PASSWORD_PATTERN,
      message: '密码长度需在6-20位之间，且需包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

/**
 * 提取错误信息：优先取后端业务错误 message，其次取 HTTP 错误响应体 message，最后兜底
 *
 * @param error 捕获到的错误对象
 * @returns 展示给用户的错误文案
 */
const getErrorMessage = (error: unknown): string => {
  const err = error as { response?: { data?: { message?: string } }; message?: string }
  return err?.response?.data?.message || err?.message || '请求失败，请稍后重试'
}

// 启动 60 秒验证码重发倒计时
const startCountdown = () => {
  countdown.value = 60
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      window.clearInterval(countdownTimer)
      countdownTimer = undefined
    }
  }, 1000)
}

// 发送邮箱验证码：先校验邮箱格式，校验通过后调用接口并启动倒计时
const handleSendCode = () => {
  registerFormRef.value?.validateField('email', (valid: boolean) => {
    if (!valid) {
      return
    }
    codeSending.value = true
    sendEmailCodeApi({ email: registerForm.email.trim() })
      .then((res) => {
        ElMessage.success(res.message || '验证码已发送，请在5分钟内完成验证')
        startCountdown()
      })
      .catch((error: unknown) => {
        // 发送失败弹窗提示
        ElMessageBox.alert(getErrorMessage(error), '发送失败', {
          type: 'error',
          confirmButtonText: '知道了'
        })
      })
      .finally(() => {
        codeSending.value = false
      })
  })
}

// 注册处理：表单校验通过后调用注册接口，成功跳转登录页，失败弹窗提示
const handleRegister = () => {
  registerFormRef.value?.validate((valid: boolean) => {
    if (!valid) {
      return
    }
    loading.value = true
    registerApi({
      username: registerForm.username.trim(),
      email: registerForm.email.trim(),
      code: registerForm.code.trim(),
      password: registerForm.password
    })
      .then((res) => {
        ElMessage.success(res.message || '注册成功，请登录')
        goLogin()
      })
      .catch((error: unknown) => {
        // 注册失败错误弹窗提示
        ElMessageBox.alert(getErrorMessage(error), '注册失败', {
          type: 'error',
          confirmButtonText: '知道了'
        })
      })
      .finally(() => {
        loading.value = false
      })
  })
}

// 跳转登录页
const goLogin = () => {
  router.push('/login')
}

// 组件卸载时清理倒计时定时器
onUnmounted(() => {
  if (countdownTimer !== undefined) {
    window.clearInterval(countdownTimer)
    countdownTimer = undefined
  }
})
</script>

<style lang="scss" scoped>
.register-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  .title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .subtitle {
    font-size: 14px;
    color: #909399;
  }
}

.register-form {
  .register-btn {
    width: 100%;
  }
}

.register-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 8px;

  .tip {
    font-size: 14px;
    color: #909399;
  }
}
</style>
