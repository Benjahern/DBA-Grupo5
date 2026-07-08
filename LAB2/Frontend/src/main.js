import { createApp } from 'vue'
import App from './App.vue'
import router from './routes/index.js'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'primeicons/primeicons.css'

const app = createApp(App)
app.use(router)
app.mount('#app')
