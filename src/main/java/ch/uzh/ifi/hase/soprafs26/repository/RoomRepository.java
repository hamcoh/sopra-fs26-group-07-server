package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Room;

@Repository("roomRepository")
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room findByRoomId(Long roomId);
    Room findByRoomJoinCode(String roomJoinCode);
}

