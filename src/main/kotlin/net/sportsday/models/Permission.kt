package net.sportsday.models

import kotlinx.serialization.Serializable
import net.sportsday.models.Permission.*
import net.sportsday.models.Permission.Game
import net.sportsday.models.Permission.Image
import net.sportsday.models.Permission.Information
import net.sportsday.models.Permission.Location
import net.sportsday.models.Permission.Match
import net.sportsday.models.Permission.Sport
import net.sportsday.models.Permission.Tag
import net.sportsday.models.Permission.Team
import net.sportsday.models.Permission.TeamTag
import net.sportsday.models.Permission.User

/**
 * Created by testusuke on 2024/04/05
 * @author testusuke
 */

@Serializable
sealed class Permission(
    val name: String,
    val description: String
) {
    @Serializable
    object AccessPolicy : Permission(
        name = "AccessPolicy",
        description = "アクセスポリシー"
    ) {
        @Serializable
        object Read : Permission(
            name = "AccessPolicy.Read",
            description = "アクセスポリシーの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "AccessPolicy.Write",
            description = "アクセスポリシーの書き込み"
        )
    }

    @Serializable
    object Class : Permission(
        name = "Class",
        description = "クラス"
    ) {
        @Serializable
        object Read : Permission(
            name = "Class.Read",
            description = "クラスの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Class.Write",
            description = "クラスの書き込み"
        )
    }

    @Serializable
    object User : Permission(
        name = "User",
        description = "ユーザー"
    ) {
        @Serializable
        object Read : Permission(
            name = "User.Read",
            description = "ユーザーの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "User.Write",
            description = "ユーザーの書き込み"
        )

        @Serializable
        object Role : Permission(
            name = "User.Role",
            description = "ユーザーのロール"
        ) {
            @Serializable
            object Read : Permission(
                name = "User.Role.Read",
                description = "ユーザーのロールの読み取り"
            )

            @Serializable
            object Write : Permission(
                name = "User.Role.Write",
                description = "ユーザーのロールの書き込み"
            )
        }
    }

    @Serializable
    object Team : Permission(
        name = "Team",
        description = "チーム"
    ) {
        @Serializable
        object Read : Permission(
            name = "Team.Read",
            description = "チームの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Team.Write",
            description = "チームの書き込み"
        )
    }

    @Serializable
    object TeamTag : Permission(
        name = "TeamTag",
        description = "チームタグ"
    ) {
        @Serializable
        object Read : Permission(
            name = "TeamTag.Read",
            description = "チームタグの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "TeamTag.Write",
            description = "チームタグの書き込み"
        )
    }

    @Serializable
    object Tag : Permission(
        name = "Tag",
        description = "タグ"
    ) {
        @Serializable
        object Read : Permission(
            name = "Tag.Read",
            description = "タグの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Tag.Write",
            description = "タグの書き込み"
        )
    }

    @Serializable
    object Sport : Permission(
        name = "Sport",
        description = "スポーツ"
    ) {
        @Serializable
        object Read : Permission(
            name = "Sport.Read",
            description = "スポーツの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Sport.Write",
            description = "スポーツの書き込み"
        )
    }

    @Serializable
    object Game : Permission(
        name = "Game",
        description = "ゲーム"
    ) {
        @Serializable
        object Read : Permission(
            name = "Game.Read",
            description = "ゲームの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Game.Write",
            description = "ゲームの書き込み"
        )
    }

    @Serializable
    object Match : Permission(
        name = "Match",
        description = "マッチ"
    ) {
        @Serializable
        object Read : Permission(
            name = "Match.Read",
            description = "マッチの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Match.Write",
            description = "マッチの書き込み"
        )
    }

    @Serializable
    object Location : Permission(
        name = "Location",
        description = "ロケーション"
    ) {
        @Serializable
        object Read : Permission(
            name = "Location.Read",
            description = "ロケーションの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Location.Write",
            description = "ロケーションの書き込み"
        )
    }

    @Serializable
    object Information : Permission(
        name = "Information",
        description = "情報"
    ) {
        @Serializable
        object Read : Permission(
            name = "Information.Read",
            description = "情報の読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Information.Write",
            description = "情報の書き込み"
        )
    }

    @Serializable
    object Image : Permission(
        name = "Image",
        description = "画像"
    ) {
        @Serializable
        object Read : Permission(
            name = "Image.Read",
            description = "画像の読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Image.Write",
            description = "画像の書き込み"
        )
    }

    @Serializable
    object PermissionManager : Permission(
        name = "Permission",
        description = "権限管理"
    ) {
        @Serializable
        object Read : Permission(
            name = "Permission.Read",
            description = "権限管理の読み取り"
        )
    }

    @Serializable
    object Role : Permission(
        name = "Role",
        description = "ロール"
    ) {
        @Serializable
        object Read : Permission(
            name = "Role.Read",
            description = "ロールの読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Role.Write",
            description = "ロールの書き込み"
        )
    }

    @Serializable
    object Configuration : Permission(
        name = "Configuration",
        description = "設定"
    ) {
        @Serializable
        object Read : Permission(
            name = "Configuration.Read",
            description = "設定の読み取り"
        )

        @Serializable
        object Write : Permission(
            name = "Configuration.Write",
            description = "設定の書き込み"
        )
    }
}

object PermissionList {

    private val permissions = listOf(
        AccessPolicy.Read,
        AccessPolicy.Write,
        Class.Read,
        Class.Write,
        User.Read,
        User.Write,
        User.Role,
        User.Role.Read,
        User.Role.Write,
        Team.Read,
        Team.Write,
        TeamTag.Read,
        TeamTag.Write,
        Tag.Read,
        Tag.Write,
        Sport.Read,
        Sport.Write,
        Game.Read,
        Game.Write,
        Match.Read,
        Match.Write,
        Location.Read,
        Location.Write,
        Information.Read,
        Information.Write,
        Image.Read,
        Image.Write,
        PermissionManager.Read,
        Permission.Role.Read,
        Permission.Role.Write,
        Configuration.Read,
        Configuration.Write,
    )

    /**
     * Get all permissions
     */
    fun getAll(): List<Permission> = permissions

    /**
     * Get permission by name
     */
    fun getByName(name: String): Permission? {
        return permissions.find { it.name == name }
    }
}
