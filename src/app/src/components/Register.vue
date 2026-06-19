<template>
  <div class="auth-wrap" id="register">
    <div class="auth-glow" aria-hidden="true"></div>
    <div class="auth-card app-card">
      <h2 class="text-2xl font-extrabold text-ink text-center">Create your account</h2>
      <p class="text-muted text-sm text-center mt-1 mb-6">Join <RouterLink to="/" class="brand-wordmark brand-link">Memowand</RouterLink></p>

      <va-form ref="registerForm">
        <va-input
          v-model="username"
          type="text"
          class="mb-5 w-full"
          :rules="[(v) => v.length >= 3]"
          required
          error-messages="Must be at least 3 characters"
          autofocus
          label="Username"
          minlength="3"
        >
        </va-input>
        <va-input
          type="email"
          class="mb-5 w-full"
          required
          label="Email"
          error-messages="Must be a valid email address"
          v-model="email"
        >
        </va-input>
        <va-input
          type="password"
          class="mb-5 w-full"
          required
          :rules="[(v) => v.length >= 6]"
          error-messages="Must be at least 6 characters"
          label="Password"
          minlength="6"
          v-model="newPassword"
        >
        </va-input>
        <va-input
          type="password"
          class="mb-6 w-full"
          required
          :rules="[(v) => v.length >= 6, (v) => v === this.newPassword]"
          error-messages="Must be at least 6 characters and match the new password"
          label="Repeat Password"
          minlength="6"
          v-model="repeatPassword"
        >
        </va-input>
        <va-button color="primary" gradient round class="w-full" :disabled="disabled" @click="invokeButton">
          <VaInnerLoading :loading="disabled"> Create account </VaInnerLoading>
        </va-button>
      </va-form>

      <div v-if="successMessage !== ''" class="text-center text-body mt-5">
        {{ successMessage }}
        <va-chip flat class="mt-2" to="/">Home</va-chip>
      </div>
      <va-alert v-if="errorMessage.length > 0" class="mt-5 text-center" color="danger">{{ errorMessage }}</va-alert>

      <p class="text-center text-sm text-muted mt-6">
        Already have an account?
        <router-link class="text-accent font-semibold" :to="{ name: 'Login' }">Sign in</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'register',
  data() {
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
        password: this.newPassword
      }
      this.disabled = true
      this.errorMessage = ''

      if (this.username.length < 3) {
        this.errorMessage = 'Username must be at least 3 characters'
        this.disabled = false
        return
      }

      if (this.email.length < 5 || !this.email.includes('@')) {
        this.errorMessage = 'Email must be a valid email address'
        this.disabled = false
        return
      }

      if (this.newPassword.length < 6) {
        this.errorMessage = 'Password must be at least 6 characters'
        this.disabled = false
        return
      }

      if (this.newPassword !== this.repeatPassword) {
        this.errorMessage = 'Passwords do not match'
        this.disabled = false
        return
      }

      axios
        .post('/api/user', request)
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
      this.$refs.registerForm.reset()
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
