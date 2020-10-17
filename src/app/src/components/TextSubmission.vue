<template>
  <div id="textSubmission" class="container">
    <section class="hero is-link">
      <div class="hero-body">
        <div class="container">
          <div class="title">
            Tell us anything
          </div>
          <div class="subtitle mt-2">
            <p>Our staff will read every word.</p>
            <p>100% Anonymous and Confidential.</p>
          </div>
        </div>
      </div>
    </section>
    <form class="loginBorder">
      <b-field>
        <b-input type="textarea"
                 minlength="1"
                 v-model="text"
                 maxlength="500"
                 placeholder="Your message here">
        </b-input>
      </b-field>
      <stripe type="text" :textValue="text"></stripe>
    </form>
  </div>
</template>

<script>
import Stripe from './payment/Stripe'

export default {
  name: 'TextSubmission',
  components: {Stripe},
  data () {
    return {
      text: ''
    }
  }
}
</script>

<style scoped>
  .loginBorder{
    border: #dddddd 1px solid;
    background: #efefef;
    padding-bottom: 30px;
  }
</style>
