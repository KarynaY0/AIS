package ais.repository;

import ais.entity.Admin;
import ais.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Find admin by user
     * @param user the user to search for
     * @return Optional containing the admin if found
     */
    Optional<Admin> findByUser(User user);

    /**
     * Find admin by user ID
     * @param userId the user ID to search for
     * @return Optional containing the admin if found
     */
    Optional<Admin> findByUser_UserId(Long userId);

    /**
     * Find admin by username
     * @param username the username to search for
     * @return Optional containing the admin if found
     */
    @Query("SELECT a FROM Admin a WHERE a.user.username = :username")
    Optional<Admin> findByUsername(@Param("username") String username);

    /**
     * Check if admin exists for a user
     * @param user the user to check
     * @return true if admin exists, false otherwise
     */
    boolean existsByUser(User user);

    /**
     * Check if admin exists by user ID
     * @param userId the user ID to check
     * @return true if admin exists, false otherwise
     */
    boolean existsByUser_UserId(Long userId);

    /**
     * Count total number of admins
     * @return total count of admins
     */
    @Query("SELECT COUNT(a) FROM Admin a")
    long countAllAdmins();
}