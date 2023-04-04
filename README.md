# MeiliSearch-Plus

*Released `io.github.lamtong.msp:msp-spring-boot-starter:1.0.0.SNAPSHOT`*.

> ***MeiliSearch-Plus** is an enhanced component for **Java SDK** of **MeiliSearch**,
> aiming to **offer enhancement without modification***.
---

**MeiliSearch-Plus** provides enhanced approaches for **CURD** of **MeiliSearch**, *in the spirit of **Mybatis-Plus***.

Using **MeiliSearch-Plus** you can easily, for example:

* CURD of index
* Update primary key of index
* Update settings of index
* And more...

---



## Usage

Add MeiliSearch-Plus dependency to your **Spring-Boot** project

```xml
# Maven
<dependency>
    <groupId>io.github.lamtong.msp</groupId>
    <artifactId>msp-spring-boot-starter</artifactId>
    <version>1.0.0.SNAPSHOT</version>
</dependency>
```

Configure MeiliSearch-Plus properties in your **application.properties** or **application.yaml** with prefix **msp**,
for example

```yaml
msp:
  host-url: http://localhost:7700
  api-key: meili_master_key
  class-with-index: true
  use-class-name-as-default: false
  auto-update-settings-of-index: true
  auto-update-primary-key-of-index: true
```

At least, you must configure **host-url** and **api-key** to enable **MeiliSearch-Plus**, or **MeiliSearch-Plus** will
fail to start.

Annotate your **Spring-Boot** project's main function with **@ScanMapper** as below.

```java
package com.github.lamtong.msp;

import io.github.lamtong.msp.autoconfigure.ScanMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@ScanMapper
@SpringBootApplication
public class MspApplication {

    public static void main(String[] args) {
        SpringApplication.run(MspApplication.class, args);
    }

}
```

Code your entity like

```java
package com.github.lamtong.msp.entity;

import io.github.lamtong.msp.core.annotation.Index;
import io.github.lamtong.msp.core.annotation.PrimaryKey;

@Index(value = "user")
public class User {

    @PrimaryKey
    private long id;

    private String name;

    private String password;

    private String address;

    private String birth;

    private String remark;

    // constructor, getter, setter and toString...

}
```

By default, **@Index** and **@PrimaryKey** are required to indicate the name of index and the primary key of index
respectively.

Extends interface **BaseMapper** to acquire **CRUD** capacities, for example

```java
package com.github.lamtong.msp.mapper;

import com.github.lamtong.msp.entity.User;
import io.github.lamtong.msp.core.annotation.Mapper;
import io.github.lamtong.msp.core.mapper.BaseMapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

Annotation **@Mapper** is mandatory to required to indicate that **UserMapper** defined above is intending to extend **BaseMapper** provided by **MeiliSearch-Plus**.

**Note** that class which extends **BaseMapper** is supported to be an interface rather a class. **MeiliSearch-Plus**
will generate corresponding implementations using **javassist** library itself.

Now you can auto-wire **UserMapper** in you code and operate corresponding index with embedded **default** methods
provided by **BaseMapper**. for example

```java
package com.github.lamtong.msp.controller;

import com.github.lamtong.msp.entity.User;
import com.github.lamtong.msp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "user")
@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping(value = "insert")
    public User insert() {
        User user = new User();
        // ...
        this.userMapper.insert(user);
        return user;
    }
}
```

Not only that, you can customize methods to achieve specific operations such as

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {

    default User getUserById(Long id) {
        Index index = this.getIndex();
        User user;
        try {
            user = index.getDocument(String.valueOf(id), User.class);
        } catch (MeilisearchException e) {
            throw new RuntimeException(e);
        }
        return user;
    }
}
```

**Aware** that any methods defined by yourself must be **default**, and any operations on index **user** rely on **Index** instance provided by **BaseMapper** by calling `this.getIndex()`. 

For more information, see `io.github.lamtong.msp.core.mapper.BaseMapper`.

Now yon can enjoy **MeiliSearch** in your **Spring-Boot** project.

> You may notice that some ides may prompt that `Could not autowire. No beans of 'UserMapper' type found.`, just ignore it! **msp-spring-boot-starter** will register implementation of **UserMapper** into spring container. It can always be auto-wired.

---



## Core Classes

### BaseMapper

`io.github.lamtong.msp.core.mapper.BaseMapper` is the base mapper interface of **MeiliSearch-Plus** with embedded **CRUD** methods. This interfaces is supported to be extended to operate on index of **MeiliSearch**.

**Note** that almost all methods **MeiliSearch** client is asynchronous except for retrieving documents from index, methods in **BaseMapper** works the same by ignoring **TaskInfo** instance returned by **Index**.

>  ***Synchronous operations has be added soon now, under test***.

---



### MapperScanner

`io.github.lamtong.msp.core.mapper.MapperScanner` scans mapper interfaces, which should extend  `io.github.lamtong.msp.core.mapper.BaseMapper` directly and be annotated with `io.github.lamtong.msp.core.annotation.Mapper` annotation, and returns scanned mapper classes in a List.

**MapperScanner** scans mapper interfaces using *[reflections](https://github.com/ronmamo/reflections)* library. 

---



### MapperImplementationFactory

`io.github.lamtong.msp.core.mapper.MapperImplementationFactory` is factory to generate implementations of scanned mapper interfaces and returns implementation classes than instances.

**MapperImplementationFactory** generates implementations using *[javassist](https://github.com/jboss-javassist/javassist)* library.

---



### MapperValidator

`io.github.lamtong.msp.core.mapper.MapperValidator` validates generic type parameter of **BaseMapper** when extending this interface, checking that entity class is annotated with **@Index** and **@PrimaryKey** or not, updating primary key of index, updating setting of index and binding implementation class with corresponding **Index** instance.

**MapperValidator** validates entity class’s fields retrieved by *[reflections](https://github.com/ronmamo/reflections)* library.

---


## Prerequisites

OpenJDK 1.8 or above.

SpringBoot 2.7.x, not SpringBoot 3.x.x

---


## Contribute

Pull requests are welcomed!

You can do what you want to with **Apache 2** license.

***Cheers !***

---

