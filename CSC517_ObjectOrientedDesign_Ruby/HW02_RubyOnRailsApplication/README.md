CSC517_HW02
Homework 2 (Ruby on Rails)
Tour Management System

Web Application deployment:
https://young-fortress-53892.herokuapp.com/

Admin:

    Preconfigured admin username: john@john.com 
    Preconfigured admin password: john_password
    
Testing:

    Please see the various "how to" instructions.

Landing Page:


    The app assumes that all visitors start on the same page.
    Tacit in that assumption is that the user will never type a URL like
    ".../photos" into their web browser.

    From the log-in page, a visitor may login to gain more functionality, 
    or they can see only the list of all tours and reviews.
    We are using the same login page to redirect all users to same user
    dashboard, but this dashboard has been customized to show only the relevant
    links to the user.
    
App Interactions

    Users interact with the app depending on their role. There are currently 
    four roles, each with an increasing ability to perform functions. The 
    four roles are visitors, customers, agents, and admins.
    
    Visitors are users of the system who have not logged into the system. 
    They are limited to browsing tours, browsing reviews, and creating an 
    account.

    Customers are users of the system who have registered with the system (as
    a customer) and have logged into the system. In addition to browsing 
    tours and reviews, customers can bookmark tours of interest, review tours
    that have completed, and book tours.
    
    Agents are users of the system who have registered with the system 
    (as an agent) and have logged into the system. Agents are able to 
    create tours, create locations, and can examine aggregate statistics 
    about the tours they have created (bookmarks, bookings, reviews, etc).
    
    The admin is a unique, preconfigured user that can perform all of the tasks
    that the other users can perform. The admin cannot change his password or
    username, but uniquely can create new users (customers or agents).

    Details about the roles of the different users and the steps required
    to perform diffeent tasks are outlined below.

Miscellaneous Notes:

    Testing:
    
        RSpec
        
            Per the requirements:
                Thoroughly test one model and one controller (prefer RSpec
                testing framework). Tests should cover all functions and handle
                all test cases, including edge cases.
            
            We chose RSpec testing of the locations controller and the bookmarks
            model. RSpec specifications can be found in 'spec/controllers' and
            'spec/models' directories, respectively. We used the 'simplecov' gem
            to assess the coverage of the modules.
            Contoller results:
                13 examples, 0 failures, 13 passed, 100% coverage
            Model results:
                9 examples, 0 failures, 9 passed, 100% coverage 
            
        Minitest
        
            Tests can be found in 'test/controllers' and 'test/models'
            directories.
            We have chosen NOT to focus our Minitest testing.
            Instead, we test a broader set of controllers and models in a
            slightly more shallow way.
            Controller results:
                80 tests, 108 assertions, 0 failures, 0 errors, 0 skips
            Model results:
                14 tests, 38 assertions, 0 failures, 0 errors, 0 skips
            
    Tour Operator Contact Info:
    
        Per Piazza:
            "...we have to create operator with some basic info?"
            "Agent information should be fine."
        This is a good idea and if we were starting over we likely would use
        agent profile information.
        However we already had a field for adding custom operator contact info
        when creating a tour.
        We have chosen to retain our custom contact info in the interest of
        flexibility.
        
    Price Filtering:
    
        Per Piazza:
            "...Is it acceptable to filter just by maximum price?"
            "There are no restrictions on the way you would like to implement
            any functionality."
        We offer price filtering by maximum price, but not by minimum price.
        This is on the theory that cheaper is always better (all else being
        equal).
    
    Booking / Waitlisting:
    
        There is no maximum value for the waitlist size.
        
        Instead of forcing the user to go through extra steps when
        "some, but not enough, seats are available", 
        we show the user how many seats areavailable and allow them to select among the following:
        "Book All Seats" / "Book Available Seats, Waitlist Remaining Seats" / "Waitlist All Seats".
        If the user selects an inappropriate option given the number of available seats,
        then they are instructed as to what went wrong.
        
        As noted in the requirements, if a customer cancels or reduces a booking,
        or if a customer cancels their account,
        waitlisted customers are enrolled in applicable tours.

    Enforcement of User Roles:
    
        As noted in "User Roles" different types of users have different
        privileges.
        Because there is often more than one way to access a given feature,
        we centralized the logic related to user roles and privileges.
        This logic can be found in app/helpers/sessions_helper.rb

    Locations:
    
        Locations are case sensitive. So, "NC, USA" is treated differently than
        "nc, USA". Be careful when adding a new location.
        
    "Extra" Model:

        Our E/R Diagram can be found at
        doc/CSC517_HW02_EntityRelationshipDiagram.png
        We have a "start_at" model that we ended up never really using.
        Instead, we find the start location for a tour based on order of records
        stored in the "visits" model.
        Time did not permit cleaning up this 'extra' code.
        
    "Extra" Views:
    
        We used scaffolding to start our application, which creates many views.
        Some of these views are never actually available to the user via a link.
        Most of these 'extra' views are retained:
            for time constraints
            for future development on the application if needed
            
