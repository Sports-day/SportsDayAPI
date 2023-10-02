# configuration

## Description

<details>
<summary><strong>Table Definition</strong></summary>

```sql
CREATE TABLE `configuration` (
  `id` int NOT NULL AUTO_INCREMENT,
  `key` varchar(128) NOT NULL,
  `value` varchar(1024) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

</details>

## Columns

| Name | Type | Default | Nullable | Extra Definition | Children | Parents | Comment |
| ---- | ---- | ------- | -------- | ---------------- | -------- | ------- | ------- |
| id | int |  | false | auto_increment |  |  |  |
| key | varchar(128) |  | false |  |  |  |  |
| value | varchar(1024) |  | false |  |  |  |  |
| updated_at | datetime(6) |  | false |  |  |  |  |

## Constraints

| Name | Type | Definition |
| ---- | ---- | ---------- |
| PRIMARY | PRIMARY KEY | PRIMARY KEY (id) |

## Indexes

| Name | Definition |
| ---- | ---------- |
| PRIMARY | PRIMARY KEY (id) USING BTREE |

## Relations

![er](configuration.svg)

---

> Generated by [tbls](https://github.com/k1LoW/tbls)