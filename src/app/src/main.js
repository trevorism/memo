import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import { createVuestic } from 'vuestic-ui'
import config from '../vuestic.config.js'
import VueMixpanel from 'vue-mixpanel'
import './style.css'
import { installAuthRefresh, startProactiveRefresh } from './utils/authRefresh'

// Silently renew the session token (15-min lifetime) so users aren't logged out
// mid-use: a 401 interceptor refreshes-and-retries, plus a proactive timer.
installAuthRefresh()
startProactiveRefresh()

const app = createApp(App)
app.use(router)
app.use(createVuestic({ config }))
app.use(VueMixpanel, {
    token: "3ad96fc246692627d5addd73aa6072ae",
    config: {
        track_pageview: true,
        secure_cookie: true,
        same_site: 'None'
    },
})
app.mount('#app')