Record Deletion FAQs:

    Here we discuss side effects of deleting certain kinds of records through the web application.
    We only discuss records that are visible to the user (tours, reviews, etc).
    We do not explicitly discuss other records that may exist "in the background" to support the application.

    1. What happens when a USER is deleted?
    
        The following information is deleted:
        
            * Bookmarks created by the user
            * Reviews created by the user
            * Bookings spots created by the user
                If this frees up tour spots, 
                other customers may be enrolled as noted in "Booking / Waitlisting"
            * Waitlist spots created by the user
            
        The following information is NOT deleted:
        
            * Tours created by the user
                such tours will no longer have an agent listed for them
            * Photos created by the user
            * Locations created by the user
    
    2. What happens when an ADMIN USER is deleted?
    
        Trick question: Admin account cannot be deleted!
    
    3. What happens when a BOOKMARK is deleted?
    
        No other information is deleted as a side effect of deleting a bookmark.
    
    4. What happens when a TOUR is deleted?
    
        The following information is deleted:
            
            * Bookmarks for the tour
            * Reviews of the tour
            * Booking / Waitlists spots for the tour
            * Photos of the tour
            
        The following information is NOT deleted:
        
            * Locations visited by the tour
    
    5. What happens when a REVIEW is deleted?
    
        No other information is deleted as a side effect of deleting a review.
            
        Specifically, the following information is NOT deleted:
        
            * Bookmarks created by the same user for the same tour
            * Booking / Waitlist spots created by the same user for the same tour
    
    6. What happens when a BOOKING is deleted?
    
        Please see "Booking / Waitlisting" for a full explanation.
            
        The following information is NOT deleted:
        
            * Bookmarks created by the same user for the same tour
            * Reviews created by the same user for the same tour
    
    7. What happens when a WAITLIST spot is deleted?
    
        No other information is deleted as a side effect of deleting a waitlist spot.
    
    8. What happens when a LOCATION is deleted?
    
        No other information is deleted as a side effect of deleting a location.
            
        Specifically, the following information is NOT deleted:
        
            * Tours that visit this location
                (tour itinerary will no longer include this location)
    
    9. What happens when a PHOTO is deleted?
    
        No other information is deleted as a side effect of deleting a photo.
        
User Roles:
    
    Visitor:
    
        * can see a list of all tours.
        * can see a list of all reviews.
        * can sign up for a new account.
        * can log in.
        
    Logged-In-User:
    
        * can do anything a visitor can do.
        * can see their user profile.
        * can edit their user profile.

    Customer:
    
        * can do anything a logged-in user can do.
        * can do anything a visitor can do.
        * can see a list of their bookmarks.
        * can see a list of their reviews.
        * can see a list of their bookings.

    Agent:
    
        * can do anything a logged-in user can do.
        * can do anything a visitor can do.
        * can create locations.
        * can create tours.
        * can see a list of all locations.
        * can see a list of their tours.
        * can see a list of bookmarks for their tours.
        * can see a list of bookings for their tours.
        * can see a list of reviews for their tours.

    Admin:
    
        * can do anything an agent can do.
        * can do anything a customer can do.
        * can do anything a logged-in user can do.
        * can do anything a visitor can do.
        * can see a list of all users.
        * can see a list of all bookings.
        * cannot edit their user profile email or password.
        * can see a list of all users.
        * can see a list of all bookings.
        
