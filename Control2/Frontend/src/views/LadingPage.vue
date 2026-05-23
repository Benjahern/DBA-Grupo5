<template>
  <div class="landing-wrapper">
    <!-- Navbar con botón Ingresar -->
    <nav class="navbar navbar-expand-lg navbar-dark fixed-top">
      <div class="container">
        <a class="navbar-brand d-flex align-items-center" href="#">
          <span>Host Cloud Usach</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav ms-auto">
            <li class="nav-item">
              <router-link to="/login" class="btn btn-primary btn-ingresar">
                Ingresar
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <header class="hero-section d-flex align-items-center">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-lg-6">
            <h1 class="hero-title animate-fade-in">
              Infraestructura Cloud para la Universidad de Santiago de Chile
            </h1>
            <p class="hero-subtitle animate-fade-in delay-1">
              Host Cloud Usach es tu plataforma de gestión de instancias virtuales,
              bases de datos y recursos en la nube. Diseñada para estudiantes,
              investigadores y proyectos académicos de la Usach.
            </p>
            <div class="hero-buttons animate-fade-in delay-2">
              <router-link to="/login" class="btn btn-primary btn-lg me-3">
                Comenzar ahora
              </router-link>
              <a href="#caracteristicas" class="btn btn-outline-light btn-lg">
                Conocer más
              </a>
            </div>
          </div>
          <div class="col-lg-6 text-center mt-4 mt-lg-0">
            <div class="hero-visual">
              <div class="cloud-icon">
                <i class="bi bi-cloud"></i>
              </div>
              <div class="instance-cards">
                <div class="instance-card">
                  <i class="bi bi-cpu"></i>
                  <span>2 vCPU</span>
                </div>
                <div class="instance-card">
                  <i class="bi bi-memory"></i>
                  <span>4 GB RAM</span>
                </div>
                <div class="instance-card">
                  <i class="bi bi-hdd"></i>
                  <span>100 GB</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Características Section -->
    <section id="caracteristicas" class="features-section">
      <div class="container">
        <h2 class="section-title text-center mb-5">¿Por qué elegir Host Cloud Usach?</h2>
        <div class="row g-4">
          <div class="col-md-4">
            <div class="feature-card">
              <div class="feature-icon">
                <i class="bi bi-lightning-charge"></i>
              </div>
              <h3>Despliegue Rápido</h3>
              <p>Crea instancias virtuales en minutos. Nuestro sistema automatizado aprovisiona recursos en la nube de forma inmediata.</p>
            </div>
          </div>
          <div class="col-md-4">
            <div class="feature-card">
              <div class="feature-icon">
                <i class="bi bi-shield-check"></i>
              </div>
              <h3>Seguridad Garantizada</h3>
              <p>Autenticación mediante JWT. Tus datos y proyectos están protegidos con los más altos estándares.</p>
            </div>
          </div>
          <div class="col-md-4">
            <div class="feature-card">
              <div class="feature-icon">
                <i class="bi bi-graph-up"></i>
              </div>
              <h3>Monitoreo en Tiempo Real</h3>
              <p>Visualiza el consumo de recursos, estadísticas de rendimiento y estado de tus instancias en todo momento.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Info Section -->
    <section class="info-section">
      <div class="container text-center">
        <h2>Gestión Integral de Recursos</h2>
        <p class="lead">Administra CPUs, memoria RAM, almacenamiento y ancho de banda desde un único panel de control.</p>
        <ul class="feature-list text-start d-inline-block">
          <li><i class="bi bi-check-circle"></i> Creación y eliminación de instancias</li>
          <li><i class="bi bi-check-circle"></i> Inicio, detención y reinicio de servicios</li>
          <li><i class="bi bi-check-circle"></i> Asignación automática de direcciones IP</li>
          <li><i class="bi bi-check-circle"></i> Tracking de consumo por usuario</li>
        </ul>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="container text-center position-relative">
        <p class="mb-2">Host Cloud Usach — Proyecto académico Bases de datos avanzadas</p>
        <p class="text-white-50 small">Universidad de Santiago de Chile</p>
        <div class="usach-image" aria-hidden="true"></div>
      </div>
    </footer>
  </div>
</template>

<script setup></script>

