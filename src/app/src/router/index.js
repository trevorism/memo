import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import ForgotPassword from '../components/ForgotPassword.vue'
import Home from "../components/Home.vue";
import Register from "../components/Register.vue";
import Upload from "../components/Upload.vue";
import PhotoDetails from '../components/PhotoDetails.vue'
import FolderDetails from '../components/FolderDetails.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home,
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
      path: '/upload/',
      name: 'Upload',
      component: Upload,
      props: true
    },
    {
      path: '/photo/:imageId/',
      name: 'PhotoDetails',
      component: PhotoDetails,
      props: true
    },
    {
      path: '/folder/:folderId/',
      name: 'FolderDetails',
      component: FolderDetails,
      props: true
    }
  ]
})

export default router
