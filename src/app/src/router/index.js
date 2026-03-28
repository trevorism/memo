import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import ForgotPassword from '../components/ForgotPassword.vue'
import mixpanel from 'mixpanel-browser';
import SplashPage from "../components/SplashPage.vue";
import Register from "../components/Register.vue";
import Welcome from "../components/Welcome.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Splash',
      component: SplashPage,
      props: true
    },
    {
      path: '/login/',
      name: 'Login',
      component: Login,
      props: true
    },
    {
      path: '/register/',
      name: 'Register',
      component: Register,
      props: true
    },
    {
      path: '/forgot/',
      name: 'ForgotPassword',
      component: ForgotPassword,
      props: true
    },
    {
      path: '/welcome/',
      name: 'Welcome',
      component: Welcome,
      props: true
    }
  ]
})

router.afterEach((to) => {
  mixpanel.track(to.fullPath)
})

export default router