How To (Read Me First!):

    * All of these instructions assume that you are starting from the landing page.
    * All of these instructions assume that your user role allows you to perform these functions.
    * Some of these instructions have alternative ways to accomplish the same task.
    * It is not feasible to list all possible ways of accomplishing all possible tasks.

How To (USERS):
    
    How to sign up
        
        [] Click "Sign up now!"
        [] Enter a name
        [] Enter an email (must be [something]@[something].com)
        [] Enter a password (must be between 6 and 40 characters)
        [] Click "Agent" if you wish to act as a tour agent
        [] Click "Customer" if you wish to act as a customer
        [] Click "Create User"
        
        Note: we have chosen to allow a new user, with one email address, to 
        sign up as:
            * An agent
            * A customer
            * Both agent & customer
            * Neither agent nor customer
    
    How to log in
    
        [] Enter your email and password
        [] Click "Log In"
        
    How to edit your user profile
    
        [] Click "Show My Profile"
        [] Click "Edit Profile"
        [] Make changes
            Note: Users cannot edit their email or password
        [] Click "Update User"
        
    How to cancel your account
    
        [] Click "Show My Profile"
        [] Click "Cancel Account"
    
    How To cancel another user's account
    
        Before testing this task:
        
            You will not be able to cancel another user's account,
            if there is no other user.
            If this is the case, please first see "How to create an account for someone else".
    
        How to perform this task:
        
        [] Click "Show All Users"
        [] Click "Cancel Account" next to the user of interest
        
    How to view a list of all users
    
        [] Click "Show All Users"
        
    How to create an account for someone else
    
        [] Click "Show All Users"
        [] Click "New User"
        [] Follow instructions for "How To Sign Up"

