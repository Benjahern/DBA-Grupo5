-- indexar el estado del servidor y la direccion iṕ
CREATE INDEX index_instance_ip_state ON "Instance" ("Ip_address", "State")