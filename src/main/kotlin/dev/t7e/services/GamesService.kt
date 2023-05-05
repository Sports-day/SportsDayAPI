package dev.t7e.services

import dev.t7e.models.Game
import dev.t7e.models.GameEntity

/**
 * Created by testusuke on 2023/05/05
 * @author testusuke
 */
object GamesService: StandardService<GameEntity, Game>(
    objectName = "game",
    _getAllObjectFunction = { GameEntity.getAll() },
    _getObjectByIdFunction = { GameEntity.getById(it) },
    fetchFunction = { GameEntity.fetch(it) }
) {

}