How To (TOURS):

    How to create a tour

        Before testing this task:
        
            You will not be able to create a tour,
            if there are no locations in the system.
            If this is the case, please first see "How to create a location".
    
        How to perform this task:
        
        [] Click "Show All Tours"
        [] Click "New Tour"
        [] Enter details in all provided fields
        [] Click "Create Tour"
        
    How to view a list of all tours

        Before testing this task:
        
            You will not be able to view a list of all tours,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
    
        How to perform this task:
    
        [] Click "Show All Tours"
        
    How to view a list of tours that you created

        Before testing this task:
        
            You will not be able to view a list of tours that you created,
            if you have not created any tours.
            If this is the case, please first see "How to create a tour".
    
        How to perform this task:
    
        [] Click "Show My Tours"
        
    How to search tours using filters

        Before testing this task:
        
            You will not be able to search tours using filters,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
    
        How to perform this task:
        
        [] Click "Show All Tours"
        [] Enter filter criteria under "Filter Results"
        [] Click "Search"
        
    How to view all details for a particular tour

        Before testing this task:
        
            You will not be able to view all details for a particular tour,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
            
            You will not be able to see guests (customers who have booked the tour),
            if no customers have booked the tour.
            If this is the case, please first see "How to book / waitlist a tour".
            
            You will not be able to see photos, if there are no photos for the tour.
            If this is the case, please first see "How to add photos to tours".
            
            You will not be able to see reviews, if there are no reviews for the tour.
            If this is the case, please first see "How to create a review",
            as well as "How to create a review (practical testing tips)".
    
        How to perform this task:
        
        [] Click "Show All Tours"
        [] Click "Show" next to the tour of interest
            Note: Included in this view:
                * Description
                * Price
                * Booking Deadline
                * Start Date
                * End Date
                * Agent
                * Operator Contact
                * Status (In Future / Completed / Cancelled)
                * # Seats
                * # Seats Booked
                * # Seats Available
                * # Seats Waitlisted
                * Itinerary (locations, with starting location first)
                * Guests (customers who have booked the tour)
                * Photos
                * Reviews

    How to edit / update a tour

        Before testing this task:
        
            You will not be able to edit / update a tour,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
            
            You will not be able to edit / update a tour, if the tour is completed,
            per the requirement "an agent can not edit a completed tour".
            If this is the case, please first see "How to create a tour".
    
        How to perform this task:
        
        [] Click "Show All Tours"
        [] Click "Edit" next to the tour of interest
        [] Make the desired changes
        [] Click "Update Tour"
    
    How to cancel a tour

        Before testing this task:
        
            You will not be able to cancel a tour,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
            
            You will not be able to cancel a tour, if the tour is completed,
            per the requirement "an agent can not edit a completed tour".
            If this is the case, please first see "How to create a tour".
    
        How to perform this task:
        
        [] Click "Show All Tours"
        [] Click "Edit" next to the tour of interest
        [] Click "Cancelled"
        [] Click "Update Tour"

    How to delete a tour

        Before testing this task:
        
            You will not be able to delete a tour,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
            
            You will not be able to delete a tour,
            if the tour is completed,
            per the requirement "an agent can not edit a completed tour".
            If this is the case, please first see "How to create a tour".
    
        How to perform this task:
        
        [] Click "Show my tours"
        [] Click "Delete Tour" next to the tour of interest
        
    How to add photos to tours

        Before testing this task:
        
            You will not be able to add photos to a tour,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
            
            You will not be able to add photos to a tour,
            if the tour is completed,
            per the requirement "an agent can not edit a completed tour".
            If this is the case, please first see "How to create a tour".
    
        How to perform this task:
        
        [] Click "Show All Tours"
        [] Click "Edit" next to the tour of interest
        [] Click "Edit Photos"
        [] Click "New [tour name] Photo"
        [] Enter a name for the photo
        [] Choose a file for the photo
        [] Click "Create Photo"
        
    How to delete photos from tours

        Before testing this task:
        
            You will not be able to delete photos from a tour,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
            
            You will not be able to delete photos from a tour,
            if the tour is completed,
            per the requirement "an agent can not edit a completed tour".
            If this is the case, please first see "How to create a tour".
            
            You will not be able to delete photos from a tour,
            if the tour has no photos.
            If this is the case, please first see "How to add photos to tours".
    
        How to perform this task:
        
        [] Click "Show All Tours"
        [] Click "Edit" next to the tour of interest
        [] Click "Edit Photos"
        [] Click "Delete Photo" next to the name of the photo of interest

How To (BOOKMARKS):

    How to bookmark a tour

        Before testing this task:
        
            You will not be able to bookmark a tour,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
    
        How to perform this task:
        
        [] Follow instructions for "How to view all details for a particular tour"
        [] Click "Bookmark"
        [] Click "Create Bookmark"
            This step is easy to forget so be careful to include it!
        
    How to view a list of all bookmarks

        Before testing this task:
        
            You will not be able to view a list of all bookmarks,
            if there are no bookmarks in the system.
            If this is the case, please first see "How to bookmark a tour".
    
        How to perform this task:
        
        [] Click "Show All Bookmarks"

    How to view a list of bookmarks of tours that you created

        Before testing this task:
        
            You will not be able to view a list of bookmarks of tours that you created,
            if you have not created any tours.
            If this is the case, please first see "How to create a tour".
            
            You will not be able to view a list of bookmarks of tours that you created,
            if none of your tours have been bookmarked.
            If this is the case, please first see "How to bookmark a tour".
    
        How to perform this task:
        
        [] Click "Show Bookmarks for My Tours"
        
    How to see which tours you have bookmarked

        Before testing this task:
        
            You will not be able to see which tours you have bookmarked,
            if you have not bookmarked any tours.
            If this is the case, please first see "How to bookmark a tour".
    
        How to perform this task:
        
        [] Click "Show My Bookmarks"
        
    How to delete a bookmark

        Before testing this task:
        
            You will not be able to delete a bookmark,
            if there are no bookmarks in the system.
            If this is the case, please first see "How to bookmark a tour".
    
        How to perform this task:
        
        [] Click "Show My Bookmarks"
        [] Click "Delete Bookmark" next to the bookmark of interest

