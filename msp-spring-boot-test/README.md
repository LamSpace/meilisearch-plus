## msp-spring-boot-test

> A Spring-Boot test project for **msp-spring-boot-starter**, showing how to use **MeiliSearch-Plus** in your
**Spring-Boot** project.
---

### Start MeiliSearch

```shell
docker run -d \
  --name meili \
  -p 7700:7700 \
  -e MEILI_ENV='development' \
  -e MEILI_MASTER_KEY='meili_master_key' \
  -v $(pwd)/meili_data:/meili_data \
  getmeili/meilisearch:v1.0
```

---
