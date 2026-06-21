import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import { createVuestic } from 'vuestic-ui'
import config from '../vuestic.config.js'
import VueMixpanel from 'vue-mixpanel'
import mixpanel from 'mixpanel-browser'
import './style.css'
import { installAuthRefresh, startProactiveRefresh } from './utils/authRefresh'
import { isLoggedIn, getCurrentUserName } from './utils/auth'

installAuthRefresh()
startProactiveRefresh()

const app = createApp(App)
app.use(VueMixpanel, {
    token: "f364be892a57c11f8f6171626c7b8f37",
    config: {
        api_host: `${window.location.origin}/mp`,
        debug: import.meta.env.DEV,
        track_pageview: false,
        persistence: 'localStorage'
    },
})
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