How To (REVIEWS):

    How to create a review

        Before testing this task:
        
            See "How to create a review (practical testing tips)".
    
        How to perform this task:
        
        [] Click "Show My Reviews"
        [] Click "New Review"
        [] Select a tour among those listed (those you have booked and which
           have ended)
        [] Enter a subject and content
        [] Click "Create Review"
        
    How to create a review (practical testing tips)
    
        Per the requirements:
            "Only future tours can be booked for... [customers may] Submit a
            review for a tour that that customer has already taken"
            Piazza further clarifies that "already taken" means that the tour
            has already ended.
        So, how to create a review in a practical time frame for testing?
        We have found that the following strategy works:
            - Create a tour that takes place in the future
            - Book a seat on this tour
            - Use DB tools to alter the tour dates such that it is a past tour
            - Create a review for the tour
        Alternatively, you could test tour creation and booking one day,
        and reviews the next.
            - In this strategy, make the tour just one day in duration

    How to view a list of all reviews

        Before testing this task:
        
            You will not be able to view a list of all reviews,
            if there are no reviews in the system.
            If this is the case, please first see "How to create a review".
    
        How to perform this task:
        
        [] Click "Show All Reviews"
        
    How to view reviews for tours you have created

        Before testing this task:
        
            You will not be able to view reviews for tours you have created,
            if you have not created any tours.
            If this is the case, please first see "How to create a tour".
            
            You will not be able to view reviews for tours you have created,
            if nobody has review your tours.
            If this is the case, please first see "How to create a review".
    
        How to perform this task:
        
        [] Click "Show Review for My Tours"
        
    How to view reviews you have created

        Before testing this task:
        
            You will not be able to view reviews you have created,
            if you have not created any reviews.
            If this is the case, please first see "How to create a review".
    
        How to perform this task:
        
        [] Click "Show My Reviews"
        
    How to view reviews for a particular tour
        
        See "How to view all details for a particular tour"
    
    How to edit a review

        Before testing this task:
        
            You will not be able to edit a review,
            if there are no reviews in the system.
            If this is the case, please first see "How to create a review".
    
        How to perform this task:
        
        [] Click "Show All Reviews"
        [] Click "Edit" next to the review of interest
        [] Make the desired changes
        [] Click "Update Review"

    How to delete a review

        Before testing this task:
        
            You will not be able to delete a review,
            if there are no reviews in the system.
            If this is the case, please first see "How to create a review".
    
        How to perform this task:
        
        [] Click "Show All Reviews"
        [] Click "Delete Review" next to the review of interest

