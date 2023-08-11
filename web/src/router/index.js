import { createRouter, createWebHistory } from 'vue-router'
import HomeIndexView from '@/views/HomeIndexView.vue'
import PkIndexView from '@/views/pk/PkIndexView.vue'
import MyTalkView from '@/views/talk/MyTalkView.vue'
import RecordIndexView from '@/views/record/RecordIndexView.vue'
import RecordContentView from '@/views/record/RecordContentView.vue'
import RanklistIndexView from '@/views/ranklist/RanklistIndexView.vue'
import UserBotIndexView from '@/views/user/bot/UserBotIndexView.vue'
import NotFound from '@/views/error/NotFound.vue'
import UserAccountLoginView from '@/views/user/account/UserLoginView.vue'
import UserAccountRegisterView from '@/views/user/account/UserAccountRegisterView.vue'
import store from '@/store/index'
const routes = [
    // {
    //   path: "/",
    //   name: "home",
    //   redirect: "/pk/",
    //   meta: {
    //     requestAuth: true
    //   }
    // },
    {
        path: "/",
        name: "home",
        component: HomeIndexView,
        meta:{
            requestAuth: true,
        }
    },
    {
      path: "/pk/",
      name: "pk_index",
      component: PkIndexView,
      meta: {
        requestAuth: true
      }
    },
    {
      path: "/record/",
      name: "record_index",
      component: RecordIndexView,
      meta: {
        requestAuth: true
      }
    },
    {
      path: "/record/:recordId/",
      name: "record_content",
      component: RecordContentView,
      meta: {
        requestAuth: true
      }
    },
    {
      path: "/ranklist/",
      name: "ranklist_index",
      component: RanklistIndexView,
      meta: {
        requestAuth: true
      }
    },
    {
      path: "/user/bot/",
      name: "user_bot_index",
      component: UserBotIndexView,
      meta: {
        requestAuth: true
      }
    },
    {
        path: "/user/talk/",
        name: "user_talk_index",
        component: MyTalkView,
        meta: {
            requestAuth: true
        }
    },
    {
      path: "/user/account/login/",
      name: "user_account_login",
      component: UserAccountLoginView,
      meta: {
        requestAuth: false,
      }
    },
    {
      path: "/user/account/register/",
      name: "user_account_register",
      component: UserAccountRegisterView,
      meta: {
        requestAuth: false,
      }
    },
    {
      path: "/404/",
      name: "404",
      component: NotFound,
      meta: {
        requestAuth: false
      }
    },
    {
      path: "/:catchAll(.*)",
      redirect: "/404/"
    }
  
]

const router = createRouter({
  history: createWebHistory(),
  routes
})
// to跳转到哪个页面， from表示从哪个页面跳转过去
// next的表示将页面要不要执行下一步操作，写之前首先要记录每一个未授权界面
router.beforeEach((to, from, next) => {
   if(to.meta.requestAuth && !store.state.user.is_login){//如果要跳转的页面需要登录，但是没有登录，就强制到登录界面
    next({name: "user_account_login"})
   }else{
    next();//跳转到默认页面
   }
})
export default router
