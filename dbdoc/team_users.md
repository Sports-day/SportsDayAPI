# team_users

## Description

<details>
<summary><strong>Table Definition</strong></summary>

```sql
CREATE TABLE `team_users` (
  `team` int NOT NULL,
  `user` int NOT NULL,
  PRIMARY KEY (`team`,`user`),
  KEY `fk_team_users_user__id` (`user`),
  CONSTRAINT `fk_team_users_team__id` FOREIGN KEY (`team`) REFERENCES `teams` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_team_users_user__id` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

</details>

## Columns

| Name | Type | Default | Nullable | Children | Parents | Comment |
| ---- | ---- | ------- | -------- | -------- | ------- | ------- |
| team | int |  | false |  | [teams](teams.md) |  |
| user | int |  | false |  | [users](users.md) |  |

## Constraints

| Name | Type | Definition |
| ---- | ---- | ---------- |
| fk_team_users_team__id | FOREIGN KEY | FOREIGN KEY (team) REFERENCES teams (id) |
| fk_team_users_user__id | FOREIGN KEY | FOREIGN KEY (user) REFERENCES users (id) |
| PRIMARY | PRIMARY KEY | PRIMARY KEY (team, user) |

## Indexes

| Name | Definition |
| ---- | ---------- |
| fk_team_users_user__id | KEY fk_team_users_user__id (user) USING BTREE |
| PRIMARY | PRIMARY KEY (team, user) USING BTREE |

## Relations

![er](team_users.svg)

---

> Generated by [tbls](https://github.com/k1LoW/tbls)