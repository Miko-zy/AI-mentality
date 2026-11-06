<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="title">AI陪伴与心理韧性成长平台</h1>
        <p class="subtitle">温暖陪伴 · 韧性成长</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="email">
          <el-input
            v-model.trim="loginForm.email"
            placeholder="请输入邮箱账号"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span class="tip">还没有账号？</span>
        <el-link type="primary" :underline="false" @click="goRegister">
          立即注册
        </el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { UserInfo } from '@/types'
import { LoginForm, LoginUserVO } from '@/types/auth'
import { loginApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 登录表单引用（用于表单校验）
const loginFormRef = ref<FormInstance>()

// 登录请求加载态
const loading = ref(false)

// 登录表单（账号即邮箱，与后端 LoginDTO 对齐）
const loginForm = reactive<LoginForm>({
  email: '',
  password: ''
})

// 表单校验规则（邮箱必填 + 格式校验、密码必填）
const loginRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱账号', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

/**
 * 将后端 UserVO 映射为全局 UserInfo（store 现有契约）
 *
 * @param user 后端登录返回的用户信息
 * @returns store 使用的用户信息
 */
const mapToUserInfo = (user: LoginUserVO): UserInfo => ({
  id: user.userId,
  username: user.nickname || user.email,
  nickname: user.nickname,
  avatar: user.avatarUrl,
  role: user.role || 'user'
})

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

// 登录处理：表单校验通过后调用登录接口，成功存 token 跳首页，失败弹窗提示
const handleLogin = () => {
  loginFormRef.value?.validate((valid: boolean) => {
    if (!valid) {
      return
    }
    loading.value = true
    loginApi({
      email: loginForm.email.trim(),
      password: loginForm.password
    })
      .then((res) => {
        // 响应异常保护：登录成功必须携带 token 与用户信息
        if (!res.data || !res.data.token || !res.data.user) {
          ElMessageBox.alert('登录响应异常，请稍后重试', '登录失败', {
            type: 'error',
            confirmButtonText: '知道了'
          })
          return
        }
        // token 存入 pinia（store 内部同步持久化到 localStorage）
        userStore.setToken(res.data.token)
        // 用户信息一并存入，供后续页面与请求拦截器使用
        userStore.setUserInfo(mapToUserInfo(res.data.user))
        ElMessage.success('登录成功')
        // 存在重定向参数则回跳原页面，否则跳转首页
        const redirect = route.query.redirect as string
        router.push(redirect || '/')
      })
      .catch((error: unknown) => {
        // 登录失败错误弹窗提示
        ElMessageBox.alert(getErrorMessage(error), '登录失败', {
          type: 'error',
          confirmButtonText: '知道了'
        })
      })
      .finally(() => {
        loading.value = false
      })
  })
}

// 跳转注册页
const goRegister = () => {
  router.push('/register')
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
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

.login-form {
  .login-btn {
    width: 100%;
  }
}

.login-footer {
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
