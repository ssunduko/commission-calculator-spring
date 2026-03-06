package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================
 * JPA ENTITY: UserProfile
 * ============================================================
 *
 * CONCEPT: @OneToOne RELATIONSHIP (One-to-One)
 * =============================================
 * A One-to-One relationship means each row in Table A is linked to exactly
 * one row in Table B, and vice versa. It's the most restrictive relationship:
 *
 * WHEN TO USE ONE-TO-ONE:
 * - To split a wide table into a core table + optional/extended data
 * - When the related data is loaded infrequently (lazy loading benefit)
 * - To separate frequently-changing columns from stable ones
 * - Example: User (login info) + UserProfile (personal details)
 *
 * HOW ONE-TO-ONE WORKS IN JPA:
 * 1. The OWNING side has the @JoinColumn (foreign key column in its table).
 *    Here, UserProfile owns the relationship with a 'user_id' FK column.
 * 2. The INVERSE side uses mappedBy to point back to the owning field.
 *    Here, User has @OneToOne(mappedBy = "user") which is the read-only mirror.
 * 3. Only ONE UserProfile can reference a given User (enforced by unique = true
 *    on the FK column, preventing multiple profiles per user).
 *
 * OWNING SIDE vs. INVERSE SIDE:
 * - Owning side (UserProfile.user): Has @JoinColumn, controls the FK in the DB.
 *   Setting profile.setUser(user) writes the foreign key.
 * - Inverse side (User.profile): Has mappedBy, is a read-only mirror.
 *   Setting user.setProfile(profile) alone does NOT persist the FK.
 *   You must ALWAYS set the owning side for the relationship to be saved.
 *
 * CONTRAST WITH @ManyToOne / @OneToMany:
 * - @ManyToOne: Many rows in Table A can reference the SAME row in Table B
 *   (e.g., many Deals to one SalesRep). No uniqueness constraint on the FK.
 * - @OneToOne: Only ONE row in Table A can reference a given row in Table B.
 *   The FK column has a UNIQUE constraint, enforcing the 1:1 cardinality.
 *
 * FETCH STRATEGY:
 * - FetchType.LAZY is used so the User is not loaded every time we access
 *   a UserProfile. This avoids unnecessary JOIN queries.
 * - CAUTION: @OneToOne on the INVERSE side (User.profile) can be difficult
 *   to truly lazy-load in some JPA implementations because JPA needs to
 *   know whether to set the field to null or to a proxy.
 *
 * CASCADE:
 * - No CascadeType here because UserProfile and User have independent
 *   lifecycles. Deleting a profile should NOT delete the user.
 * - If you wanted the profile to be auto-deleted when the user is deleted,
 *   you would add CascadeType.ALL + orphanRemoval on the User.profile side.
 *
 * DATABASE DESIGN:
 * - 'user_profiles' table with a 'user_id' FK column (UNIQUE constraint)
 * - The UNIQUE constraint on user_id is what makes this 1:1, not 1:N
 */
@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "idx_user_profile_user_id", columnList = "user_id", unique = true)
})
@Data
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * OWNING SIDE of the @OneToOne relationship.
     *
     * @JoinColumn(name = "user_id"): Creates a 'user_id' FK column in the
     *   'user_profiles' table that references 'users.id'.
     *
     * unique = true: This is the KEY difference from @ManyToOne.
     *   It enforces that only ONE UserProfile row can point to a given User,
     *   making the relationship truly one-to-one at the database level.
     *
     * nullable = false: Every profile must belong to a user.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @Column(length = 2000)
    private String bio;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "job_title")
    private String jobTitle;

    @Column
    private String location;

    public UserProfile(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id='" + id + '\'' +
                ", userId='" + (user != null ? user.getId() : null) + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", location='" + location + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