How To (BOOKINGS / WAITLISTS):

    How to book / waitlist a tour

        Before testing this task:
        
            You will not be able to book / waitlist a tour,
            if there are no tours in the system.
            If this is the case, please first see "How to create a tour".
            
            You will not be able to book / waitlist a tour,
            if the tour's booking deadline has passed.
            If this is the case, please try to book a tour whose booking deadline has not passed.
            If there is no such future tour, please first see "How to create a tour".
    
        How to perform this task:
        
        [] Click "Show All Tours"
        [] Click "Book" next to the tour of interest
        [] Enter the number of seats you wish to book
        [] Select an option from "Book or waitlist?"
        [] Click "Create Booking"
        
    How to view all bookings / waitlistings

        Before testing this task:
        
            You will not be able to view all bookings / waitlistings,
            if there are no bookings / waitlistings in the system.
            If this is the case, please first see "How to book / waitlist a tour".
    
        How to perform this task:
        
        [] Click "Show All Bookings"

    How to view bookings / waitlistings for the tours you have created
    
        Before testing this task:
        
            You will not be able to view bookings / waitlistings for the tours you have created,
            if you have not created any tours.
            If this is the case, please first see "How to create a tour".
        
            You will not be able to view bookings / waitlistings for the tours you have created,
            if nobody has booked / waitlisted your tours.
            If this is the case, please first see "How to book / waitlist a tour".
    
        How to perform this task:
        
        [] Click "Show Bookings for My Tours"
        
    How to view bookings / waitlistings that you have created

        Before testing this task:
        
            You will not be able to view bookings / waitlistings that you have created,
            if you have not created any bookings / waitlistings.
            If this is the case, please first see "How to book / waitlist a tour".
    
        How to perform this task:
        
        [] Click "Show My Bookings"
        
    How to view bookings for a particular tour
        
        See "How to view all details for a particular tour"
        
    How to cancel all seats from a booking

        Before testing this task:
        
            You will not be able to cancel all seats from a booking,
            if you have not created any bookings / waitlistings.
            If this is the case, please first see "How to book / waitlist a tour".
            
            You will not be able to cancel all seats from a booking,
            if the tour has already started.
            If this is the case, please book a future tour (see "How to book / waitlist a tour").
    
        How to perform this task:
        
        [] Click "Show My Bookings"
        [] Click "Cancel Booking" next to the booking of interest
    
    How to cancel a few seats from a booking (or otherwise edit a booking)

        Before testing this task:
        
            You will not be able to cancel a few seats from a booking (or otherwise edit a booking),
            if you have not created any bookings / waitlistings.
            If this is the case, please first see "How to book / waitlist a tour".
            
            You will not be able to cancel a few seats from a booking (or otherwise edit a booking),
            if the tour has already started.
            If this is the case, please book a future tour (see "How to book / waitlist a tour").
    
        How to perform this task:
        
        [] Click "Show My Bookings"
        [] Click "Edit Booking" next to the booking of interest
        [] Make the desired changes
        [] Click "Update Booking"
        
    How to confirm that next customer is enrolled in waitlist after seat cancellation
        
        [] Log in as the FIRST customer (see "How to sign up" / "How to log in")
        [] Book all available seats in a tour (see "How to book / waitlist a tour")
        [] Log in as the SECOND customer (see "How to sign up" / "How to log in")
        [] Waitlist a few seats in this same tour (see "How to book / waitlist a tour")
        [] Log in as the FIRST customer (see "How to log in")
        [] Cancel all your seats on this same tour (see "How to cancel all seats from a booking")
        [] Log in as the SECOND customer(see "How to log in")
        [] Confirm that your waitlist seats have been converted to booked seats (see "How to view bookings / waitlistings that you have created")

How To (LOCATIONS):

    How to create a location
        
        [] Click "Show All Locations"
        [] Click "New Location"
        [] Enter details in all provided fields
        [] Click "Create Location"
        
    How to view all locations

        Before testing this task:
        
            You will not be able to view all locations,
            if there are no locations in the system.
            If this is the case, please first see "How to create a location".
    
        How to perform this task:
        
        [] Click "Show All Locations"
    
    How to edit a location

        Before testing this task:
        
            You will not be able to edit a location,
            if there are no locations in the system.
            If this is the case, please first see "How to create a location".
    
        How to perform this task:
        
        [] Click "Show All Locations"
        [] Click "Edit" next to the location of interest
        [] Make the desired changes
        [] Click "Update Location"
    
    How to delete a location

        Before testing this task:
        
            You will not be able to delete a location,
            if there are no locations in the system.
            If this is the case, please first see "How to create a location".
    
        How to perform this task:
        
        [] Click "Show All Locations"
        [] Click "Destroy Location" next to location of interest

Small Known Bugs

    Every software organization releases software with a few small known bugs (nothing is perfect)!
    Rather than hoping nobody will notice, here we openly state the few small bugs of which we are aware.
    
    1. Repeated Tour in Tour Index View
    
        If you create a tour which visits the same location multiple times,
        then you "Show All Tours" and filter by desired location,
        you will see the same tour listed multiple times.
    
    2. Inappropriate Tour Booking Deadline
    
        It is possible to create a tour whose booking deadline is after the tour's start date.
    
    3. No confirmation required to delete records
    
        It is possible to delete a record (tour, review, etc.) without having to answer an "Are you sure?" prompt.
        
    4. Broken photo links
    
        It is possible that a photo uploaded by one user may not be visible to another user.
        
    5. Ease of forgetting "Create Bookmark" step
    
        See "How to bookmark a tour"
        The "Create Bookmark" step at the end of the process is easy to forget.
        
    6. User with no special role
    
        It is possible to create a user who is neither an agent nor a customer.
        Such a user can do very little that is useful.