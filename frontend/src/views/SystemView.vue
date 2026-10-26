<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import {
  createMenu,
  deleteMenu,
  listAdminMenus,
  listAdminPermissions,
  listAdminRoles,
  listAdminUsers,
  updateMenu,
  updateRolePermissions,
  updateUserRoles,
  updateUserStatus,
} from '@/api/admin'
import AppCheckboxPanel from '@/components/app/AppCheckboxPanel.vue'
import AppDataTable from '@/components/app/AppDataTable.vue'
import AppFormDialog from '@/components/app/AppFormDialog.vue'
import AppPageCard from '@/components/app/AppPageCard.vue'
import AppStatusTag from '@/components/app/AppStatusTag.vue'
import { useApiFeedback } from '@/composables/useApiFeedback'
import { useRoutePageTitle } from '@/composables/useRoutePageTitle'
import type {
  AdminPermissionItem,
  AdminRoleItem,
  AdminUserItem,
  MenuItem,
  MenuPayload,
} from '@/types/api'
import { ENABLE_STATUS_OPTIONS, EnableStatus, isEnableStatusEnabled } from '@/constants/dicts'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const pageTitle = useRoutePageTitle()
const { showError, showSuccess } = useApiFeedback()
const loading = ref(false)
const dialogSaving = ref(false)
const users = ref<AdminUserItem[]>([])
const roles = ref<AdminRoleItem[]>([])
const menus = ref<MenuItem[]>([])
const permissions = ref<AdminPermissionItem[]>([])

const section = computed(() => {
  if (route.name === 'platform-roles') return 'roles'
  if (route.name === 'platform-menus') return 'menus'
  return 'users'
})

const parentOptions = computed(() => [{ id: 0, name: '根菜单', children: menus.value }])

const roleCheckboxOptions = computed(() =>
  roles.value.map((role) => ({
    value: role.code,
    title: `${role.code} · ${role.name}`,
  })),
)

const permissionCheckboxOptions = computed(() =>
  permissions.value.map((item) => ({
    value: item.code,
    title: `${item.code} · ${item.name}`,
    tag: item.groupName ?? undefined,
  })),
)

