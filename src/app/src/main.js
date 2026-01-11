import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import { createVuestic } from 'vuestic-ui'
import config from '../vuestic.config.js'

import './style.css'

const app = createApp(App)
app.use(router)
app.use(createVuestic({ config }))
app.mount('#app')
