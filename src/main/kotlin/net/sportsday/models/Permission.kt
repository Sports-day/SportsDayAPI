package net.sportsday.models

import kotlinx.serialization.Serializable

/**
 * Created by testusuke on 2024/04/05
 * @author testusuke
 */

@Serializable
sealed class Permission(
    val name: String,
    val description: String
) {
    object AccessPolicy: Permission(
        name = "AccessPolicy",
        description = "アクセスポリシー"
    ) {
        object Read: Permission(
            name = "AccessPolicy.Read",
            description = "アクセスポリシーの読み取り"
        )

        object Write: Permission(
            name = "AccessPolicy.Write",
            description = "アクセスポリシーの書き込み"
        )
    }

    object Class: Permission(
        name = "Class",
        description = "クラス"
    ) {
        object Read: Permission(
            name = "Class.Read",
            description = "クラスの読み取り"
        )

        object Write: Permission(
            name = "Class.Write",
            description = "クラスの書き込み"
        )
    }

    object User: Permission(
        name = "User",
        description = "ユーザー"
    ) {
        object Read: Permission(
            name = "User.Read",
            description = "ユーザーの読み取り"
        )

        object Write: Permission(
            name = "User.Write",
            description = "ユーザーの書き込み"
        )

        object Role: Permission(
            name = "User.Role",
            description = "ユーザーのロール"
        ) {
            object Read: Permission(
                name = "User.Role.Read",
                description = "ユーザーのロールの読み取り"
            )

            object Write: Permission(
                name = "User.Role.Write",
                description = "ユーザーのロールの書き込み"
            )
        }
    }

    object Team: Permission(
        name = "Team",
        description = "チーム"
    ) {
        object Read: Permission(
            name = "Team.Read",
            description = "チームの読み取り"
        )

        object Write: Permission(
            name = "Team.Write",
            description = "チームの書き込み"
        )
    }

    object TeamTag: Permission(
        name = "TeamTag",
        description = "チームタグ"
    ) {
        object Read: Permission(
            name = "TeamTag.Read",
            description = "チームタグの読み取り"
        )

        object Write: Permission(
            name = "TeamTag.Write",
            description = "チームタグの書き込み"
        )
    }

    object Tag: Permission(
        name = "Tag",
        description = "タグ"
    ) {
        object Read: Permission(
            name = "Tag.Read",
            description = "タグの読み取り"
        )

        object Write: Permission(
            name = "Tag.Write",
            description = "タグの書き込み"
        )
    }

    object Sport: Permission(
        name = "Sport",
        description = "スポーツ"
    ) {
        object Read: Permission(
            name = "Sport.Read",
            description = "スポーツの読み取り"
        )

        object Write: Permission(
            name = "Sport.Write",
            description = "スポーツの書き込み"
        )
    }

    object Game: Permission(
        name = "Game",
        description = "ゲーム"
    ) {
        object Read: Permission(
            name = "Game.Read",
            description = "ゲームの読み取り"
        )

        object Write: Permission(
            name = "Game.Write",
            description = "ゲームの書き込み"
        )
    }

    object Match: Permission(
        name = "Match",
        description = "マッチ"
    ) {
        object Read: Permission(
            name = "Match.Read",
            description = "マッチの読み取り"
        )

        object Write: Permission(
            name = "Match.Write",
            description = "マッチの書き込み"
        )
    }

    object Location: Permission(
        name = "Location",
        description = "ロケーション"
    ) {
        object Read: Permission(
            name = "Location.Read",
            description = "ロケーションの読み取り"
        )

        object Write: Permission(
            name = "Location.Write",
            description = "ロケーションの書き込み"
        )
    }

    object Information: Permission(
        name = "Information",
        description = "情報"
    ) {
        object Read: Permission(
            name = "Information.Read",
            description = "情報の読み取り"
        )

        object Write: Permission(
            name = "Information.Write",
            description = "情報の書き込み"
        )
    }

    object Image: Permission(
        name = "Image",
        description = "画像"
    ) {
        object Read: Permission(
            name = "Image.Read",
            description = "画像の読み取り"
        )

        object Write: Permission(
            name = "Image.Write",
            description = "画像の書き込み"
        )
    }

    object PermissionManager: Permission(
        name = "Permission",
        description = "権限管理"
    ) {
        object Read: Permission(
            name = "Permission.Read",
            description = "権限管理の読み取り"
        )

        object Write: Permission(
            name = "Permission.Write",
            description = "権限管理の書き込み"
        )
    }

    object Role: Permission(
        name = "Role",
        description = "ロール"
    ) {
        object Read: Permission(
            name = "Role.Read",
            description = "ロールの読み取り"
        )

        object Write: Permission(
            name = "Role.Write",
            description = "ロールの書き込み"
        )
    }

    companion object {
        private val permissions = listOf(
            AccessPolicy,
            AccessPolicy.Read,
            AccessPolicy.Write,
            Class,
            Class.Read,
            Class.Write,
            User,
            User.Read,
            User.Write,
            User.Role,
            User.Role.Read,
            User.Role.Write,
            Team,
            Team.Read,
            Team.Write,
            TeamTag,
            TeamTag.Read,
            TeamTag.Write,
            Tag,
            Tag.Read,
            Tag.Write,
            Sport,
            Sport.Read,
            Sport.Write,
            Game,
            Game.Read,
            Game.Write,
            Match,
            Match.Read,
            Match.Write,
            Location,
            Location.Read,
            Location.Write,
            Information,
            Information.Read,
            Information.Write,
            Image,
            Image.Read,
            Image.Write,
            PermissionManager,
            PermissionManager.Read,
            PermissionManager.Write,
            Role,
            Role.Read,
            Role.Write,
        )

        /**
         * Get all permissions
         */
        fun getAll(): List<Permission> = permissions

        /**
         * Get permission by name
         */
        fun getByName(name: String): Permission? = permissions.find { it.name == name }
    }
}
