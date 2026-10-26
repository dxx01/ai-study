<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import AuthShell from '@/components/auth/AuthShell.vue'
import { useApiFeedback } from '@/composables/useApiFeedback'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const { showError, showSuccess } = useApiFeedback()
const loading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  username: 'admin',
  password: 'admin123',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    showSuccess('登录成功')
    const redirect = (route.query.redirect as string) || '/workspace'
    await router.push(redirect)
  } catch (e) {
    showError(e, '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthShell title="登录 AI 应用平台" subtitle="AI 应用工程学习与实践 · 示例场景：健康科普顾问" variant="teal">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" autocomplete="username" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password autocomplete="current-password" />
      </el-form-item>
      <el-button type="primary" native-type="submit" :loading="loading" class="auth-form-full">登录</el-button>
    </el-form>
    <template #footer>
      还没有账号？
      <RouterLink to="/register">注册</RouterLink>
    </template>
  </AuthShell>
</template>
