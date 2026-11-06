import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由配置
// 仅登录/注册页对游客开放（requiresAuth: false），其余页面均需登录
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: {
      title: '注册',
      requiresAuth: false
    }
  },
  {
    // 前台统一布局：顶栏 + 固定侧边导航（首页/AI陪伴/评估/训练/个人中心共用）
    path: '/',
    component: () => import('@/views/layout/MainLayout.vue'),
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: {
          title: '首页',
          requiresAuth: true
        }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/index.vue'),
        meta: {
          title: 'AI陪伴',
          requiresAuth: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      },
      {
        path: 'scale',
        name: 'ScaleList',
        component: () => import('@/views/scale/index.vue'),
        meta: {
          title: '心理韧性评估',
          requiresAuth: true
        }
      },
      {
        path: 'scale/answer',
        name: 'ScaleAnswer',
        component: () => import('@/views/scale/answer.vue'),
        meta: {
          title: '测评答题',
          requiresAuth: true
        }
      },
      {
        path: 'scale/report/:recordId',
        name: 'ScaleReport',
        component: () => import('@/views/scale/report.vue'),
        meta: {
          title: '评估报告',
          requiresAuth: true
        }
      },
      {
        path: 'training',
        name: 'TrainingCourses',
        component: () => import('@/views/training/index.vue'),
        meta: {
          title: '心理韧性训练',
          requiresAuth: true
        }
      },
      {
        path: 'training/detail/:courseId',
        name: 'TrainingDetail',
        component: () => import('@/views/training/detail.vue'),
        meta: {
          title: '课程学习',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: {
      title: '管理后台',
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: '',
        redirect: '/admin/users'
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users.vue'),
        meta: {
          title: '用户管理'
        }
      },
      {
        path: 'scales',
        name: 'AdminScales',
        component: () => import('@/views/admin/scales.vue'),
        meta: {
          title: '量表管理'
        }
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/courses.vue'),
        meta: {
          title: '课程管理'
        }
      },
      {
        path: 'crisis',
        name: 'AdminCrisis',
        component: () => import('@/views/admin/crisis.vue'),
        meta: {
          title: '安全危机事件'
        }
      },
      {
        path: 'feedback',
        name: 'AdminFeedback',
        component: () => import('@/views/admin/feedback.vue'),
        meta: {
          title: '反馈处理'
        }
      },
      {
        path: 'notices',
        name: 'AdminNotices',
        component: () => import('@/views/admin/notices.vue'),
        meta: {
          title: '系统公告'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
// 无 token 访问受保护页面（requiresAuth: true）时强制跳转登录页，并记录原目标路径用于登录后回跳
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - AI陪伴与心理韧性成长平台`
  }

  // 判断是否需要登录
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      // 无 token 强制跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    // 管理员后台：已登录但非admin角色（如无token会403的接口页面）一律回首页
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      next('/')
      return
    }
    next()
  } else {
    // 已登录访问登录/注册页时跳转到首页，避免重复登录注册
    if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
      next('/')
    } else {
      next()
    }
  }
})

export default router