<style scoped>
.landing-wrapper {
  min-height: 100vh;
  background: #2c3e50;
  color: white;
  font-family: 'Space Grotesk', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Navbar */
.navbar {
  background: #1a252f;
  backdrop-filter: blur(10px);
  border-bottom: 2px solid #1abc9c;
}

.navbar-brand {
  font-size: 1.4rem;
  font-weight: 600;
  color: #fff !important;
}

.logo-usach {
  height: 40px;
  width: auto;
}

.btn-ingresar {
  background: #1abc9c;
  border: none;
  color: #1a252f;
  font-weight: 600;
  padding: 0.5rem 1.5rem;
  transition: all 0.2s;
}

.btn-ingresar:hover {
  background: #16a085;
  transform: translateY(-2px);
}

/* Hero Section */
.hero-section {
  min-height: 100vh;
  padding-top: 80px;
  background: linear-gradient(180deg, #2c3e50 0%, #1a252f 100%);
  display: flex;
  align-items: center;
}

.hero-title {
  font-size: 2.8rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.hero-subtitle {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 2rem;
  line-height: 1.8;
}

.hero-buttons :deep(.btn-primary) {
  background: #1abc9c;
  border: none;
  color: #1a252f;
  padding: 0.75rem 2rem;
  font-size: 1.1rem;
  font-weight: 600;
}

.hero-buttons :deep(.btn-outline-light) {
  border-color: #1abc9c;
  color: #1abc9c;
  padding: 0.75rem 2rem;
  font-size: 1.1rem;
}

.hero-buttons :deep(.btn-outline-light):hover {
  background: #1abc9c;
  border-color: #1abc9c;
  color: #1a252f;
}

/* Hero Visual */
.hero-visual {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
}

.cloud-icon {
  font-size: 7rem;
  color: #1abc9c;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

.instance-cards {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.instance-card {
  background: #ffffff;
  border: 2px solid #1abc9c;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.95rem;
  color: #1a252f;
  font-weight: 500;
}

.instance-card i {
  font-size: 1.5rem;
  color: #1abc9c;
}

/* Features Section */
.features-section {
  padding: 5rem 0;
  background: #34495e;
}

.section-title {
  font-size: 2.2rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 3rem;
}

.feature-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
  height: 100%;
  color: #333;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.3);
}

.feature-icon {
  font-size: 2.5rem;
  color: #1abc9c;
  margin-bottom: 1.25rem;
}

.feature-card h3 {
  font-size: 1.4rem;
  margin-bottom: 1rem;
  color: #1a252f;
  font-weight: 600;
}

.feature-card p {
  color: #555;
  line-height: 1.7;
}

/* Info Section */
.info-section {
  padding: 5rem 0;
  background: #2c3e50;
}

.info-section h2 {
  font-size: 2.2rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 1.5rem;
  align-items: center;

}

.info-section .lead {
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 2rem;
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-list li {
  padding: 0.75rem 0;
  font-size: 1.05rem;
  color: #ffffff;
}

.feature-list i {
  color: #1abc9c;
  margin-right: 0.75rem;
}

/* Dashboard Preview */
.dashboard-preview {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  width: 100%;
  max-width: 350px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.preview-header {
  background: #1abc9c;
  height: 40px;
}

.preview-content {
  display: flex;
  padding: 1rem;
  gap: 0.75rem;
}

.preview-sidebar {
  width: 30%;
  background: #e8e8e8;
  border-radius: 8px;
  min-height: 150px;
}

.preview-main {
  width: 70%;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.preview-stat {
  background: #e8e8e8;
  border-radius: 8px;
  height: 45px;
}

/* Footer */
.footer {
  padding: 2.5rem 0;
  background: #1a252f;
  border-top: 2px solid #1abc9c;
}

.footer p {
  margin: 0;
}

.usach-image {
  position: absolute;
  bottom: 10px;
  right: 10px;
  height: 40px;
  width: auto;
  opacity: 0.6;
}

/* Animations */
.animate-fade-in {
  opacity: 0;
  animation: fadeIn 0.8s ease forwards;
}

.delay-1 { animation-delay: 0.2s; }
.delay-2 { animation-delay: 0.4s; }

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 992px) {
  .hero-title {
    font-size: 2.2rem;
  }

  .hero-section .col-lg-6:last-child {
    margin-top: 3rem;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 1.8rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .hero-buttons {
    flex-direction: column;
  }

  .hero-buttons :deep(.btn) {
    width: 100%;
    margin-bottom: 0.75rem;
  }

  .cloud-icon {
    font-size: 5rem;
  }
}
</style>