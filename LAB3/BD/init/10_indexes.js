
// indice TTL
// Expira automáticamente los documentos 30 días
db.consumption.createIndex(
    { "Created_at": 1 },
    { expireAfterSeconds: 2592000, name: "metrica_ttl_idx" }
);

// indice compuesto busqueda rapida de instancias (servidores)
db.instance.createIndex(
    { "userId": 1, "State": 1 },
    { name: "cliente_estado_idx" }
);

