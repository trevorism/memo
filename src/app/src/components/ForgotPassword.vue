<template>
  <div id="forgot" class="auth-wrap">
    <div class="auth-glow" aria-hidden="true"></div>
    <div class="auth-card app-card">
      <h2 class="text-2xl font-extrabold text-ink text-center">Forgot password?</h2>
      <p class="text-muted text-sm text-center mt-1 mb-6">We'll send you a link to reset it.</p>

      <va-form autofocus>
        <va-input
          type="email"
          class="mb-6 w-full"
          required
          label="Email"
          autofocus
          error-messages="Must be a valid email address"
          v-model="email"
        >
        </va-input>
        <div class="flex gap-3">
          <va-button color="primary" gradient round class="flex-1" :disabled="disabled" @click="invokeButton">
            <va-inner-loading :loading="disabled"> Send link </va-inner-loading>
          </va-button>
          <va-button preset="secondary" round :to="{ name: 'Login' }"> Cancel </va-button>
        </div>
      </va-form>

      <div v-if="successMessage !== ''" class="text-center text-body mt-5">
        {{ successMessage }}
        <va-chip flat class="mt-2" :to="{ name: 'Login' }">Login</va-chip>
      </div>
      <va-alert v-if="errorMessage.length > 0" class="mt-5 text-center" color="danger">{{ errorMessage }}</va-alert>
    </div>
  </div>
</template>

<script>
import { forgotPassword } from '../utils/authApi'
import { isValidEmail } from '../utils/validation'

export default {
  name: 'ForgotPassword',
  data() {
    return {
      email: '',
      disabled: false,
      errorMessage: '',
      successMessage: ''
    }
  },
  methods: {
    invokeButton: function () {
      this.disabled = true
      this.errorMessage = ''

      if (!isValidEmail(this.email)) {
        this.errorMessage = 'Email must be a valid email address'
        this.disabled = false
        return
      }

      forgotPassword(this.email)
        .then(() => {
          this.disabled = false
          this.errorMessage = ''
          this.successMessage = 'Email sent successfully!'
        })
        .catch(() => {
          this.errorMessage = 'Unable to find the email address'
          this.successMessage = ''
          this.disabled = false
        })
    }
  }
}
</script>

<style scoped>
.auth-wrap {
  position: relative;
  min-height: calc(100vh - var(--header-h));
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 2rem 1rem;
}

.auth-glow {
  position: absolute;
  inset: -10% -10% auto -10%;
  height: 60%;
  background:
    radial-gradient(40% 60% at 30% 20%, color-mix(in srgb, var(--c-accent) 26%, transparent), transparent 70%),
    radial-gradient(45% 55% at 75% 25%, color-mix(in srgb, var(--c-accent-2) 24%, transparent), transparent 70%);
  filter: blur(20px);
  pointer-events: none;
  z-index: 0;
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 24rem;
  padding: 2rem;
}
</style>
