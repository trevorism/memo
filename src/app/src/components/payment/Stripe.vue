<template>
  <div id="payment" class="container">
    <div class="is-centered has-text-centered">
      <button class="button is-info" :disabled="disabled" @click="invokeButton">
        Submit ($1.99)
        <b-loading :is-full-page="false" :active.sync="disabled" :can-cancel="false"></b-loading>
      </button>
    </div>
    <div v-if="successMessage !== ''" class="has-text-centered">{{successMessage}}
      <router-link class="is-info ml-4" to="/login">Login</router-link>
    </div>
    <div class="has-text-centered has-text-danger">{{errorMessage}}</div>
  </div>
</template>

<script>
import { loadStripe } from '@stripe/stripe-js'
import axios from 'axios'
const stripePromise = loadStripe('pk_live_51HbczGKUPlXay6LPD19T6KKerdVzh6pDRX1rSlDLQALdep9aQVoZ2gv9T17YptAw3Ac1mxBzfsqWoooQdPQtqYno00InsjrrOe')

export default {
  name: 'Stripe',
  props: ['type', 'textValue'],
  data () {
    return {
      disabled: false,
      errorMessage: '',
      successMessage: ''
    }
  },
  methods: {
    invokeButton: async function () {
      const stripe = await stripePromise
      let request = {
        text: this.textValue,
        type: this.type
      }
      this.disabled = true
      axios.post('api/text/submission', request)
        .then((response) => {
          this.disabled = false
          this.errorMessage = ''
          this.successMessage = 'Rerouting to payment provider...'
          stripe.redirectToCheckout({
            sessionId: response.data.id
          })
        })
        .catch(() => {
          this.errorMessage = 'Submission Error.'
          this.successMessage = ''
          this.disabled = false
        })
    }
  }
}
</script>

<style scoped>
.loginBorder {
  border: #dddddd 1px solid;
  background: #efefef;
}
.formWidth {
  width: 400px
}
</style>
