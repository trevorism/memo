<template>
  <div class="grid justify-items-center" id="register">
    <div class="container">
      <div class="grid justify-items-center">
        <h2 class=" text-xl font-bold py-6 my-6">Register for Memowand</h2>
        <va-form ref="registerForm" class="border-2 rounded-md w-80">
          <div class="mx-4 mt-4 mb-4">
            <va-input v-model="username"
                      type="text"
                      class="mb-6 w-full"
                      :rules="[(v) => v.length >= 3]"
                      required
                      error-messages="Must be at least 3 characters"
                      autofocus
                      label="Username"
                      minlength="3">
            </va-input>
            <va-input type="email"
                      class="mb-6 w-full"
                      required
                      label="Email"
                      error-messages="Must be a valid email address"
                      v-model="email">
            </va-input>
            <va-input type="password"
                      class="mb-6 w-full"
                      required
                      :rules="[(v) => v.length >= 6]"
                      error-messages="Must be at least 6 characters"
                      label="Password"
                      minlength="6"
                      v-model="newPassword">
            </va-input>
            <va-input type="password"
                      class="mb-6 w-full"
                      required
                      :rules="[(v) => v.length >= 6, (v) => (v === this.newPassword)]"
                      error-messages="Must be at least 6 characters and match the new password"
                      label="Repeat Password"
                      minlength="6"
                      v-model="repeatPassword">
            </va-input>
            <div class="grid justify-items-center">
              <va-button color="success" :disabled="disabled" @click="invokeButton">
                <VaInnerLoading :loading="disabled"> Submit </VaInnerLoading>
              </va-button>
            </div>
          </div>
        </va-form>
        <div v-if="successMessage !== ''" class="w-80 text-center">
          {{successMessage}}
          <va-chip flat class="grid justify-items-center basis-1/4" to="/">Home</va-chip>
        </div>
        <va-alert v-if="errorMessage.length > 0" class="w-80 text-center" color="danger">{{errorMessage}}</va-alert>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'register',
  data () {
    return {
      username: '',
      email: '',
      newPassword: '',
      repeatPassword: '',
      errorMessage: '',
      successMessage: '',
      disabled: false
    }
  },
  methods: {
    invokeButton: function () {
      let request = {
        username: this.username,
        email: this.email,
        password: this.repeatPassword
      }
      this.disabled = true
      this.errorMessage = ''
      axios.post('api/user', request)
          .then(() => {
            this.disabled = false
            this.clearFields()
            this.errorMessage = ''
            this.successMessage = 'Registration success! An email will be sent when your registration is activated.'
          })
          .catch(() => {
            this.errorMessage = 'Unable to complete your registration'
            this.successMessage = ''
            this.disabled = false
            this.clearFields()
          })
    },
    clearFields: function () {
      this.username = ''
      this.email = ''
      this.newPassword = ''
      this.repeatPassword = ''
      this.$refs.registerForm.reset();
    }
  }
}
</script>

<style scoped>

</style>
