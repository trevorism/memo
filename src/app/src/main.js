import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import { createVuestic } from 'vuestic-ui'
import config from '../vuestic.config.js'
import './style.css'
import { TrevorismAuth } from '@trevorism/ui-auth'

const app = createApp(App)
app.use(router)
app.use(TrevorismAuth, { router, loginPath: '/login' })
app.use(createVuestic({ config }))

app.mount('#app')
