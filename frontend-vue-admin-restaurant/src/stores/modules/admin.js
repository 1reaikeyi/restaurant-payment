import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginEmployee, logoutEmployee, registerEmployee, getEmployeeDetail } from '@/api/employee.js'
import { getUserIdFromToken } from '@/stores/modules/jwt.js'

export const useEmployeeStore
    = defineStore('takeout', () => {
        const token = ref('')

        const setToken = (newToken) => {
            token.value = newToken
        }
        const removeToken = () => {
            token.value = ''
        }

        const userId = computed(() => getUserIdFromToken(token.value))

        const user = ref({})

        const login = async (data) => {
            const res = await loginEmployee(data)
            return res
        }

        const register = async (data) => {
            const res = await registerEmployee(data)
            return res
        }

        const getUser = async () => {
            if (!userId.value) {
                console.error('无法获取用户ID，token可能无效')
                return
            }
            const res = await getEmployeeDetail(userId.value)
            user.value = res.data
        }

        const setUser = (obj) => {
            user.value = obj
        }

        return {
            token, setToken, removeToken,
            user, getUser, setUser, userId,
            login, register
        }
    },
    {
        persist: true
    }
)
