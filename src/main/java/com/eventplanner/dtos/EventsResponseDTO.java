package com.eventplanner.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventsResponseDTO
{
    private UUID eventId;

    private String title;

    private String location;

    private String dateTime;

    private OrganizerInfo organizerInfo;

    public static class OrganizerInfo
    {
        private String firstname;
        private String lastname;

        public OrganizerInfo() {
        }

        public OrganizerInfo(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }
    }
}