async function loadAll() {
  loading.value = true
  try {
    const [userData, roleData, menuData, permissionData] = await Promise.all([
      listAdminUsers(),
      listAdminRoles(),
      listAdminMenus(),
      listAdminPermissions(),
    ])
    users.value = userData
    roles.value = roleData
    menus.value = menuData
    permissions.value = permissionData
  } catch (error) {
    showError(error, '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)

async function toggleStatus(row: AdminUserItem) {
  const next = isEnableStatusEnabled(row.status) ? EnableStatus.Disabled : EnableStatus.Enabled
  try {
    await updateUserStatus(row.id, next)
    row.status = next
    showSuccess(next === EnableStatus.Enabled ? '已启用' : '已禁用')
  } catch (error) {
    showError(error, '操作失败')
  }
}

const roleDialog = ref(false)
const editingUser = ref<AdminUserItem | null>(null)
const selectedRoles = ref<string[]>([])

function openRoleDialog(row: AdminUserItem) {
  editingUser.value = row
  selectedRoles.value = [...row.roles]
  roleDialog.value = true
}

async function saveRoles() {
  if (!editingUser.value || dialogSaving.value) return
  dialogSaving.value = true
  try {
    await updateUserRoles(editingUser.value.id, selectedRoles.value)
    roleDialog.value = false
    showSuccess('角色已更新')
    await loadAll()
  } catch (error) {
    showError(error, '保存失败')
  } finally {
    dialogSaving.value = false
  }
}

const permissionDialog = ref(false)
const editingRole = ref<AdminRoleItem | null>(null)
const selectedPermissions = ref<string[]>([])

function openPermissionDialog(row: AdminRoleItem) {
  editingRole.value = row
  selectedPermissions.value = [...row.permissions]
  permissionDialog.value = true
}

async function savePermissions() {
  if (!editingRole.value || dialogSaving.value) return
  dialogSaving.value = true
  try {
    await updateRolePermissions(editingRole.value.id, selectedPermissions.value)
    permissionDialog.value = false
    showSuccess('权限已更新')
    await loadAll()
  } catch (error) {
    showError(error, '保存失败')
  } finally {
    dialogSaving.value = false
  }
}

const menuDialog = ref(false)
const menuFormRef = ref<FormInstance>()
const editingMenuId = ref<number | null>(null)
const menuForm = reactive<MenuPayload>({
  parentId: 0,
  name: '',
  path: '',
  icon: '',
  permissionCode: '',
  sortOrder: 0,
  visible: true,
  status: EnableStatus.Enabled,
})

const menuDialogTitle = computed(() => (editingMenuId.value ? '编辑菜单' : '新增菜单'))

const menuRules: FormRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [
    { required: true, message: '请输入路由', trigger: 'blur' },
    { pattern: /^\//, message: '路由需以 / 开头', trigger: 'blur' },
  ],
}

function resetMenuForm() {
  editingMenuId.value = null
  Object.assign(menuForm, {
    parentId: 0,
    name: '',
    path: '',
    icon: '',
    permissionCode: '',
    sortOrder: 0,
    visible: true,
    status: EnableStatus.Enabled,
  })
}

async function openCreateMenu() {
  resetMenuForm()
  menuDialog.value = true
  await nextTick()
  menuFormRef.value?.clearValidate()
}

async function openEditMenu(row: MenuItem) {
  editingMenuId.value = row.id
  Object.assign(menuForm, {
    parentId: row.parentId,
    name: row.name,
    path: row.path,
    icon: row.icon ?? '',
    permissionCode: row.permissionCode ?? '',
    sortOrder: row.sortOrder,
    visible: row.visible ?? true,
    status: row.status ?? EnableStatus.Enabled,
  })
  menuDialog.value = true
  await nextTick()
  menuFormRef.value?.clearValidate()
}

async function saveMenu() {
  if (dialogSaving.value) {
    return
  }
  const valid = await menuFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  dialogSaving.value = true
  try {
    if (editingMenuId.value) {
      await updateMenu(editingMenuId.value, { ...menuForm })
    } else {
      await createMenu({ ...menuForm })
    }
    menuDialog.value = false
    showSuccess('菜单已保存')
    await loadAll()
  } catch (error) {
    showError(error, '保存失败')
  } finally {
    dialogSaving.value = false
  }
}

async function removeMenu(row: MenuItem) {
  try {
    await ElMessageBox.confirm(`确定删除菜单「${row.name}」吗？`, '删除确认', { type: 'warning' })
    await deleteMenu(row.id)
    showSuccess('已删除')
    await loadAll()
  } catch (error) {
    if (error !== 'cancel') {
      showError(error, '删除失败')
    }
  }
}
</script>

<template>
  <AppPageCard :title="pageTitle" :loading="loading">
    <template v-if="section === 'menus'" #actions>
      <el-button type="primary" @click="openCreateMenu">新增菜单</el-button>
    </template>

    <div class="app-page-list-body">
    <AppDataTable v-if="section === 'users'" :data="users">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <AppStatusTag :status="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="150">
        <template #default="{ row }">
          <el-tag v-for="role in row.roles" :key="role" size="small" class="app-tag-gap">{{ role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最近登录" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.lastLoginAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openRoleDialog(row)">改角色</el-button>
          <el-button
            link
            :type="isEnableStatusEnabled(row.status) ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ isEnableStatusEnabled(row.status) ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </AppDataTable>

    <AppDataTable v-else-if="section === 'roles'" :data="roles">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="code" label="角色编码" width="120" />
      <el-table-column prop="name" label="角色名" min-width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <AppStatusTag :status="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="权限" min-width="320">
        <template #default="{ row }">
          <el-tag v-for="code in row.permissions" :key="code" size="small" class="app-tag-gap">{{ code }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openPermissionDialog(row)">分配权限</el-button>
        </template>
      </el-table-column>
    </AppDataTable>

    <AppDataTable
      v-else
      tree
      :fill="false"
      :pagination="false"
      :data="menus"
      row-key="id"
      :tree-props="{ children: 'children' }"
      empty-text="暂无菜单，可点击「新增菜单」"
    >
      <el-table-column prop="name" label="菜单" min-width="180" />
      <el-table-column prop="path" label="路由" min-width="180" />
      <el-table-column prop="permissionCode" label="权限码" min-width="180" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditMenu(row)">编辑</el-button>
          <el-button link type="danger" @click="removeMenu(row)">删除</el-button>
        </template>
      </el-table-column>
    </AppDataTable>
    </div>
  </AppPageCard>

  <AppFormDialog
    v-model="roleDialog"
    title="分配角色"
    width="narrow"
    :confirm-loading="dialogSaving"
    @confirm="saveRoles"
  >
    <AppCheckboxPanel v-model="selectedRoles" :options="roleCheckboxOptions" placeholder="搜索角色编码或名称" />
  </AppFormDialog>

  <AppFormDialog
    v-model="permissionDialog"
    title="分配权限"
    width="wide"
    :confirm-loading="dialogSaving"
    @confirm="savePermissions"
  >
    <AppCheckboxPanel
      v-model="selectedPermissions"
      :options="permissionCheckboxOptions"
      placeholder="搜索权限码或名称"
      max-height="420px"
    />
  </AppFormDialog>

  <AppFormDialog
    v-model="menuDialog"
    :title="menuDialogTitle"
    width="medium"
    :confirm-loading="dialogSaving"
    @confirm="saveMenu"
  >
    <el-form ref="menuFormRef" :model="menuForm" :rules="menuRules" label-width="100px">
      <el-form-item label="父菜单">
        <el-tree-select
          v-model="menuForm.parentId"
          :data="parentOptions"
          node-key="id"
          :props="{ label: 'name', children: 'children' }"
          check-strictly
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="menuForm.name" maxlength="64" />
      </el-form-item>
      <el-form-item label="路由" prop="path">
        <el-input v-model="menuForm.path" maxlength="128" placeholder="/platform/example" />
      </el-form-item>
      <el-form-item label="图标">
        <el-input v-model="menuForm.icon" maxlength="64" placeholder="Element Plus 图标名" />
      </el-form-item>
      <el-form-item label="权限码">
        <el-input v-model="menuForm.permissionCode" maxlength="128" placeholder="可空" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="menuForm.sortOrder" :min="0" style="width: 100%" />
      </el-form-item>
      <el-form-item label="可见">
        <el-switch v-model="menuForm.visible" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="menuForm.status" style="width: 100%">
          <el-option
            v-for="opt in ENABLE_STATUS_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
  </AppFormDialog>
</template>
