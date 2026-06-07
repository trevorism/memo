import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import PropertiesReader from 'properties-reader'
import axios from 'axios'

let token = ''
try {
  const properties = PropertiesReader('../main/resources/secrets.properties')
  const clientId = properties.get('localUser')
  const clientSecret = properties.get('localPassword')
  if (clientId && clientSecret) {
    axios
      .post('https://auth.trevorism.com/token', {
        id: clientId,
        password: clientSecret,
        type: 'user',
        tenantGuid: '606db07c-3733-4697-88de-bb159773ea94'
      })
      .then((response) => {
        token = response.data
      })
  }
} catch (e) {}

export default defineConfig({
  plugins: [tailwindcss(), vue()],
  server: {
    host: 'localhost',
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080/',
        changeOrigin: true,
        secure: false,
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('proxy error', err)
          })
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('Sending Request to the Target:', req.method, req.url)
            proxyReq.setHeader('Cookie', `session=${token}`)
          })
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            console.log('Received Response from the Target:', proxyRes.statusCode, req.url)
            const expires = new Date(new Date().getTime() + 60 * 15 * 1000).toUTCString()
            _res.setHeader('Set-Cookie', `user_name=test; Path=/; Expires=${expires}`)
          })
        }
      },
      '/_ah': {
        target: 'http://127.0.0.1:8080/',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
