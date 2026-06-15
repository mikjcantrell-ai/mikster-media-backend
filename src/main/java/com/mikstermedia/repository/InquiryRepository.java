package com.mikstermedia.repository;

import com.mikstermedia.model.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data repository for {@link Inquiry} contact-form records.
 */
@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    /** Look up all inquiries from the same sender email. */
    List<Inquiry> findBySenderEmail(String senderEmail);

    /** All inquiries ordered newest first. */
    List<Inquiry> findAllByOrderByReceivedDateDesc();

    /** Count of inquiries not yet read by admin. */
    long countByIsReadFalse();
}
