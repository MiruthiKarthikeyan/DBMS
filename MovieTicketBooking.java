import java.sql.*;
import java.util.Scanner;

public class MovieTicketBooking {

    static Connection con;
    static Statement st;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        // connection details
        String url = "jdbc:mysql://localhost:3306/movie_db";
        String user = "root";
        String password = "your_password";

        // connect java to mysql
        try {
            // step 1: load the driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // step 2: connect to the database
            con = DriverManager.getConnection(url, user, password);

            // step 3: create a statement to send sql queries
            st = con.createStatement();
            System.out.println("Connected to MySQL successfully");
        } catch (Exception e) {
            System.out.println("Connection failed: " + e);
            return;
        }

        // show all movies (this also proves the connection works)
        try {
            showMovies();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // menu
        int choice = 0;
        while (choice != 5) {
            System.out.println();
            System.out.println("===== MOVIE TICKET BOOKING SYSTEM =====");
            System.out.println("1. View movies and available seats");
            System.out.println("2. Book ticket");
            System.out.println("3. Cancel ticket");
            System.out.println("4. View all bookings");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine());

                if (choice == 1) {
                    viewShows();
                } else if (choice == 2) {
                    bookTicket();
                } else if (choice == 3) {
                    cancelTicket();
                } else if (choice == 4) {
                    viewBookings();
                } else if (choice == 5) {
                    System.out.println("Thank you!");
                } else {
                    System.out.println("Please enter 1 to 5");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter numbers only");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        // close the connection
        try {
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // show all movies with their price
    static void showMovies() throws Exception {
        ResultSet rs = st.executeQuery("select * from movies");

        System.out.println();
        System.out.println("Movies available:");
        System.out.println("MovieID\tTitle\tPrice");
        while (rs.next()) {
            System.out.println(rs.getInt("movie_id") + "\t"
                    + rs.getString("title") + "\t"
                    + rs.getDouble("price"));
        }
    }

    // 1. show every movie, its shows and the seats available
    static void viewShows() throws Exception {
        ResultSet rs = st.executeQuery(
            "select s.show_id, m.title, s.show_time, m.price, s.total_seats, s.available_seats "
            + "from movies m, shows s where m.movie_id = s.movie_id order by s.show_id");

        System.out.println();
        System.out.printf("%-8s %-10s %-10s %-8s %-6s %s%n",
                "ShowID", "Movie", "Time", "Price", "Total", "Available");
        while (rs.next()) {
            System.out.printf("%-8d %-10s %-10s %-8.2f %-6d %d%n",
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getDouble(4), rs.getInt(5), rs.getInt(6));
        }
    }

    // 2. book a ticket
    static void bookTicket() throws Exception {
        viewShows();

        System.out.print("\nEnter Show ID: ");
        int showId = Integer.parseInt(sc.nextLine());
        System.out.print("Enter your name: ");
        String name = sc.nextLine().trim();
        System.out.print("Enter number of seats: ");
        int seats = Integer.parseInt(sc.nextLine());

        if (name.equals("") || seats <= 0) {
            System.out.println("Please enter a valid name and number of seats");
            return;
        }

        // find the price and the seats available for this show
        ResultSet rs = st.executeQuery(
            "select m.price, s.available_seats from movies m, shows s "
            + "where m.movie_id = s.movie_id and s.show_id = " + showId);
        if (!rs.next()) {
            System.out.println("Invalid Show ID");
            return;
        }
        double price = rs.getDouble(1);
        int available = rs.getInt(2);

        if (seats > available) {
            System.out.println("Sorry, only " + available + " seats are available");
            return;
        }

        double total = price * seats;

        // save the booking
        name = name.replace("'", "''");
        st.executeUpdate("insert into bookings(customer_name, show_id, seats, total_amount) values('"
                + name + "'," + showId + "," + seats + "," + total + ")");

        // get the booking id that was just created
        rs = st.executeQuery("select last_insert_id()");
        rs.next();
        int bookingId = rs.getInt(1);

        // reduce the available seats
        st.executeUpdate("update shows set available_seats = available_seats - " + seats
                + " where show_id = " + showId);

        System.out.println("\nTicket booked successfully!");
        System.out.println("Booking ID   : " + bookingId);
        System.out.println("Seats booked : " + seats);
        System.out.println("Total amount : Rs. " + total);
    }

    // 3. cancel a ticket
    static void cancelTicket() throws Exception {
        viewBookings();

        System.out.print("\nEnter Booking ID to cancel: ");
        int bookingId = Integer.parseInt(sc.nextLine());

        // find which show and how many seats were booked
        ResultSet rs = st.executeQuery(
            "select show_id, seats from bookings where booking_id = " + bookingId);
        if (!rs.next()) {
            System.out.println("Booking ID not found");
            return;
        }
        int showId = rs.getInt(1);
        int seats = rs.getInt(2);

        // delete the booking
        st.executeUpdate("delete from bookings where booking_id = " + bookingId);

        // add the seats back
        st.executeUpdate("update shows set available_seats = available_seats + " + seats
                + " where show_id = " + showId);

        System.out.println("Ticket cancelled. " + seats + " seat(s) are available again.");
    }

    // 4. show all bookings
    static void viewBookings() throws Exception {
        ResultSet rs = st.executeQuery(
            "select b.booking_id, b.customer_name, m.title, s.show_time, b.seats, b.total_amount "
            + "from bookings b, shows s, movies m "
            + "where b.show_id = s.show_id and s.movie_id = m.movie_id order by b.booking_id");

        System.out.println();
        System.out.printf("%-10s %-15s %-10s %-10s %-6s %s%n",
                "BookingID", "Customer", "Movie", "Time", "Seats", "Total");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.printf("%-10d %-15s %-10s %-10s %-6d %.2f%n",
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getInt(5), rs.getDouble(6));
        }
        if (!found) {
            System.out.println("No bookings yet");
        }
    }
}
