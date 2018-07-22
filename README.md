# akka_cqrs

**To Run the cluster**

### Update host for Seed nodes in the shared/reference.conf

```cd shared && sbt publishLocal```

```cd ../banking```

 Run below command from above Folder in two diffrent terminal tabs

 Open a Terminal Run 
   
 ```sbt -DHTTP_PORT=8081 -DCLUSTER_PORT=2554 -DACCOUNT_MANAGEMENT_BIND_PORT=9091 -DNODE_HOSTNAME={YOUR_HOST} -DPERSISTENT_ENTITY_TIMEOUT=10s  "runMain com.navneetgupta.banking.Main"``` 
 
 Open Another Terminal Run  
   
 ```sbt -DHTTP_PORT=8080 -DCLUSTER_PORT=2553 -DACCOUNT_MANAGEMENT_BIND_PORT=9090 -DNODE_HOSTNAME={YOUR_HOST} -DPERSISTENT_ENTITY_TIMEOUT=10s  "runMain com.navneetgupta.banking.Main"```
