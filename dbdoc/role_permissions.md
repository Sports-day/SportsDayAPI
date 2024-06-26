# role_permissions

## Description

<details>
<summary><strong>Table Definition</strong></summary>

```sql
CREATE TABLE `role_permissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role` int NOT NULL,
  `permission` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_role_permissions_role__id` (`role`),
  CONSTRAINT `fk_role_permissions_role__id` FOREIGN KEY (`role`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=[Redacted by tbls] DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

</details>

## Columns

| Name | Type | Default | Nullable | Extra Definition | Children | Parents | Comment |
| ---- | ---- | ------- | -------- | ---------------- | -------- | ------- | ------- |
| id | int |  | false | auto_increment |  |  |  |
| role | int |  | false |  |  | [roles](roles.md) |  |
| permission | varchar(64) |  | false |  |  |  |  |

## Constraints

| Name | Type | Definition |
| ---- | ---- | ---------- |
| fk_role_permissions_role__id | FOREIGN KEY | FOREIGN KEY (role) REFERENCES roles (id) |
| PRIMARY | PRIMARY KEY | PRIMARY KEY (id) |

## Indexes

| Name | Definition |
| ---- | ---------- |
| fk_role_permissions_role__id | KEY fk_role_permissions_role__id (role) USING BTREE |
| PRIMARY | PRIMARY KEY (id) USING BTREE |

## Relations

![er](role_permissions.svg)

---

> Generated by [tbls](https://github.com/k1LoW/tbls)
