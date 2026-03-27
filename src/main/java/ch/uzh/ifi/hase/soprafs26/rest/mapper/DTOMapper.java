package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ChangePassDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);


	@Mapping(source = "username", target = "username")
	@Mapping(source = "password", target = "password")
	@Mapping(source = "bio", target = "bio")
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "creationDate", target = "creationDate")
	@Mapping(source = "winCount", target = "winCount")
	@Mapping(source = "winRatePercentage", target = "winRatePercentage")
	@Mapping(source = "totalGamesPlayed", target = "totalGamesPlayed")
	@Mapping(source = "totalPoints", target = "totalPoints")
	@Mapping(source = "rank", target = "rank")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "token", target = "token")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "creationDate", target = "creationDate")
	@Mapping(source = "winCount", target = "winCount")
	@Mapping(source = "winRatePercentage", target = "winRatePercentage")
	@Mapping(source = "totalGamesPlayed", target = "totalGamesPlayed")
	@Mapping(source = "totalPoints", target = "totalPoints")
	UserDTO convertEntityToUserDTO(User user);


	@Mapping(source = "newPassword", target = "password")
	User convertChangePassDTOtoEntity(ChangePassDTO changePassDTO);

	@Mapping(source = "maxNumPlayers", target = "maxNumPlayers")
	@Mapping(source = "gameDifficulty", target = "gameDifficulty")
	@Mapping(source = "gameLanguage", target = "gameLanguage")
	@Mapping(source = "gameMode", target = "gameMode")
	@Mapping(source = "maxSkips", target = "maxSkips")
	@Mapping(source = "timeLimitSeconds", target = "timeLimitSeconds")
	@Mapping(source = "numOfProblems", target = "numOfProblems")
	Room convertRoomPostDTOtoEntity(RoomPostDTO roomPostDTO);

	@Mapping(source = "roomId", target = "roomId")
	@Mapping(source = "roomJoinCode", target = "roomJoinCode")
	@Mapping(source = "maxNumPlayers", target = "maxNumPlayers")
	@Mapping(source = "currentNumPlayers", target = "currentNumPlayers")
	@Mapping(source = "roomOpen", target = "isRoomOpen")
	@Mapping(source = "hostUserId", target = "hostUserId")
	@Mapping(source = "playerIds", target = "playerIds")
	@Mapping(source = "gameDifficulty", target = "gameDifficulty")
	@Mapping(source = "gameLanguage", target = "gameLanguage")
	@Mapping(source = "gameMode", target = "gameMode")
	@Mapping(source = "maxSkips", target = "maxSkips")
	@Mapping(source = "timeLimitSeconds", target = "timeLimitSeconds")
	@Mapping(source = "numOfProblems", target = "numOfProblems")
	RoomGetDTO convertEntityToRoomGetDTO(Room room);
}
