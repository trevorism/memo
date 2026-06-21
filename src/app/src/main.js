import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import { createVuestic } from 'vuestic-ui'
import config from '../vuestic.config.js'
import mixpanel from 'mixpanel-browser'
import './style.css'
import { installAuthRefresh, startProactiveRefresh } from './utils/authRefresh'
import { isLoggedIn, getCurrentUserName } from './utils/auth'

installAuthRefresh()
startProactiveRefresh()

mixpanel.init("f364be892a57c11f8f6171626c7b8f37", {
    api_host: `${window.location.origin}/mp`,
    debug: import.meta.env.DEV,
    track_pageview: false,
    persistence: 'localStorage',
    ignore_dnt: true,
})

const app = createApp(App)
app.provide('mixpanel', mixpanel)
app.use(router)
app.use(createVuestic({ config }))

if (isLoggedIn()) {
    try {
        mixpanel.identify(getCurrentUserName())
    } catch {
        // Analytics is best-effort; never block startup on it.
    }
}

app.mount('#app')
