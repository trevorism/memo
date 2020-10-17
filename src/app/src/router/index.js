import Vue from 'vue'
import Router from 'vue-router'
import FrontPage from '@/components/FrontPage'
import TextSubmission from '@/components/TextSubmission'
import PaymentSuccess from '@/components/payment/PaymentSuccess'

Vue.use(Router)

export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      name: 'FrontPage',
      component: FrontPage
    },
    {
      path: '/text',
      name: 'TextSubmission',
      component: TextSubmission
    },
    {
      path: '/paymentsuccess',
      name: 'paymentSuccess',
      component: PaymentSuccess
    }
  ]
})
