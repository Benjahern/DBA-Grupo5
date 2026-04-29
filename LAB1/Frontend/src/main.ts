import { createApp } from 'vue'
import App from './App.vue'
import router from './routes'
import 'primeicons/primeicons.css'

const app = createApp(App)
app.use(router)
app.mount('#app')
